/* ─── Supabase config ───────────────────────────────────────────────────────
 * Replace the two values below with those from your Supabase account.
 * Settings > API > Project URL  and  anon public key.
 * The anon key is public by design — RLS protects data on the server.
 * ─────────────────────────────────────────────────────────────────────────── */
const SUPABASE_URL      = 'https://bvquyfzllqnbfxncsacn.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2cXV5ZnpsbHFuYmZ4bmNzYWNuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgxODU1MzQsImV4cCI6MjA5Mzc2MTUzNH0.xa_rs4bVLoTv58P7U8rDOaPjo1Dqt60q8cR-IWFpbug';

const sb = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

/* ─── RBAC — espelhado no banco via Row Level Security ───────────────────── */
const RBAC = {
    teacher: { views: ['dashboard','students','tasks','notices','schedule'], canWrite: true,  canExport: false },
    student: { views: ['dashboard','tasks','notices'],                       canWrite: false, canExport: false },
    admin:   { views: ['dashboard','students','tasks','notices','schedule'], canWrite: true,  canExport: true  },
};

/* ─── Estado ─────────────────────────────────────────────────────────────── */
const state = {
    profile:          null,   // { id, full_name, role, school_id }
    taskFilter:       'all',
    studentSearch:    '',
    editingStudentId: null,
};

/* ─── Grade horária (dado estático, sem necessidade de banco) ────────────── */
const SCHEDULE = [
    { time:'07:00', mon:'Math',       tue:'Portuguese',  wed:'History',    thu:'Science',    fri:'P.E.'       },
    { time:'08:00', mon:'Portuguese', tue:'Math',        wed:'Science',    thu:'Math',       fri:'Arts'       },
    { time:'09:00', mon:'History',    tue:'Science',     wed:'Math',       thu:'Portuguese', fri:'English'    },
    { time:'10:30', mon:'Science',    tue:'History',     wed:'English',    thu:'History',    fri:'Math'       },
    { time:'11:30', mon:'English',    tue:'P.E.',        wed:'Portuguese', thu:'Arts',       fri:'Portuguese' },
];

/* ─── Utilitários ────────────────────────────────────────────────────────── */
const formatDate  = d => { if (!d) return ''; const [y,m,day] = d.split('-'); return `${day}/${m}/${y}`; };
const debounce    = (fn, ms = 250) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };

/* XSS: toda string do usuário que vai para innerHTML passa por aqui */
const esc = s => String(s ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');

/* ─── Toast ──────────────────────────────────────────────────────────────── */
let toastTimer;
function toast(msg, type = 'success') {
    let el = document.getElementById('ts-toast');
    if (!el) { el = document.createElement('div'); el.id = 'ts-toast'; document.body.appendChild(el); }
    el.textContent = msg;
    el.className   = `ts-toast ts-toast-${type} show`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 2800);
}

/* ─── Auth UI ────────────────────────────────────────────────────────────── */
const authOverlay = () => document.getElementById('authOverlay');
const setAuthErr  = msg => { document.getElementById('authError').textContent = msg; };

function showLoginSection() {
    document.getElementById('loginSection').style.display    = '';
    document.getElementById('registerSection').style.display = 'none';
    setAuthErr('');
}

function showRegisterSection() {
    document.getElementById('loginSection').style.display    = 'none';
    document.getElementById('registerSection').style.display = '';
    setAuthErr('');
}

async function login() {
    const email    = document.getElementById('authEmail').value.trim();
    const password = document.getElementById('authPassword').value;
    if (!email || !password) return setAuthErr('Please fill in email and password.');
    setAuthErr('');
    const btn = document.getElementById('btnLogin');
    btn.disabled = true;
    btn.textContent = 'Signing in...';
    const { error } = await sb.auth.signInWithPassword({ email, password });
    btn.disabled    = false;
    btn.textContent = 'Sign In';
    if (error) setAuthErr(error.message === 'Invalid login credentials'
        ? 'Incorrect email or password.' : error.message);
}

