# TakStud — Educational Management SPA

Live demo: https://non-s.github.io/TakStud

Stack: GitHub Pages (front-end) + Supabase (PostgreSQL, Auth, Realtime)

---

## Setup in 5 Steps

### 1. Create a Supabase project

Go to [supabase.com](https://supabase.com), create a free account and a new project. Save the **Project URL** and the **anon public key** (Settings → API).

### 2. Configure credentials

In `script.js`, replace:

```js
const SUPABASE_URL      = 'https://xxxxxxxxxxxx.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';
```

The anon key is public by design — Row Level Security protects data on the server side.

### 3. Run the SQL schema

In the Supabase Dashboard → SQL Editor, run the block below:

```sql
-- ── Tables ───────────────────────────────────────────────────────────────

CREATE TABLE schools (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE profiles (
    id         UUID PRIMARY KEY REFERENCES auth.users ON DELETE CASCADE,
    full_name  TEXT NOT NULL,
    role       TEXT NOT NULL DEFAULT 'teacher'
                   CHECK (role IN ('teacher', 'student', 'admin')),
    school_id  UUID REFERENCES schools(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE students (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL,
    cls        TEXT NOT NULL,
    email      TEXT DEFAULT '',
    school_id  UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE tasks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       TEXT NOT NULL,
    subject     TEXT DEFAULT '',
    due_date    DATE,
    description TEXT DEFAULT '',
    done        BOOLEAN DEFAULT FALSE,
    school_id   UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE notices (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title      TEXT NOT NULL,
    content    TEXT NOT NULL,
    school_id  UUID NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ── Row Level Security ────────────────────────────────────────────────────

ALTER TABLE schools  ENABLE ROW LEVEL SECURITY;
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks    ENABLE ROW LEVEL SECURITY;
ALTER TABLE notices  ENABLE ROW LEVEL SECURITY;

-- Helper functions (SECURITY DEFINER = run as postgres, bypass RLS)
CREATE OR REPLACE FUNCTION my_school_id()
RETURNS UUID LANGUAGE SQL STABLE SECURITY DEFINER AS $$
    SELECT school_id FROM profiles WHERE id = auth.uid();
$$;

CREATE OR REPLACE FUNCTION my_role()
RETURNS TEXT LANGUAGE SQL STABLE SECURITY DEFINER AS $$
    SELECT role FROM profiles WHERE id = auth.uid();
$$;

-- Profiles: each user can only read and update their own profile
CREATE POLICY "own profile read"   ON profiles FOR SELECT USING (id = auth.uid());
CREATE POLICY "own profile update" ON profiles FOR UPDATE USING (id = auth.uid());

-- Schools: school members can only see their own school
CREATE POLICY "school read" ON schools FOR SELECT USING (id = my_school_id());

-- Students
CREATE POLICY "students read"   ON students FOR SELECT
    USING (school_id = my_school_id());
CREATE POLICY "students insert" ON students FOR INSERT
    WITH CHECK (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "students update" ON students FOR UPDATE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "students delete" ON students FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));

-- Tasks
CREATE POLICY "tasks read"   ON tasks FOR SELECT
    USING (school_id = my_school_id());
CREATE POLICY "tasks insert" ON tasks FOR INSERT
    WITH CHECK (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "tasks update" ON tasks FOR UPDATE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "tasks delete" ON tasks FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));

-- Notices
CREATE POLICY "notices read"   ON notices FOR SELECT
    USING (school_id = my_school_id());
CREATE POLICY "notices insert" ON notices FOR INSERT
    WITH CHECK (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "notices delete" ON notices FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));

-- ── RPC: creates school + profile in a single atomic transaction ──────────

CREATE OR REPLACE FUNCTION create_school_and_profile(
    p_user_id    UUID,
    p_full_name  TEXT,
    p_school_name TEXT
)
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER AS $$
DECLARE
    v_school_id UUID;
BEGIN
    INSERT INTO schools (name) VALUES (p_school_name)
        RETURNING id INTO v_school_id;
    INSERT INTO profiles (id, full_name, role, school_id)
        VALUES (p_user_id, p_full_name, 'admin', v_school_id);
END;
$$;
```

### 4. (Recommended) Disable email confirmation for testing

Authentication → Settings → uncheck "Enable email confirmations".  
In production, keep it enabled and configure a custom email domain.

### 5. Configure the redirect URL

Authentication → URL Configuration → add your GitHub Pages URL:

```
https://non-s.github.io/TakStud
```

---

## Architecture

### Why Supabase + GitHub Pages?

The front-end is static (plain HTML/CSS/JS) — no Node server, no PHP, no runtime needed. Supabase exposes PostgreSQL over REST and WebSocket. The result is a stack with no application server: GitHub Pages serves the files, Supabase handles data and authentication.

### Row Level Security — the real difference

In a localStorage-only approach, RBAC is cosmetic: anyone can open DevTools and swap their role. With RLS, policies live in the database:

```sql
-- A student tries to delete a student record: the database rejects it at the data layer
CREATE POLICY "students delete" ON students FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
```

The front-end doesn't need to trust itself. Even if someone tampers with the JS in the browser, the server returns a 403.

### Multi-tenancy via school_id

Every record (student, task, notice) carries a `school_id`. RLS policies filter by `my_school_id()`, which returns the authenticated user's school. Multiple schools share the same Supabase instance without ever seeing each other's data.

### Realtime

```js
sb.channel('db-changes')
    .on('postgres_changes', { event: '*', schema: 'public', table: 'students' }, () => renderStudents())
    ...
    .subscribe();
```

When a teacher adds a student, all other teachers in the same school see the update without reloading the page. Supabase Realtime uses PostgreSQL LISTEN/NOTIFY under the hood.

### XSS prevention

Every external string written to `innerHTML` goes through `esc()`:

```js
const esc = s => String(s ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
```

---

## Files

```
TakStud/
├── index.html   — markup, auth overlay, modals
├── style.css    — dark theme, auth card, toast, responsive
├── script.js    — Supabase client, auth, async CRUD, realtime, RBAC
└── README.md    — this file: SQL schema + setup guide
```

No build step. No bundler. No framework.
