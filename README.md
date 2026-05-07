# TakStud — Educational Management SPA

Live demo: https://non-s.github.io/TakStud

## What it is

A single-page application for school management. Demonstrates frontend architecture patterns that survive real use: role-based access control that actually changes the UI, event delegation instead of scattered inline handlers, a localStorage proxy that makes persistence transparent, and a toast system that replaces the browser's modal `alert()`.

## Architecture

### Storage layer — localStorage proxy

```js
const DB = {
    get students(){ return JSON.parse(localStorage.getItem('ts_students')||'[]') },
    set students(v){ localStorage.setItem('ts_students',JSON.stringify(v)) },
};
```

The getter/setter pattern means every read is always fresh and every write is immediately persisted. There is no in-memory cache that can go stale. The tradeoff is parsing on every read — acceptable for datasets of this size.

### RBAC — enforced at render time

```js
const RBAC = {
    teacher: { views: ['dashboard','students','tasks','notices','schedule'], canWrite: true,  canExport: false },
    student: { views: ['dashboard','tasks','notices'],                       canWrite: false, canExport: false },
    admin:   { views: ['dashboard','students','tasks','notices','schedule'], canWrite: true,  canExport: true  },
};
```

`applyRBAC()` runs on every role change. It hides inaccessible nav items, add buttons, and action columns — then re-renders the current view. The render functions themselves check `RBAC[currentRole].canWrite` before emitting edit/delete buttons. You can't bypass the restriction by caching a rendered view.

### Event delegation

Instead of `onclick="editStudent(3)"` inline in every table row (which leaks global references and breaks on re-render), a single listener sits on the container:

```js
document.getElementById('studentsBody').addEventListener('click', e => {
    const btn = e.target.closest('[data-action]');
    if (!btn) return;
    if (btn.dataset.action === 'edit-student') openEditStudent(Number(btn.dataset.id));
    else if (btn.dataset.action === 'del-student') deleteStudent(Number(btn.dataset.id));
});
```

The rendered HTML carries `data-action` and `data-id`. One listener survives re-renders.

### Keyboard shortcuts

- `Ctrl+N` — opens the "new" modal for the current view
- `Escape` — closes any open modal
- `Enter` inside a modal field (not textarea) — triggers save

### Toast notifications

Replaces `alert()` with a CSS-animated toast injected once and reused. Type (`success`, `error`, `warn`) maps to border and text color.

### Export (admin only)

Dumps the full DB as `takstud-YYYY-MM-DD.json` using `Blob` + `URL.createObjectURL`. The button is invisible to non-admin roles.

## Files

```
TakStud/
├── index.html   — markup and modal templates
├── style.css    — component styles, dark theme, responsive sidebar
├── script.js    — DB, RBAC, renders, CRUD, event delegation
└── README.md
```

No build step. No dependencies beyond Font Awesome for icons.
