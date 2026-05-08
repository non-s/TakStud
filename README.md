# TakStud — SPA de Gestão Educacional

Demo online: https://non-s.github.io/TakStud

Tecnologias: GitHub Pages (front-end) + Supabase (PostgreSQL, Auth, Realtime)

---

## Configuração em 5 Passos

### 1. Crie um projeto no Supabase

Acesse [supabase.com](https://supabase.com), crie uma conta gratuita e um novo projeto. Salve a **URL do projeto** e a **chave pública anon** (Configurações → API).

### 2. Configure as credenciais

Em `script.js`, substitua:

```js
const SUPABASE_URL      = 'https://xxxxxxxxxxxx.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';
```

A chave anon é pública por design — o Row Level Security protege os dados no lado do servidor.

### 3. Execute o esquema SQL

No Supabase Dashboard → SQL Editor, execute o bloco abaixo:

```sql
-- ── Tabelas ───────────────────────────────────────────────────────────────

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

-- Funções auxiliares (SECURITY DEFINER = executa como postgres, ignora RLS)
CREATE OR REPLACE FUNCTION my_school_id()
RETURNS UUID LANGUAGE SQL STABLE SECURITY DEFINER AS $$
    SELECT school_id FROM profiles WHERE id = auth.uid();
$$;

CREATE OR REPLACE FUNCTION my_role()
RETURNS TEXT LANGUAGE SQL STABLE SECURITY DEFINER AS $$
    SELECT role FROM profiles WHERE id = auth.uid();
$$;

-- Perfis: cada usuário só pode ler e atualizar o próprio perfil
CREATE POLICY "own profile read"   ON profiles FOR SELECT USING (id = auth.uid());
CREATE POLICY "own profile update" ON profiles FOR UPDATE USING (id = auth.uid());

-- Escolas: membros só enxergam a própria escola
CREATE POLICY "school read" ON schools FOR SELECT USING (id = my_school_id());

-- Alunos
CREATE POLICY "students read"   ON students FOR SELECT
    USING (school_id = my_school_id());
CREATE POLICY "students insert" ON students FOR INSERT
    WITH CHECK (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "students update" ON students FOR UPDATE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "students delete" ON students FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));

-- Tarefas
CREATE POLICY "tasks read"   ON tasks FOR SELECT
    USING (school_id = my_school_id());
CREATE POLICY "tasks insert" ON tasks FOR INSERT
    WITH CHECK (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "tasks update" ON tasks FOR UPDATE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "tasks delete" ON tasks FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));

-- Avisos
CREATE POLICY "notices read"   ON notices FOR SELECT
    USING (school_id = my_school_id());
CREATE POLICY "notices insert" ON notices FOR INSERT
    WITH CHECK (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
CREATE POLICY "notices delete" ON notices FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));

-- ── RPC: cria escola + perfil em uma única transação atômica ──────────────

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

### 4. (Recomendado) Desabilitar confirmação de e-mail para testes

Authentication → Settings → desmarque "Enable email confirmations".  
Em produção, mantenha habilitado e configure um domínio de e-mail personalizado.

### 5. Configure a URL de redirecionamento

Authentication → URL Configuration → adicione a URL do seu GitHub Pages:

```
https://non-s.github.io/TakStud
```

---

## Arquitetura

### Por que Supabase + GitHub Pages?

O front-end é estático (HTML/CSS/JS puro) — sem servidor Node, sem PHP, sem runtime necessário. O Supabase expõe o PostgreSQL via REST e WebSocket. O resultado é uma stack sem servidor de aplicação: o GitHub Pages serve os arquivos e o Supabase cuida dos dados e da autenticação.

### Row Level Security — a diferença real

Em uma abordagem somente com localStorage, o RBAC é cosmético: qualquer pessoa pode abrir o DevTools e trocar sua role. Com RLS, as políticas ficam no banco de dados:

```sql
-- Um aluno tenta excluir um registro de aluno: o banco rejeita na camada de dados
CREATE POLICY "students delete" ON students FOR DELETE
    USING (school_id = my_school_id() AND my_role() IN ('teacher','admin'));
```

O front-end não precisa confiar em si mesmo. Mesmo que alguém altere o JS no navegador, o servidor retorna 403.

### Multi-tenancy via school_id

Cada registro (aluno, tarefa, aviso) carrega um `school_id`. As políticas de RLS filtram por `my_school_id()`, que retorna a escola do usuário autenticado. Múltiplas escolas compartilham a mesma instância do Supabase sem jamais ver os dados umas das outras.

### Realtime

```js
sb.channel('db-changes')
    .on('postgres_changes', { event: '*', schema: 'public', table: 'students' }, () => renderStudents())
    ...
    .subscribe();
```

Quando um professor adiciona um aluno, todos os outros professores da mesma escola veem a atualização sem recarregar a página. O Supabase Realtime usa PostgreSQL LISTEN/NOTIFY internamente.

### Prevenção de XSS

Toda string externa escrita em `innerHTML` passa pela função `esc()`:

```js
const esc = s => String(s ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
```

---

## Arquivos

```
TakStud/
├── index.html   — marcação, overlay de autenticação, modais
├── style.css    — tema escuro, card de auth, toast, responsivo
├── script.js    — cliente Supabase, auth, CRUD assíncrono, realtime, RBAC
└── README.md    — este arquivo: esquema SQL + guia de configuração
```

Sem etapa de build. Sem bundler. Sem framework.