async function register() {
    const name     = document.getElementById('regName').value.trim();
    const school   = document.getElementById('regSchool').value.trim();
    const email    = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    if (!name || !school || !email || !password) return setAuthErr('Please fill in all fields.');
    if (password.length < 6) return setAuthErr('Password must be at least 6 characters.');
    setAuthErr('');
    const btn = document.getElementById('btnRegister');
    btn.disabled = true;
    btn.textContent = 'Creating account...';

    const { data, error } = await sb.auth.signUp({ email, password });
    if (error) {
        btn.disabled = false; btn.textContent = 'Create account';
        return setAuthErr(error.message);
    }

    /* Cria escola + perfil em uma transação via RPC para evitar estado parcial */
    const { error: rpcErr } = await sb.rpc('create_school_and_profile', {
        p_user_id:   data.user.id,
        p_full_name: name,
        p_school_name: school,
    });

    btn.disabled = false; btn.textContent = 'Create account';
    if (rpcErr) return setAuthErr('Account created but profile failed. Try signing in.');
    setAuthErr('Account created! Check your email and sign in.');
    showLoginSection();
}

async function logout() {
    await sb.auth.signOut();
}

/* ─── Auth state machine ─────────────────────────────────────────────────── */
sb.auth.onAuthStateChange(async (event, session) => {
    if (!session) {
        state.profile = null;
        authOverlay().style.display = 'flex';
        showLoginSection();
        return;
    }

    const { data: profile, error } = await sb
        .from('profiles')
        .select('id, full_name, role, school_id')
        .eq('id', session.user.id)
        .single();

    if (error || !profile) {
        await sb.auth.signOut();
        setAuthErr('Profile not found. Confirm your email or contact support.');
        authOverlay().style.display = 'flex';
        return;
    }

    state.profile = profile;
    document.getElementById('currentRole').textContent      = profile.full_name;
    document.getElementById('currentRoleBadge').textContent =
        { teacher: 'Teacher', student: 'Student', admin: 'Admin' }[profile.role] ?? profile.role;

    authOverlay().style.display = 'none';
    applyRBAC();
    subscribeToChanges();
    showView('dashboard');
});

/* ─── Real-time — Supabase Realtime publica mudanças do Postgres ─────────── */
let realtimeChannel = null;
function subscribeToChanges() {
    if (realtimeChannel) sb.removeChannel(realtimeChannel);
    realtimeChannel = sb.channel('db-changes')
        .on('postgres_changes', { event: '*', schema: 'public', table: 'students' }, () => {
            renderStudents();
            renderDashboard();
        })
        .on('postgres_changes', { event: '*', schema: 'public', table: 'tasks' }, () => {
            renderTasks();
            renderDashboard();
        })
        .on('postgres_changes', { event: '*', schema: 'public', table: 'notices' }, () => {
            renderNotices();
            renderDashboard();
        })
        .subscribe();
}

/* ─── RBAC enforcement ───────────────────────────────────────────────────── */
function applyRBAC() {
    const p = RBAC[state.profile.role] ?? RBAC.student;
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.style.display = p.views.includes(btn.dataset.view) ? '' : 'none';
    });
    document.querySelectorAll('.btn-add').forEach(btn => {
        btn.style.display = p.canWrite ? '' : 'none';
    });
    const exportBtn = document.getElementById('exportBtn');
    if (exportBtn) exportBtn.style.display = p.canExport ? '' : 'none';
}

/* ─── Navegação ──────────────────────────────────────────────────────────── */
function showView(view) {
    if (!state.profile) return;
    const p = RBAC[state.profile.role] ?? RBAC.student;
    if (!p.views.includes(view)) return;
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById(`view-${view}`)?.classList.add('active');
    document.querySelector(`[data-view="${view}"]`)?.classList.add('active');
    ({ dashboard: renderDashboard, students: renderStudents, tasks: renderTasks,
       notices: renderNotices, schedule: renderSchedule })[view]?.();
}

/* ─── Camada de dados (todo acesso vai ao Postgres via Supabase REST) ─────── */
async function loadStudents() {
    const { data, error } = await sb.from('students')
        .select('*').eq('school_id', state.profile.school_id).order('name');
    if (error) { toast('Error loading students.', 'error'); return []; }
    return data;
}

async function loadTasks() {
    const { data, error } = await sb.from('tasks')
        .select('*').eq('school_id', state.profile.school_id)
        .order('created_at', { ascending: false });
    if (error) { toast('Error loading tasks.', 'error'); return []; }
    return data;
}

async function loadNotices() {
    const { data, error } = await sb.from('notices')
        .select('*').eq('school_id', state.profile.school_id)
        .order('created_at', { ascending: false });
    if (error) { toast('Error loading notices.', 'error'); return []; }
    return data;
}

