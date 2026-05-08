/* ─── Configuração do Supabase ───────────────────────────────────────────────
 * Substitua os dois valores abaixo pelos da sua conta Supabase.
 * Configurações > API > URL do projeto  e  chave pública anon.
 * A chave anon é pública por design — o RLS protege os dados no servidor.
 * ─────────────────────────────────────────────────────────────────────────── */
const SUPABASE_URL      = 'https://bvquyfzllqnbfxncsacn.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ2cXV5ZnpsbHFuYmZ4bmNzYWNuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgxODU1MzQsImV4cCI6MjA5Mzc2MTUzNH0.xa_rs4bVLoTv58P7U8rDOaPjo1Dqt60q8cR-IWFpbug';

const sb = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

/* ─── RBAC — espelhado no banco via Row Level Security ──────────────────── */
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

/* ─── Horários (dados estáticos, sem necessidade de banco) ──────────────── */
const SCHEDULE = [
    { time:'07:00', mon:'Matemática', tue:'Português',   wed:'História',   thu:'Ciências',   fri:'Ed. Física'  },
    { time:'08:00', mon:'Português',  tue:'Matemática',  wed:'Ciências',   thu:'Matemática', fri:'Artes'       },
    { time:'09:00', mon:'História',   tue:'Ciências',    wed:'Matemática', thu:'Português',  fri:'Inglês'      },
    { time:'10:30', mon:'Ciências',   tue:'História',    wed:'Inglês',     thu:'História',   fri:'Matemática'  },
    { time:'11:30', mon:'Inglês',     tue:'Ed. Física',  wed:'Português',  thu:'Artes',      fri:'Português'   },
];

/* ─── Utilitários ────────────────────────────────────────────────────────── */
const formatDate  = d => { if (!d) return ''; const [y,m,day] = d.split('-'); return `${day}/${m}/${y}`; };
const debounce    = (fn, ms = 250) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };

/* XSS: toda string do usuário escrita em innerHTML passa por aqui */
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

/* ─── UI de Autenticação ─────────────────────────────────────────────────── */
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
    if (!email || !password) return setAuthErr('Preencha o e-mail e a senha.');
    setAuthErr('');
    const btn = document.getElementById('btnLogin');
    btn.disabled = true;
    btn.textContent = 'Entrando...';
    const { error } = await sb.auth.signInWithPassword({ email, password });
    btn.disabled    = false;
    btn.textContent = 'Entrar';
    if (error) setAuthErr(error.message === 'Invalid login credentials'
        ? 'E-mail ou senha incorretos.' : error.message);
}

async function register() {
    const name     = document.getElementById('regName').value.trim();
    const school   = document.getElementById('regSchool').value.trim();
    const email    = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    if (!name || !school || !email || !password) return setAuthErr('Preencha todos os campos.');
    if (password.length < 6) return setAuthErr('A senha deve ter no mínimo 6 caracteres.');
    setAuthErr('');
    const btn = document.getElementById('btnRegister');
    btn.disabled = true;
    btn.textContent = 'Criando conta...';

    const { data, error } = await sb.auth.signUp({ email, password });
    if (error) {
        btn.disabled = false; btn.textContent = 'Criar conta';
        return setAuthErr(error.message);
    }

    /* Cria escola + perfil em uma única transação RPC para evitar estado parcial */
    const { error: rpcErr } = await sb.rpc('create_school_and_profile', {
        p_user_id:   data.user.id,
        p_full_name: name,
        p_school_name: school,
    });

    btn.disabled = false; btn.textContent = 'Criar conta';
    if (rpcErr) return setAuthErr('Conta criada, mas perfil falhou. Tente entrar.');
    setAuthErr('Conta criada! Confirme seu e-mail e entre.');
    showLoginSection();
}

async function logout() {
    await sb.auth.signOut();
}

/* ─── Máquina de estados de autenticação ─────────────────────────────────── */
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
        setAuthErr('Perfil não encontrado. Confirme seu e-mail.');
        authOverlay().style.display = 'flex';
        return;
    }

    state.profile = profile;
    document.getElementById('currentRole').textContent      = profile.full_name;
    document.getElementById('currentRoleBadge').textContent =
        { teacher: 'Professor', student: 'Aluno', admin: 'Admin' }[profile.role] ?? profile.role;

    authOverlay().style.display = 'none';
    applyRBAC();
    subscribeToChanges();
    showView('dashboard');
});

