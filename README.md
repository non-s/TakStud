# TakStud — Educational Management SPA

Live demo: https://non-s.github.io/TakStud

Stack: GitHub Pages (front-end) + Supabase (PostgreSQL, Auth, Realtime)

---

## Setup em 5 passos

### 1. Crie um projeto no Supabase

Acesse [supabase.com](https://supabase.com), crie uma conta gratuita e um novo projeto. Guarde a **Project URL** e a **anon public key** (Settings → API).

### 2. Configure as credenciais

Em `script.js`, substitua:

```js
const SUPABASE_URL      = 'https://xxxxxxxxxxxx.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';
```

A anon key é pública por design — o Row Level Security protege os dados no servidor.

### 3. Execute o schema SQL

No Supabase Dashboard → SQL Editor, execute o bloco abaixo:

```sql
-- ── Tabelas ──────────────────────────────────────────────────────────────

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

-- Funções auxiliares (SECURITY DEFINER = rodam como postgres, passam pelo RLS)
CREATE OR REPLACE FUNCTION my_school_id()
RETURNS UUID LANGUAGE SQL STABLE SECURITY DEFINER AS $$
    SELECT school_id FROM profiles WHERE id = auth.uid();
$$;

CREATE OR REPLACE FUNCTION my_role()
RETURNS TEXT LANGUAGE SQL STABLE SECURITY DEFINER AS $$
    SELECT role FROM profiles WHERE id = auth.uid();
$$;

-- Profiles: cada usuário vê e edita só o próprio perfil
CREATE POLICY "own profile read"   ON profiles FOR SELECT USING (id = auth.uid());
CREATE POLICY "own profile update" ON profiles FOR UPDATE USING (id = auth.uid());

-- Schools: membros da escola veem a própria escola
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

-- ── RPC: cria escola + perfil em uma transação atômica ───────────────────

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

### 4. (Recomendado) Desative confirmação de e-mail para testes

Authentication → Settings → desmarque "Enable email confirmations".  
Em produção real, mantenha ativo e configure um domínio de e-mail.

### 5. Configure o redirect URL

Authentication → URL Configuration → adicione sua URL do GitHub Pages:

```
https://non-s.github.io/TakStud
```

---

## Arquitetura

### Por que Supabase + GitHub Pages?

O front-end é estático (HTML/CSS/JS puro) — não precisa de servidor Node, PHP ou qualquer runtime. O Supabase expõe o PostgreSQL via REST e WebSocket. O resultado é uma stack sem servidor de aplicação: GitHub Pages serve os arquivos, Supabase cuida dos dados e da autenticação.

### Row Level Security — a diferença real

No modo anterior (localStorage), o RBAC era cosmético: qualquer pessoa abria o DevTools e trocava de role. Com RLS, as políticas vivem no banco:

```sql
-- Aluno tenta deletar um aluno: o banco rejeita na camada de dados
CREATE POLICY "students delete" ON students FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
```

O front-end não precisa confiar em si mesmo. Mesmo que alguém manipule o JS no browser, o servidor retorna erro 403.

### Multi-tenancy via school_id

Cada registro (aluno, tarefa, comunicado) carrega `school_id`. As políticas de RLS filtram por `my_school_id()`, que retorna o school_id do usuário autenticado. Múltiplas escolas usam a mesma instância Supabase sem ver dados umas das outras.

### Real-time

```js
sb.channel('db-changes')
    .on('postgres_changes', { event: '*', schema: 'public', table: 'students' }, () => renderStudents())
    ...
    .subscribe();
```

Quando um professor adiciona um aluno, todos os outros professores da mesma escola veem a atualização sem recarregar a página. O Supabase Realtime usa PostgreSQL LISTEN/NOTIFY por baixo.

### XSS eliminado

Toda string de origem externa que vai para `innerHTML` passa por `esc()`:

```js
const esc = s => String(s ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
```

---

## Arquivos

```
TakStud/
├── index.html   — markup, auth overlay, modais
├── style.css    — dark theme, auth card, toast, responsivo
├── script.js    — Supabase client, auth, CRUD async, real-time, RBAC
└── README.md    — este arquivo: SQL schema + guia de setup
```

Nenhum build step. Nenhum bundler. Nenhum framework.
