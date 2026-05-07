/* ── DB proxy — localStorage with getter/setter interception ── */
const DB = {
    get students(){ return JSON.parse(localStorage.getItem('ts_students')||'[]') },
    set students(v){ localStorage.setItem('ts_students',JSON.stringify(v)) },
    get tasks(){ return JSON.parse(localStorage.getItem('ts_tasks')||'[]') },
    set tasks(v){ localStorage.setItem('ts_tasks',JSON.stringify(v)) },
    get notices(){ return JSON.parse(localStorage.getItem('ts_notices')||'[]') },
    set notices(v){ localStorage.setItem('ts_notices',JSON.stringify(v)) },
};

let taskFilter = 'all', studentSearch = '', editingStudentId = null, currentRole = 'teacher';

/* ── RBAC — permissions enforced at render + action level ── */
const RBAC = {
    teacher: { views: ['dashboard','students','tasks','notices','schedule'], canWrite: true, canExport: false },
    student: { views: ['dashboard','tasks','notices'], canWrite: false, canExport: false },
    admin:   { views: ['dashboard','students','tasks','notices','schedule'], canWrite: true, canExport: true },
};

function applyRBAC() {
    const p = RBAC[currentRole];
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.style.display = p.views.includes(btn.dataset.view) ? '' : 'none';
    });
    document.querySelectorAll('.btn-add').forEach(btn => {
        btn.style.display = p.canWrite ? '' : 'none';
    });
    const exportBtn = document.getElementById('exportBtn');
    if (exportBtn) exportBtn.style.display = p.canExport ? '' : 'none';
    const activeView = document.querySelector('.view.active');
    if (activeView) {
        const name = activeView.id.replace('view-', '');
        if (!p.views.includes(name)) showView('dashboard');
        else {
            ({ dashboard: renderDashboard, students: renderStudents, tasks: renderTasks,
               notices: renderNotices, schedule: renderSchedule })[name]?.();
        }
    }
}

/* ── Schedule data ── */
const SCHEDULE = [
    { time:'07:00', mon:'Matemática', tue:'Português',   wed:'História',   thu:'Ciências',   fri:'Ed. Física' },
    { time:'08:00', mon:'Português',  tue:'Matemática',  wed:'Ciências',   thu:'Matemática', fri:'Artes'      },
    { time:'09:00', mon:'História',   tue:'Ciências',    wed:'Matemática', thu:'Português',  fri:'Inglês'     },
    { time:'10:30', mon:'Ciências',   tue:'História',    wed:'Inglês',     thu:'História',   fri:'Matemática' },
    { time:'11:30', mon:'Inglês',     tue:'Ed. Física',  wed:'Português',  thu:'Artes',      fri:'Português'  },
];

/* ── Seed demo data ── */
if (!localStorage.getItem('ts_seeded')) {
    const now = new Date().toISOString();
    DB.students = [
        { id:1, name:'Ana Souza',    cls:'3A', email:'ana@escola.edu.br',   createdAt: now },
        { id:2, name:'Bruno Lima',   cls:'3A', email:'bruno@escola.edu.br', createdAt: now },
        { id:3, name:'Carla Santos', cls:'2B', email:'carla@escola.edu.br', createdAt: now },
    ];
    DB.tasks = [
        { id:1, title:'Lista de Exercícios — Cap. 5', subject:'Matemática', due:'2026-05-15', desc:'Exercícios pares da pág. 120-130.', done:false, createdAt: now },
        { id:2, title:'Redação: Meio Ambiente',        subject:'Português',  due:'2026-05-20', desc:'Mínimo 30 linhas.',               done:true,  createdAt: now },
        { id:3, title:'Mapa Conceitual — Células',     subject:'Ciências',   due:'2026-05-22', desc:'Usar caneta e régua.',            done:false, createdAt: now },
    ];
    DB.notices = [
        { id:1, title:'Reunião de Pais — 20/05',  content:'A reunião será às 19h no auditório. Confirme presença.', date:'2026-05-07', createdAt: now },
        { id:2, title:'Simulado ENEM — 28/05',    content:'Traga documento com foto. Início às 8h pontualmente.',   date:'2026-05-07', createdAt: now },
    ];
    localStorage.setItem('ts_seeded', '1');
}

/* ── Utilities ── */
const uid = () => Date.now() + Math.floor(Math.random() * 1000);
const formatDate = d => { if (!d) return ''; const [y, m, day] = d.split('-'); return `${day}/${m}/${y}`; };
const debounce = (fn, ms = 250) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };

/* ── Toast ── */
let toastTimer;
function toast(msg, type = 'success') {
    let el = document.getElementById('ts-toast');
    if (!el) {
        el = document.createElement('div');
        el.id = 'ts-toast';
        document.body.appendChild(el);
    }
    el.textContent = msg;
    el.className = `ts-toast ts-toast-${type} show`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 2500);
}

/* ── Navigation ── */
function showView(view) {
    if (!RBAC[currentRole].views.includes(view)) return;
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById(`view-${view}`).classList.add('active');
    document.querySelector(`[data-view="${view}"]`)?.classList.add('active');
    ({ dashboard: renderDashboard, students: renderStudents, tasks: renderTasks,
       notices: renderNotices, schedule: renderSchedule })[view]?.();
}

/* ── Render ── */
function renderDashboard() {
    const tasks = DB.tasks, students = DB.students, notices = DB.notices;
    document.getElementById('statStudents').textContent = students.length;
    document.getElementById('statTasks').textContent    = tasks.length;
    document.getElementById('statDone').textContent     = tasks.filter(t => t.done).length;
    document.getElementById('statNotices').textContent  = notices.length;
    document.getElementById('dashPendingTasks').innerHTML =
        tasks.filter(t => !t.done).slice(0, 5)
             .map(t => `<li><i class="fas fa-circle"></i>${t.title}</li>`).join('')
        || '<li class="empty">Nenhuma tarefa pendente.</li>';
    document.getElementById('dashNotices').innerHTML =
        notices.slice(0, 3)
               .map(n => `<li><i class="fas fa-circle"></i>${n.title}</li>`).join('')
        || '<li class="empty">Nenhum comunicado.</li>';
}

function renderStudents() {
    const canWrite = RBAC[currentRole].canWrite;
    const filtered = DB.students.filter(s =>
        !studentSearch || s.name.toLowerCase().includes(studentSearch.toLowerCase()));
    document.getElementById('studentsBody').innerHTML = filtered.length
        ? filtered.map(s => `<tr>
            <td>${s.name}</td>
            <td><span class="badge-class">${s.cls}</span></td>
            <td>${s.email || '—'}</td>
            <td>${canWrite
                ? `<div class="td-actions">
                     <button class="btn-icon-sm edit" data-action="edit-student" data-id="${s.id}" title="Editar"><i class="fas fa-edit"></i></button>
                     <button class="btn-icon-sm"      data-action="del-student"  data-id="${s.id}" title="Excluir"><i class="fas fa-trash"></i></button>
                   </div>`
                : '—'}</td>
          </tr>`).join('')
        : `<tr><td colspan="4"><div class="empty-state"><i class="fas fa-users"></i><p>Nenhum aluno encontrado.</p></div></td></tr>`;
}

function renderTasks() {
    const canWrite = RBAC[currentRole].canWrite;
    let tasks = DB.tasks;
    if (taskFilter === 'pending') tasks = tasks.filter(t => !t.done);
    if (taskFilter === 'done')    tasks = tasks.filter(t =>  t.done);
    document.getElementById('tasksList').innerHTML = tasks.length
        ? tasks.map(t => `<div class="task-item ${t.done ? 'done' : ''}">
            <div class="task-check ${t.done ? 'checked' : ''}"
                 ${canWrite ? `data-action="toggle-task" data-id="${t.id}"` : ''}>
                ${t.done ? '<i class="fas fa-check"></i>' : ''}
            </div>
            <div class="task-body">
                <div class="task-title">${t.title}</div>
                <div class="task-meta">
                    <span><i class="fas fa-book"></i>${t.subject}</span>
                    ${t.due ? `<span><i class="fas fa-calendar"></i>${formatDate(t.due)}</span>` : ''}
                </div>
                ${t.desc ? `<div class="task-desc">${t.desc}</div>` : ''}
            </div>
            ${canWrite ? `<button class="task-del" data-action="del-task" data-id="${t.id}"><i class="fas fa-times"></i></button>` : ''}
          </div>`).join('')
        : '<div class="empty-state"><i class="fas fa-tasks"></i><p>Nenhuma tarefa.</p></div>';
}

function renderNotices() {
    const canWrite = RBAC[currentRole].canWrite;
    document.getElementById('noticesList').innerHTML = DB.notices.length
        ? DB.notices.map(n => `<div class="notice-item">
            <div class="notice-header">
                <span class="notice-title">${n.title}</span>
                <div style="display:flex;align-items:center;gap:.7rem">
                    <span class="notice-date">${n.date}</span>
                    ${canWrite ? `<button class="btn-icon-sm" data-action="del-notice" data-id="${n.id}"><i class="fas fa-trash"></i></button>` : ''}
                </div>
            </div>
            <div class="notice-content">${n.content}</div>
          </div>`).join('')
        : '<div class="empty-state"><i class="fas fa-bullhorn"></i><p>Nenhum comunicado.</p></div>';
}