/* ─── Renders ────────────────────────────────────────────────────────────── */
async function renderDashboard() {
    const [students, tasks, notices] = await Promise.all([loadStudents(), loadTasks(), loadNotices()]);
    document.getElementById('statStudents').textContent = students.length;
    document.getElementById('statTasks').textContent    = tasks.length;
    document.getElementById('statDone').textContent     = tasks.filter(t => t.done).length;
    document.getElementById('statNotices').textContent  = notices.length;
    document.getElementById('dashPendingTasks').innerHTML =
        tasks.filter(t => !t.done).slice(0, 5)
             .map(t => `<li><i class="fas fa-circle"></i>${esc(t.title)}</li>`).join('')
        || '<li class="empty">No pending tasks.</li>';
    document.getElementById('dashNotices').innerHTML =
        notices.slice(0, 3)
               .map(n => `<li><i class="fas fa-circle"></i>${esc(n.title)}</li>`).join('')
        || '<li class="empty">No notices.</li>';
}

async function renderStudents() {
    const canWrite = RBAC[state.profile.role]?.canWrite ?? false;
    const all      = await loadStudents();
    const filtered = state.studentSearch
        ? all.filter(s => s.name.toLowerCase().includes(state.studentSearch.toLowerCase()))
        : all;
    document.getElementById('studentsBody').innerHTML = filtered.length
        ? filtered.map(s => `<tr>
            <td>${esc(s.name)}</td>
            <td><span class="badge-class">${esc(s.cls)}</span></td>
            <td>${esc(s.email || '—')}</td>
            <td>${canWrite
                ? `<div class="td-actions">
                     <button class="btn-icon-sm edit" data-action="edit-student" data-id="${s.id}" title="Edit"><i class="fas fa-edit"></i></button>
                     <button class="btn-icon-sm"      data-action="del-student"  data-id="${s.id}" title="Delete"><i class="fas fa-trash"></i></button>
                   </div>`
                : '—'}</td>
          </tr>`).join('')
        : `<tr><td colspan="4"><div class="empty-state"><i class="fas fa-users"></i><p>No students found.</p></div></td></tr>`;
}

async function renderTasks() {
    const canWrite = RBAC[state.profile.role]?.canWrite ?? false;
    let tasks = await loadTasks();
    if (state.taskFilter === 'pending') tasks = tasks.filter(t => !t.done);
    if (state.taskFilter === 'done')    tasks = tasks.filter(t =>  t.done);
    document.getElementById('tasksList').innerHTML = tasks.length
        ? tasks.map(t => `<div class="task-item ${t.done ? 'done' : ''}">
            <div class="task-check ${t.done ? 'checked' : ''}"
                 ${canWrite ? `data-action="toggle-task" data-id="${t.id}"` : ''}>
                ${t.done ? '<i class="fas fa-check"></i>' : ''}
            </div>
            <div class="task-body">
                <div class="task-title">${esc(t.title)}</div>
                <div class="task-meta">
                    <span><i class="fas fa-book"></i>${esc(t.subject || '')}</span>
                    ${t.due_date ? `<span><i class="fas fa-calendar"></i>${formatDate(t.due_date)}</span>` : ''}
                </div>
                ${t.description ? `<div class="task-desc">${esc(t.description)}</div>` : ''}
            </div>
            ${canWrite ? `<button class="task-del" data-action="del-task" data-id="${t.id}"><i class="fas fa-times"></i></button>` : ''}
          </div>`).join('')
        : '<div class="empty-state"><i class="fas fa-tasks"></i><p>No tasks.</p></div>';
}

async function renderNotices() {
    const canWrite = RBAC[state.profile.role]?.canWrite ?? false;
    const notices  = await loadNotices();
    document.getElementById('noticesList').innerHTML = notices.length
        ? notices.map(n => `<div class="notice-item">
            <div class="notice-header">
                <span class="notice-title">${esc(n.title)}</span>
                <div style="display:flex;align-items:center;gap:.7rem">
                    <span class="notice-date">${n.created_at.split('T')[0]}</span>
                    ${canWrite ? `<button class="btn-icon-sm" data-action="del-notice" data-id="${n.id}"><i class="fas fa-trash"></i></button>` : ''}
                </div>
            </div>
            <div class="notice-content">${esc(n.content)}</div>
          </div>`).join('')
        : '<div class="empty-state"><i class="fas fa-bullhorn"></i><p>No notices.</p></div>';
}

