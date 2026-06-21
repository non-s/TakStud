# TakStud - SPA de Gestao Educacional

Demo online: https://non-s.github.io/TakStud

Tecnologias: GitHub Pages + Firebase Authentication + Cloud Firestore.

## Firebase

O app ja esta configurado para o projeto Firebase `non-s-firebase-20260621`.

Arquivos principais:

- `firebase-config.js`: configuracao do Firebase Web app.
- `script.js`: Auth, CRUD, realtime e RBAC usando Firebase.
- `index.html`: SPA estatica.
- `style.css`: tema e layout.

## Auth

- Cadastro e login por e-mail/senha.
- Recuperacao de senha pelo Firebase Auth.
- O primeiro usuario criado em uma escola vira `admin`.

## Dados

Colecoes usadas no Firestore:

- `takstud_schools/{schoolId}`
- `takstud_profiles/{uid}`
- `takstud_students/{studentId}`
- `takstud_tasks/{taskId}`
- `takstud_notices/{noticeId}`
- `takstud_schedules/{scheduleId}`

## Seguranca

As permissoes ficam nas Firebase Security Rules publicadas no projeto:

- Cada usuario acessa apenas sua escola.
- `teacher` e `admin` escrevem alunos, tarefas e avisos.
- Apenas `admin` gerencia horarios.

## Desenvolvimento local

Sirva os arquivos por HTTP, porque `script.js` usa ES modules:

```bash
python -m http.server 5177
```

Depois abra:

```text
http://127.0.0.1:5177/TakStud/
```