/* ─── Realtime — Supabase Realtime publica alterações do Postgres ────────── */
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

/* ─── Aplicação do RBAC ──────────────────────────────────────────────────── */
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
    if (error) { toast('Erro ao carregar alunos.', 'error'); return []; }
    return data;
}

async function loadTasks() {
    const { data, error } = await sb.from('tasks')
        .select('*').eq('school_id', state.profile.school_id)
        .order('created_at', { ascending: false });
    if (error) { toast('Erro ao carregar tarefas.', 'error'); return []; }
    return data;
}

async function loadNotices() {
    const { data, error } = await sb.from('notices')
        .select('*').eq('school_id', state.profile.school_id)
        .order('created_at', { ascending: false });
    if (error) { toast('Erro ao carregar avisos.', 'error'); return []; }
    return data;
}

/* ─── Renderizações ──────────────────────────────────────────────────────── */
async function renderDashboard() {
    const [students, tasks, notices] = await Promise.all([loadStudents(), loadTasks(), loadNotices()]);
    document.getElementById('statStudents').textContent = students.length;
    document.getElementById('statTasks').textContent    = tasks.length;
    document.getElementById('statDone').textContent     = tasks.filter(t => t.done).length;
    document.getElementById('statNotices').textContent  = notices.length;
    document.getElementById('dashPendingTasks').innerHTML =
        tasks.filter(t => !t.done).slice(0, 5)
             .map(t => `<li><i class="fas fa-circle"></i>${esc(t.title)}</li>`).join('')
        || '<li class="empty">Nenhuma tarefa pendente.</li>';
    document.getElementById('dashNotices').innerHTML =
        notices.slice(0, 3)
               .map(n => `<li><i class="fas fa-circle"></i>${esc(n.title)}</li>`).join('')
        || '<li class="empty">Nenhum aviso.</li>';
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
                     <button class="btn-icon-sm edit" data-action="edit-student" data-id="${s.id}" title="Editar"><i class="fas fa-edit"></i></button>
                     <button class="btn-icon-sm"      data-action="del-student"  data-id="${s.id}" title="Excluir"><i class="fas fa-trash"></i></button>
                   </div>`
                : '—'}</td>
          </tr>`).join('')
        : `<tr><td colspan="4"><div class="empty-state"><i class="fas fa-users"></i><p>Nenhum aluno encontrado.</p></div></td></tr>`;
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
        : '<div class="empty-state"><i class="fas fa-tasks"></i><p>Nenhuma tarefa.</p></div>';
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
        : '<div class="empty-state"><i class="fas fa-bullhorn"></i><p>Nenhum aviso.</p></div>';
}

function renderSchedule() {
    const days = ['Segunda','Terça','Quarta','Quinta','Sexta'], keys = ['mon','tue','wed','thu','fri'];
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
    document.getElementById('studentModalTitle').textContent = 'Editar Aluno';
    openModal('studentModal');
}

/* ─── CRUD (escritas rejeitadas pelo RLS se o perfil não tiver permissão) ─── */
async function saveStudent() {
    const name  = document.getElementById('sName').value.trim();
    const cls   = document.getElementById('sClass').value.trim();
    const email = document.getElementById('sEmail').value.trim();
    if (!name) return toast('Nome é obrigatório.', 'error');
    if (!cls)  return toast('Turma é obrigatória.', 'error');

    if (state.editingStudentId) {
        const { error } = await sb.from('students')
            .update({ name, cls, email }).eq('id', state.editingStudentId);
        if (error) return toast('Erro ao atualizar: ' + error.message, 'error');
        toast('Aluno atualizado.');
    } else {
        const { error } = await sb.from('students')
            .insert({ name, cls, email, school_id: state.profile.school_id });
        if (error) return toast('Erro ao salvar: ' + error.message, 'error');
        toast('Aluno adicionado.');
    }
    closeModal('studentModal');
    renderStudents();
    renderDashboard();
}

async function deleteStudent(id) {
    if (!confirm('Excluir este aluno?')) return;
    const { error } = await sb.from('students').delete().eq('id', id);
    if (error) return toast('Erro ao excluir.', 'error');
    renderStudents();
    renderDashboard();
    toast('Aluno removido.', 'warn');
}