function renderSchedule() {
    const days = ['Monday','Tuesday','Wednesday','Thursday','Friday'], keys = ['mon','tue','wed','thu','fri'];
    let html = '<div class="sch-header">Hora</div>';
    days.forEach(d => html += `<div class="sch-header">${d}</div>`);
    SCHEDULE.forEach(row => {
        html += `<div class="sch-time">${row.time}</div>`;
        keys.forEach(k => html += `<div class="sch-cell"><span class="sch-subject">${row[k]}</span></div>`);
    });
    document.getElementById('scheduleGrid').innerHTML = html;
}

/* ─── Modais ─────────────────────────────────────────────────────────────── */
const openModal      = id => document.getElementById(id).classList.add('open');
const closeModal     = id => document.getElementById(id).classList.remove('open');
const closeAllModals = () => document.querySelectorAll('.modal.open').forEach(m => m.classList.remove('open'));

async function openEditStudent(id) {
    const { data: s } = await sb.from('students').select('*').eq('id', id).single();
    if (!s) return;
    state.editingStudentId = id;
    document.getElementById('sName').value  = s.name;
    document.getElementById('sClass').value = s.cls;
    document.getElementById('sEmail').value = s.email || '';
    document.getElementById('studentModalTitle').textContent = 'Edit Student';
    openModal('studentModal');
}

/* ─── CRUD (escrita rejeitada pelo RLS se o role não autorizar) ───────────── */
async function saveStudent() {
    const name  = document.getElementById('sName').value.trim();
    const cls   = document.getElementById('sClass').value.trim();
    const email = document.getElementById('sEmail').value.trim();
    if (!name) return toast('Name is required.', 'error');
    if (!cls)  return toast('Class is required.', 'error');

    if (state.editingStudentId) {
        const { error } = await sb.from('students')
            .update({ name, cls, email }).eq('id', state.editingStudentId);
        if (error) return toast('Error updating: ' + error.message, 'error');
        toast('Student updated.');
    } else {
        const { error } = await sb.from('students')
            .insert({ name, cls, email, school_id: state.profile.school_id });
        if (error) return toast('Error saving: ' + error.message, 'error');
        toast('Student added.');
    }
    closeModal('studentModal');
    renderStudents();
    renderDashboard();
}

async function deleteStudent(id) {
    if (!confirm('Delete this student?')) return;
    const { error } = await sb.from('students').delete().eq('id', id);
    if (error) return toast('Error deleting.', 'error');
    renderStudents();
    renderDashboard();
    toast('Student removed.', 'warn');
}

async function saveTask() {
    const title   = document.getElementById('tTitle').value.trim();
    const subject = document.getElementById('tSubject').value.trim();
    if (!title) return toast('Title is required.', 'error');
    const { error } = await sb.from('tasks').insert({
        title, subject,
        due_date:    document.getElementById('tDue').value || null,
        description: document.getElementById('tDesc').value.trim(),
        done:        false,
        school_id:   state.profile.school_id,
    });
    if (error) return toast('Error saving task.', 'error');
    ['tTitle','tSubject','tDue','tDesc'].forEach(id => document.getElementById(id).value = '');
    closeModal('taskModal');
    renderTasks();
    renderDashboard();
    toast('Task added.');
}

async function toggleTask(id) {
    const { data: t } = await sb.from('tasks').select('done').eq('id', id).single();
    if (!t) return;
    await sb.from('tasks').update({ done: !t.done }).eq('id', id);
    renderTasks();
    renderDashboard();
}

async function deleteTask(id) {
    await sb.from('tasks').delete().eq('id', id);
    renderTasks();
    renderDashboard();
    toast('Task removed.', 'warn');
}

async function saveNotice() {
    const title   = document.getElementById('nTitle').value.trim();
    const content = document.getElementById('nContent').value.trim();
    if (!title)   return toast('Title is required.', 'error');
    if (!content) return toast('Content is required.', 'error');
    const { error } = await sb.from('notices').insert({
        title, content, school_id: state.profile.school_id,
    });
    if (error) return toast('Error publishing.', 'error');
    ['nTitle','nContent'].forEach(id => document.getElementById(id).value = '');
    closeModal('noticeModal');
    renderNotices();
    renderDashboard();
    toast('Notice published.');
}

async function deleteNotice(id) {
    await sb.from('notices').delete().eq('id', id);
    renderNotices();
    renderDashboard();
    toast('Notice removed.', 'warn');
}