function renderSchedule() {
    const days = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta'];
    const keys  = ['mon', 'tue', 'wed', 'thu', 'fri'];
    let html = '<div class="sch-header">Hora</div>';
    days.forEach(d => html += `<div class="sch-header">${d}</div>`);
    SCHEDULE.forEach(row => {
        html += `<div class="sch-time">${row.time}</div>`;
        keys.forEach(k => html += `<div class="sch-cell"><span class="sch-subject">${row[k]}</span></div>`);
    });
    document.getElementById('scheduleGrid').innerHTML = html;
}

/* ── Modal helpers ── */
const openModal    = id => document.getElementById(id).classList.add('open');
const closeModal   = id => document.getElementById(id).classList.remove('open');
const closeAllModals = () => document.querySelectorAll('.modal.open').forEach(m => m.classList.remove('open'));

function openEditStudent(id) {
    const s = DB.students.find(s => s.id === id);
    if (!s) return;
    editingStudentId = id;
    document.getElementById('sName').value  = s.name;
    document.getElementById('sClass').value = s.cls;
    document.getElementById('sEmail').value = s.email || '';
    document.getElementById('studentModalTitle').textContent = 'Editar Aluno';
    openModal('studentModal');
}

/* ── CRUD ── */
function saveStudent() {
    const name  = document.getElementById('sName').value.trim();
    const cls   = document.getElementById('sClass').value.trim();
    const email = document.getElementById('sEmail').value.trim();
    if (!name) return toast('Nome é obrigatório.', 'error');
    if (!cls)  return toast('Turma é obrigatória.', 'error');
    const students = DB.students;
    if (editingStudentId) {
        const s = students.find(s => s.id === editingStudentId);
        if (s) { s.name = name; s.cls = cls; s.email = email; }
        toast('Aluno atualizado.');
    } else {
        students.push({ id: uid(), name, cls, email, createdAt: new Date().toISOString() });
        toast('Aluno adicionado.');
    }
    DB.students = students;
    closeModal('studentModal');
    renderStudents();
    renderDashboard();
}

function deleteStudent(id) {
    if (!confirm('Excluir este aluno?')) return;
    DB.students = DB.students.filter(s => s.id !== id);
    renderStudents();
    renderDashboard();
    toast('Aluno removido.', 'warn');
}

function saveTask() {
    const title   = document.getElementById('tTitle').value.trim();
    const subject = document.getElementById('tSubject').value.trim();
    if (!title) return toast('Título é obrigatório.', 'error');
    const tasks = DB.tasks;
    tasks.unshift({
        id: uid(), title, subject,
        due: document.getElementById('tDue').value,
        desc: document.getElementById('tDesc').value.trim(),
        done: false, createdAt: new Date().toISOString(),
    });
    DB.tasks = tasks;
    ['tTitle','tSubject','tDue','tDesc'].forEach(id => document.getElementById(id).value = '');
    closeModal('taskModal');
    renderTasks();
    renderDashboard();
    toast('Tarefa adicionada.');
}

function toggleTask(id) {
    const tasks = DB.tasks;
    const t = tasks.find(t => t.id === id);
    if (t) t.done = !t.done;
    DB.tasks = tasks;
    renderTasks();
    renderDashboard();
}

function deleteTask(id) {
    DB.tasks = DB.tasks.filter(t => t.id !== id);
    renderTasks();
    renderDashboard();
    toast('Tarefa removida.', 'warn');
}

function saveNotice() {
    const title   = document.getElementById('nTitle').value.trim();
    const content = document.getElementById('nContent').value.trim();
    if (!title)   return toast('Título é obrigatório.', 'error');
    if (!content) return toast('Conteúdo é obrigatório.', 'error');
    const notices = DB.notices;
    notices.unshift({
        id: uid(), title, content,
        date: new Date().toISOString().split('T')[0],
        createdAt: new Date().toISOString(),
    });
    DB.notices = notices;
    ['nTitle','nContent'].forEach(id => document.getElementById(id).value = '');
    closeModal('noticeModal');
    renderNotices();
    renderDashboard();
    toast('Comunicado publicado.');
}

function deleteNotice(id) {
    DB.notices = DB.notices.filter(n => n.id !== id);
    renderNotices();
    renderDashboard();
    toast('Comunicado removido.', 'warn');
}