async function saveTask() {
    const title   = document.getElementById('tTitle').value.trim();
    const subject = document.getElementById('tSubject').value.trim();
    if (!title) return toast('Título é obrigatório.', 'error');
    const { error } = await sb.from('tasks').insert({
        title, subject,
        due_date:    document.getElementById('tDue').value || null,
        description: document.getElementById('tDesc').value.trim(),
        done:        false,
        school_id:   state.profile.school_id,
    });
    if (error) return toast('Erro ao salvar tarefa.', 'error');
    ['tTitle','tSubject','tDue','tDesc'].forEach(id => document.getElementById(id).value = '');
    closeModal('taskModal');
    renderTasks();
    renderDashboard();
    toast('Tarefa adicionada.');
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
    toast('Tarefa removida.', 'warn');
}

async function saveNotice() {
    const title   = document.getElementById('nTitle').value.trim();
    const content = document.getElementById('nContent').value.trim();
    if (!title)   return toast('Título é obrigatório.', 'error');
    if (!content) return toast('Conteúdo é obrigatório.', 'error');
    const { error } = await sb.from('notices').insert({
        title, content, school_id: state.profile.school_id,
    });
    if (error) return toast('Erro ao publicar.', 'error');
    ['nTitle','nContent'].forEach(id => document.getElementById(id).value = '');
    closeModal('noticeModal');
    renderNotices();
    renderDashboard();
    toast('Aviso publicado.');
}

async function deleteNotice(id) {
    await sb.from('notices').delete().eq('id', id);
    renderNotices();
    renderDashboard();
    toast('Aviso removido.', 'warn');
}

/* ─── Exportar JSON (somente admin) ─────────────────────────────────────── */
async function exportData() {
    const [students, tasks, notices] = await Promise.all([loadStudents(), loadTasks(), loadNotices()]);
    const payload = { students, tasks, notices, school_id: state.profile.school_id, exportedAt: new Date().toISOString() };
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const a    = document.createElement('a');
    a.href     = URL.createObjectURL(blob);
    a.download = `takstud-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(a.href);
    toast('Dados exportados.');
}

/* ─── Inicialização ──────────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    /* Botões de autenticação */
    document.getElementById('btnLogin').addEventListener('click', login);
    document.getElementById('btnRegister').addEventListener('click', register);
    document.getElementById('btnShowRegister').addEventListener('click', showRegisterSection);
    document.getElementById('btnShowLogin').addEventListener('click', showLoginSection);
    document.getElementById('btnLogout').addEventListener('click', logout);

    /* Tecla Enter no formulário de login */
    ['authEmail','authPassword'].forEach(id =>
        document.getElementById(id).addEventListener('keydown', e => { if (e.key === 'Enter') login(); }));

    /* Botão de exportar (inserido na topbar, visível apenas para admin) */
    const exportBtn = document.createElement('button');
    exportBtn.id        = 'exportBtn';
    exportBtn.className = 'btn-export';
    exportBtn.innerHTML = '<i class="fas fa-download"></i> Exportar';
    exportBtn.style.display = 'none';
    exportBtn.addEventListener('click', exportData);
    document.querySelector('.topbar-user').prepend(exportBtn);

    /* Navegação */
    document.querySelectorAll('.nav-item').forEach(btn =>
        btn.addEventListener('click', () => showView(btn.dataset.view)));

    /* Alunos */
    document.getElementById('openAddStudent').addEventListener('click', () => {
        state.editingStudentId = null;
        ['sName','sClass','sEmail'].forEach(id => document.getElementById(id).value = '');
        document.getElementById('studentModalTitle').textContent = 'Adicionar Aluno';
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

    /* Tarefas */
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

    /* Avisos */
    document.getElementById('openAddNotice').addEventListener('click', () => openModal('noticeModal'));
    document.getElementById('closeNoticeModal').addEventListener('click', () => closeModal('noticeModal'));
    document.getElementById('saveNotice').addEventListener('click', saveNotice);
    document.getElementById('noticesList').addEventListener('click', e => {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        if (btn.dataset.action === 'del-notice') deleteNotice(btn.dataset.id);
    });

    /* Clique no backdrop fecha qualquer modal */
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