/* ─── Export JSON (só admin) ────────────────────────────────────────────── */
async function exportData() {
    const [students, tasks, notices] = await Promise.all([loadStudents(), loadTasks(), loadNotices()]);
    const payload = { students, tasks, notices, school_id: state.profile.school_id, exportedAt: new Date().toISOString() };
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const a    = document.createElement('a');
    a.href     = URL.createObjectURL(blob);
    a.download = `takstud-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(a.href);
    toast('Data exported.');
}

/* ─── Init ───────────────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    /* Auth buttons */
    document.getElementById('btnLogin').addEventListener('click', login);
    document.getElementById('btnRegister').addEventListener('click', register);
    document.getElementById('btnShowRegister').addEventListener('click', showRegisterSection);
    document.getElementById('btnShowLogin').addEventListener('click', showLoginSection);
    document.getElementById('btnLogout').addEventListener('click', logout);

    /* Enter no formulário de login */
    ['authEmail','authPassword'].forEach(id =>
        document.getElementById(id).addEventListener('keydown', e => { if (e.key === 'Enter') login(); }));

    /* Export button (injetado no topbar, visível só para admin) */
    const exportBtn = document.createElement('button');
    exportBtn.id        = 'exportBtn';
    exportBtn.className = 'btn-export';
    exportBtn.innerHTML = '<i class="fas fa-download"></i> Export';
    exportBtn.style.display = 'none';
    exportBtn.addEventListener('click', exportData);
    document.querySelector('.topbar-user').prepend(exportBtn);

    /* Nav */
    document.querySelectorAll('.nav-item').forEach(btn =>
        btn.addEventListener('click', () => showView(btn.dataset.view)));

    /* Students */
    document.getElementById('openAddStudent').addEventListener('click', () => {
        state.editingStudentId = null;
        ['sName','sClass','sEmail'].forEach(id => document.getElementById(id).value = '');
        document.getElementById('studentModalTitle').textContent = 'Add Student';
        openModal('studentModal');
    });
    document.getElementById('closeStudentModal').addEventListener('click', () => closeModal('studentModal'));
    document.getElementById('saveStudent').addEventListener('click', saveStudent);
    document.getElementById('studentSearch').addEventListener('input', debounce(e => {
        state.studentSearch = e.target.value;
        renderStudents();
    }));
    document.getElementById('studentsBody').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        if (btn.dataset.action === 'edit-student') openEditStudent(btn.dataset.id);
        else if (btn.dataset.action === 'del-student') deleteStudent(btn.dataset.id);
    });

    /* Tasks */
    document.getElementById('openAddTask').addEventListener('click', () => openModal('taskModal'));
    document.getElementById('closeTaskModal').addEventListener('click', () => closeModal('taskModal'));
    document.getElementById('saveTask').addEventListener('click', saveTask);
    document.querySelectorAll('.filter-btn').forEach(btn =>
        btn.addEventListener('click', () => {
            state.taskFilter = btn.dataset.filter;
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            renderTasks();
        }));
    document.getElementById('tasksList').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        if (btn.dataset.action === 'toggle-task') toggleTask(btn.dataset.id);
        else if (btn.dataset.action === 'del-task') deleteTask(btn.dataset.id);
    });

    /* Notices */
    document.getElementById('openAddNotice').addEventListener('click', () => openModal('noticeModal'));
    document.getElementById('closeNoticeModal').addEventListener('click', () => closeModal('noticeModal'));
    document.getElementById('saveNotice').addEventListener('click', saveNotice);
    document.getElementById('noticesList').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        if (btn.dataset.action === 'del-notice') deleteNotice(btn.dataset.id);
    });

    /* Backdrop click fecha qualquer modal */
    document.querySelectorAll('.modal').forEach(m =>
        m.addEventListener('click', e => { if (e.target === m) m.classList.remove('open'); }));

    /* Atalhos de teclado */
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') { closeAllModals(); return; }
        if (e.ctrlKey && e.key === 'n') {
            e.preventDefault();
            if (!state.profile || !RBAC[state.profile.role]?.canWrite) return;
            const active = document.querySelector('.view.active');
            if (!active) return;
            const map = { 'view-students':'studentModal', 'view-tasks':'taskModal', 'view-notices':'noticeModal' };
            if (map[active.id]) openModal(map[active.id]);
        }
    });

    /* Enter para salvar dentro de modal (exceto textarea) */
    ['studentModal','taskModal','noticeModal'].forEach(modalId => {
        document.getElementById(modalId).addEventListener('keydown', e => {
            if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                ({ studentModal: saveStudent, taskModal: saveTask, noticeModal: saveNotice })[modalId]?.();
            }
        });
    });
});