/* ── Export (admin only) ── */
function exportData() {
    const data = {
        students: DB.students, tasks: DB.tasks, notices: DB.notices,
        exportedAt: new Date().toISOString(),
    };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `takstud-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(a.href);
    toast('Dados exportados.');
}

/* ── Init ── */
document.addEventListener('DOMContentLoaded', () => {
    /* Nav */
    document.querySelectorAll('.nav-item').forEach(btn =>
        btn.addEventListener('click', () => showView(btn.dataset.view)));

    /* Role switcher — triggers real RBAC enforcement */
    document.querySelectorAll('.role-btn').forEach(btn =>
        btn.addEventListener('click', () => {
            currentRole = btn.dataset.role;
            document.querySelectorAll('.role-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('currentRole').textContent =
                { teacher: 'Prof. Demo', student: 'Aluno Demo', admin: 'Admin' }[currentRole];
            applyRBAC();
        }));

    /* Export button injected into topbar */
    const exportBtn = document.createElement('button');
    exportBtn.id        = 'exportBtn';
    exportBtn.className = 'btn-export';
    exportBtn.innerHTML = '<i class="fas fa-download"></i> Exportar';
    exportBtn.style.display = 'none';
    exportBtn.addEventListener('click', exportData);
    document.querySelector('.topbar-user').prepend(exportBtn);

    /* Students */
    document.getElementById('openAddStudent').addEventListener('click', () => {
        editingStudentId = null;
        ['sName','sClass','sEmail'].forEach(id => document.getElementById(id).value = '');
        document.getElementById('studentModalTitle').textContent = 'Adicionar Aluno';
        openModal('studentModal');
    });
    document.getElementById('closeStudentModal').addEventListener('click', () => closeModal('studentModal'));
    document.getElementById('saveStudent').addEventListener('click', saveStudent);
    document.getElementById('studentSearch').addEventListener('input', debounce(e => {
        studentSearch = e.target.value;
        renderStudents();
    }));

    /* Event delegation — students table */
    document.getElementById('studentsBody').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        const id = Number(btn.dataset.id);
        if (btn.dataset.action === 'edit-student') openEditStudent(id);
        else if (btn.dataset.action === 'del-student') deleteStudent(id);
    });

    /* Tasks */
    document.getElementById('openAddTask').addEventListener('click', () => openModal('taskModal'));
    document.getElementById('closeTaskModal').addEventListener('click', () => closeModal('taskModal'));
    document.getElementById('saveTask').addEventListener('click', saveTask);
    document.querySelectorAll('.filter-btn').forEach(btn =>
        btn.addEventListener('click', () => {
            taskFilter = btn.dataset.filter;
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            renderTasks();
        }));

    /* Event delegation — tasks list */
    document.getElementById('tasksList').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        const id = Number(btn.dataset.id);
        if (btn.dataset.action === 'toggle-task') toggleTask(id);
        else if (btn.dataset.action === 'del-task') deleteTask(id);
    });

    /* Notices */
    document.getElementById('openAddNotice').addEventListener('click', () => openModal('noticeModal'));
    document.getElementById('closeNoticeModal').addEventListener('click', () => closeModal('noticeModal'));
    document.getElementById('saveNotice').addEventListener('click', saveNotice);

    /* Event delegation — notices list */
    document.getElementById('noticesList').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        if (btn.dataset.action === 'del-notice') deleteNotice(Number(btn.dataset.id));
    });

    /* Backdrop click to close any modal */
    document.querySelectorAll('.modal').forEach(m =>
        m.addEventListener('click', e => { if (e.target === m) m.classList.remove('open'); }));

    /* Keyboard shortcuts */
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') { closeAllModals(); return; }

        if (e.ctrlKey && e.key === 'n') {
            e.preventDefault();
            if (!RBAC[currentRole].canWrite) return;
            const active = document.querySelector('.view.active');
            if (!active) return;
            const map = {
                'view-students': 'studentModal',
                'view-tasks':    'taskModal',
                'view-notices':  'noticeModal',
            };
            if (map[active.id]) openModal(map[active.id]);
        }
    });

    /* Enter to save in modal (skips textarea) */
    ['studentModal', 'taskModal', 'noticeModal'].forEach(modalId => {
        document.getElementById(modalId).addEventListener('keydown', e => {
            if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                ({ studentModal: saveStudent, taskModal: saveTask, noticeModal: saveNotice })[modalId]?.();
            }
        });
    });

    showView('dashboard');
});
