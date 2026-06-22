/* Firebase config is initialized by firebase-config.js. Firebase Auth and Firestore
 * protect data with Firebase Security Rules.
 */
import { initializeApp } from 'https://www.gstatic.com/firebasejs/12.15.0/firebase-app.js';
import {
    getAuth,
    createUserWithEmailAndPassword,
    signInWithEmailAndPassword,
    sendPasswordResetEmail,
    confirmPasswordReset,
    updatePassword as updateFirebasePassword,
    signOut as firebaseSignOut,
    onAuthStateChanged,
} from 'https://www.gstatic.com/firebasejs/12.15.0/firebase-auth.js';
import {
    getFirestore,
    addDoc,
    collection,
    deleteDoc,
    doc,
    getDoc,
    getDocs,
    limit as firestoreLimit,
    onSnapshot,
    orderBy as firestoreOrderBy,
    query,
    serverTimestamp,
    setDoc,
    updateDoc,
    where,
    writeBatch,
} from 'https://www.gstatic.com/firebasejs/12.15.0/firebase-firestore.js';
import { firebaseConfig } from './firebase-config.js';

const firebaseApp = initializeApp(firebaseConfig);
const firebaseAuth = getAuth(firebaseApp);
const firestore = getFirestore(firebaseApp);
const passwordResetCode = new URLSearchParams(window.location.search).get('oobCode');

const TABLE_COLLECTIONS = {
    takstud_schools: 'takstud_schools',
    takstud_profiles: 'takstud_profiles',
    takstud_students: 'takstud_students',
    takstud_tasks: 'takstud_tasks',
    takstud_notices: 'takstud_notices',
    takstud_schedules: 'takstud_schedules',
    schools: 'takstud_schools',
    profiles: 'takstud_profiles',
    students: 'takstud_students',
    tasks: 'takstud_tasks',
    notices: 'takstud_notices',
    schedules: 'takstud_schedules',
};

function normalizeFirestoreValue(value) {
    if (value && typeof value.toDate === 'function') return value.toDate().toISOString();
    if (Array.isArray(value)) return value.map(normalizeFirestoreValue);
    if (value && typeof value === 'object') {
        return Object.fromEntries(Object.entries(value).map(([key, child]) => [key, normalizeFirestoreValue(child)]));
    }
    return value;
}

function snapshotToRecord(snapshot) {
    return { id: snapshot.id, ...normalizeFirestoreValue(snapshot.data()) };
}

function byField(field, ascending = true) {
    return (a, b) => {
        const av = a[field] ?? '';
        const bv = b[field] ?? '';
        const result = String(av).localeCompare(String(bv), undefined, { numeric: true });
        return ascending ? result : -result;
    };
}

class FirebaseQuery {
    constructor(table) {
        this.collectionName = TABLE_COLLECTIONS[table] || table;
        this.filters = [];
        this.orders = [];
        this.mode = 'select';
        this.payload = null;
        this.expectSingle = false;
        this.limitCount = null;
    }

    select() {
        this.mode = 'select';
        return this;
    }

    eq(field, value) {
        this.filters.push({ field, value });
        return this;
    }

    order(field, options = {}) {
        this.orders.push({ field, ascending: options.ascending !== false });
        return this;
    }

    limit(count) {
        this.limitCount = count;
        return this;
    }

    single() {
        this.expectSingle = true;
        return this.execute();
    }

    insert(payload) {
        this.mode = 'insert';
        this.payload = Array.isArray(payload) ? payload[0] : payload;
        return this.execute();
    }

    update(payload) {
        this.mode = 'update';
        this.payload = payload;
        return this;
    }

    delete() {
        this.mode = 'delete';
        return this;
    }

    then(resolve, reject) {
        return this.execute().then(resolve, reject);
    }

    async execute() {
        try {
            if (this.mode === 'insert') {
                const ref = await addDoc(collection(firestore, this.collectionName), {
                    ...this.payload,
                    created_at: this.payload.created_at || serverTimestamp(),
                });
                return { data: [{ id: ref.id, ...this.payload }], error: null };
            }

            if (this.mode === 'update' || this.mode === 'delete') {
                const idFilter = this.filters.find(filter => filter.field === 'id');
                if (!idFilter) throw new Error('Atualizacao/exclusao requer filtro por id.');
                const ref = doc(firestore, this.collectionName, idFilter.value);
                if (this.mode === 'delete') {
                    await deleteDoc(ref);
                    return { data: null, error: null };
                }
                await updateDoc(ref, this.payload);
                return { data: null, error: null };
            }

            let constraints = this.filters
                .filter(filter => filter.field !== 'id')
                .map(filter => where(filter.field, '==', filter.value));
            this.orders.forEach(order => {
                constraints.push(firestoreOrderBy(order.field, order.ascending ? 'asc' : 'desc'));
            });
            if (this.limitCount) constraints.push(firestoreLimit(this.limitCount));
            let snapshots;
            const idFilter = this.filters.find(filter => filter.field === 'id');
            if (idFilter) {
                const snapshot = await getDoc(doc(firestore, this.collectionName, idFilter.value));
                snapshots = snapshot.exists() ? [snapshot] : [];
            } else {
                snapshots = (await getDocs(query(collection(firestore, this.collectionName), ...constraints))).docs;
            }

            let data = snapshots.map(snapshotToRecord);
            for (const order of [...this.orders].reverse()) {
                data = data.sort(byField(order.field, order.ascending));
            }
            if (this.limitCount) data = data.slice(0, this.limitCount);

            return { data: this.expectSingle ? (data[0] || null) : data, error: null };
        } catch (error) {
            return { data: this.expectSingle ? null : [], error };
        }
    }
}

const sb = {
    auth: {
        async signInWithPassword({ email, password }) {
            try {
                const credential = await signInWithEmailAndPassword(firebaseAuth, email, password);
                return { data: { user: credential.user }, error: null };
            } catch (error) {
                return { data: null, error };
            }
        },
        async signUp({ email, password }) {
            try {
                const credential = await createUserWithEmailAndPassword(firebaseAuth, email, password);
                return { data: { user: credential.user }, error: null };
            } catch (error) {
                return { data: null, error };
            }
        },
        async resetPasswordForEmail(email) {
            try {
                await sendPasswordResetEmail(firebaseAuth, email, { url: window.location.href.split('?')[0] });
                return { error: null };
            } catch (error) {
                return { error };
            }
        },
        async updateUser({ password }) {
            try {
                if (passwordResetCode) {
                    await confirmPasswordReset(firebaseAuth, passwordResetCode, password);
                    window.history.replaceState({}, document.title, window.location.pathname);
                } else if (firebaseAuth.currentUser) {
                    await updateFirebasePassword(firebaseAuth.currentUser, password);
                } else {
                    throw new Error('Link de recuperacao invalido ou expirado.');
                }
                return { error: null };
            } catch (error) {
                return { error };
            }
        },
        async signOut() {
            await firebaseSignOut(firebaseAuth);
        },
        onAuthStateChange(callback) {
            if (passwordResetCode) setTimeout(() => callback('PASSWORD_RECOVERY', null), 0);
            return onAuthStateChanged(firebaseAuth, user => {
                callback(user ? 'SIGNED_IN' : 'SIGNED_OUT', user ? { user } : null);
            });
        },
    },
    from(table) {
        return new FirebaseQuery(table);
    },
    async rpc(name, params) {
        try {
            if (name !== 'takstud_create_school_and_profile') {
                throw new Error(`RPC Firebase nao implementada: ${name}`);
            }

            const schoolRef = doc(collection(firestore, 'takstud_schools'));
            const profileRef = doc(firestore, 'takstud_profiles', params.p_user_id);
            const batch = writeBatch(firestore);
            batch.set(schoolRef, {
                name: params.p_school_name,
                owner_id: params.p_user_id,
                created_at: serverTimestamp(),
            });
            batch.set(profileRef, {
                full_name: params.p_full_name,
                role: 'admin',
                school_id: schoolRef.id,
                created_at: serverTimestamp(),
            });
            await batch.commit();
            return { data: null, error: null };
        } catch (error) {
            return { data: null, error };
        }
    },
    channel() {
        const listeners = [];
        return {
            on(_event, options, callback) {
                listeners.push({ collectionName: TABLE_COLLECTIONS[options.table] || options.table, callback });
                return this;
            },
            subscribe() {
                this.unsubscribers = listeners.map(({ collectionName, callback }) => {
                    if (!state.profile?.school_id) return () => {};
                    const q = query(collection(firestore, collectionName), where('school_id', '==', state.profile.school_id));
                    return onSnapshot(q, () => callback());
                });
                return this;
            },
            unsubscribe() {
                (this.unsubscribers || []).forEach(unsubscribe => unsubscribe());
            },
        };
    },
    removeChannel(channel) {
        channel?.unsubscribe?.();
    },
};
/** @constant {Object} ROLES - Role name constants to avoid magic strings throughout the codebase */
const ROLES = { ADMIN: 'admin', TEACHER: 'teacher', STUDENT: 'student' };


/* ─── RBAC — espelhado no servidor via Firebase Security Rules ──────────── */
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
    document.getElementById('forgotSection').style.display   = 'none';
    document.getElementById('resetSection').style.display    = 'none';
    setAuthErr('');
}

function showRegisterSection() {
    document.getElementById('loginSection').style.display    = 'none';
    document.getElementById('registerSection').style.display = '';
    document.getElementById('forgotSection').style.display   = 'none';
    document.getElementById('resetSection').style.display    = 'none';
    setAuthErr('');
}

function showForgotSection() {
    document.getElementById('loginSection').style.display    = 'none';
    document.getElementById('registerSection').style.display = 'none';
    document.getElementById('forgotSection').style.display   = '';
    document.getElementById('resetSection').style.display    = 'none';
    setAuthErr('');
}

function showResetSection() {
    document.getElementById('loginSection').style.display    = 'none';
    document.getElementById('registerSection').style.display = 'none';
    document.getElementById('forgotSection').style.display   = 'none';
    document.getElementById('resetSection').style.display    = '';
    setAuthErr('');
}


/**
 * Centralizes Firebase/JS error handling: shows user-facing toast + logs to console.
 * @param {Error|Object} err - Error from Firebase adapter or caught exception.
 * @param {string} [context='Operação'] - Label for the failed operation.
 */
function handleError(err, context = 'Operação') {
  const msg = err?.message || String(err) || 'Erro inesperado';
  console.error('[handleError]', context, err);
  toast(msg, 'error');
}


/**
 * Returns true only if every provided string is non-empty after trimming.
 * @param {...string} values
 * @returns {boolean}
 */
function validateRequired(...values) {
  return values.every(v => typeof v === 'string' && v.trim().length > 0);
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
    const { error: rpcErr } = await sb.rpc('takstud_create_school_and_profile', {
        p_user_id:   data.user.id,
        p_full_name: name,
        p_school_name: school,
    });

    btn.disabled = false; btn.textContent = 'Criar conta';
    if (rpcErr) return setAuthErr('Conta criada, mas perfil falhou. Tente entrar.');
    setAuthErr('Conta criada! Confirme seu e-mail e entre.');
    showLoginSection();
}

async function forgotPassword() {
    const email = document.getElementById('forgotEmail').value.trim();
    if (!email) return setAuthErr('Digite seu e-mail.');
    const btn = document.getElementById('btnForgot');
    btn.disabled = true; btn.textContent = 'Enviando...';
    const { error } = await sb.auth.resetPasswordForEmail(email, {
        redirectTo: window.location.href,
    });
    btn.disabled = false; btn.textContent = 'Enviar link';
    if (error) {
        setAuthErr(error.message);
    } else {
        const el = document.getElementById('authError');
        el.style.color = '#2ea043';
        el.textContent = 'Link enviado! Verifique seu e-mail.';
    }
}

async function updatePassword() {
    const pw1 = document.getElementById('newPassword').value;
    const pw2 = document.getElementById('confirmPassword').value;
    if (!pw1 || pw1.length < 6) return setAuthErr('A senha deve ter pelo menos 6 caracteres.');
    if (pw1 !== pw2) return setAuthErr('As senhas não coincidem.');
    const btn = document.getElementById('btnResetPassword');
    btn.disabled = true; btn.textContent = 'Salvando...';
    const { error } = await sb.auth.updateUser({ password: pw1 });
    btn.disabled = false; btn.textContent = 'Salvar nova senha';
    if (error) {
        setAuthErr(error.message);
    } else {
        const el = document.getElementById('authError');
        el.style.color = '#2ea043';
        el.textContent = 'Senha alterada! Fazendo login...';
        setTimeout(showLoginSection, 1800);
    }
}

function printView() {
    window.print();
}

async function logout() {
    await sb.auth.signOut();
}

/* ─── Máquina de estados de autenticação ─────────────────────────────────── */
sb.auth.onAuthStateChange(async (event, session) => {
    if (event === 'PASSWORD_RECOVERY') {
        authOverlay().style.display = 'flex';
        showResetSection();
        return;
    }

    if (!session) {
        state.profile = null;
        authOverlay().style.display = 'flex';
        showLoginSection();
        return;
    }

    const { data: profile, error } = await sb
        .from('takstud_profiles')
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

/* ─── Realtime — Firestore publica alterações das coleções ──────────────── */
let realtimeChannel = null;
function subscribeToChanges() {
    if (realtimeChannel) sb.removeChannel(realtimeChannel);
    realtimeChannel = sb.channel('db-changes')
        .on('firestore_changes', { table: 'students' }, () => {
            renderStudents();
            renderDashboard();
        })
        .on('firestore_changes', { table: 'tasks' }, () => {
            renderTasks();
            renderDashboard();
        })
        .on('firestore_changes', { table: 'notices' }, () => {
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

/* ─── Camada de dados (todo acesso vai ao Firebase/Firestore) ───────────── */
async function loadStudents() {
    const { data, error } = await sb.from('takstud_students')
        .select('*').eq('school_id', state.profile.school_id).order('name');
    if (error) { toast('Erro ao carregar alunos.', 'error'); return []; }
    return data;
}

async function loadTasks() {
    const { data, error } = await sb.from('takstud_tasks')
        .select('*').eq('school_id', state.profile.school_id)
        .order('created_at', { ascending: false });
    if (error) { toast('Erro ao carregar tarefas.', 'error'); return []; }
    return data;
}

async function loadNotices() {
    const { data, error } = await sb.from('takstud_notices')
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

async function renderSchedule() {
    const canEdit = state.profile?.role === 'admin';
    const grid = document.getElementById('scheduleGrid');
    grid.innerHTML = '<div style="grid-column:1/-1;text-align:center;padding:2rem;color:#8b949e">Carregando...</div>';

    const { data: rows } = await sb.from('takstud_schedules')
        .select('*')
        .eq('school_id', state.profile.school_id)
        .order('sort_order').order('time_slot');

    const schedule = rows?.length ? rows : SCHEDULE.map((r, i) => ({
        id: null, time_slot: r.time, mon: r.mon, tue: r.tue,
        wed: r.wed, thu: r.thu, fri: r.fri, sort_order: i,
    }));

    const days = ['Segunda','Terça','Quarta','Quinta','Sexta'];
    const keys = ['mon','tue','wed','thu','fri'];

    let html = '<div class="sch-header">Hora</div>';
    days.forEach(d => html += `<div class="sch-header">${d}</div>`);

    schedule.forEach(row => {
        html += `<div class="sch-time">${esc(row.time_slot)}${canEdit && row.id ? `<button class="sch-del-btn" data-id="${row.id}" title="Remover linha" style="margin-left:.4rem;background:none;border:none;color:#f85149;cursor:pointer;font-size:.75rem">✕</button>` : ''}</div>`;
        keys.forEach(k => html += `<div class="sch-cell"><span class="sch-subject">${esc(row[k] || '')}</span></div>`);
    });

    grid.innerHTML = html;

    if (canEdit) {
        const addRow = document.createElement('div');
        addRow.style.cssText = 'grid-column:1/-1;padding:.5rem;display:flex;justify-content:flex-end';
        addRow.innerHTML = `<button id="btnAddScheduleRow" class="btn-add" style="font-size:.82rem"><i class="fas fa-plus"></i> Adicionar Horário</button>`;
        grid.after(addRow);
        document.getElementById('btnAddScheduleRow')?.addEventListener('click', () => openScheduleModal(null, schedule.length));

        grid.querySelectorAll('.sch-del-btn').forEach(btn =>
            btn.addEventListener('click', () => deleteScheduleRow(btn.dataset.id)));
    }
}

async function deleteScheduleRow(id) {
    if (!confirm('Remover este horário?')) return;
    await sb.from('takstud_schedules').delete().eq('id', id);
    renderSchedule();
    toast('Horário removido.', 'warn');
}

function openScheduleModal(existing = null, order = 0) {
    const m = document.getElementById('scheduleModal');
    document.getElementById('schTime').value   = existing?.time_slot || '';
    document.getElementById('schMon').value    = existing?.mon || '';
    document.getElementById('schTue').value    = existing?.tue || '';
    document.getElementById('schWed').value    = existing?.wed || '';
    document.getElementById('schThu').value    = existing?.thu || '';
    document.getElementById('schFri').value    = existing?.fri || '';
    document.getElementById('scheduleModal').dataset.editId    = existing?.id || '';
    document.getElementById('scheduleModal').dataset.sortOrder = order;
    m.classList.add('open');
}

async function saveScheduleRow() {
    const editId    = document.getElementById('scheduleModal').dataset.editId;
    const sortOrder = Number(document.getElementById('scheduleModal').dataset.sortOrder);
    const time_slot = document.getElementById('schTime').value.trim();
    if (!time_slot) return toast('Horário é obrigatório.', 'error');

    const payload = {
        time_slot,
        mon: document.getElementById('schMon').value.trim(),
        tue: document.getElementById('schTue').value.trim(),
        wed: document.getElementById('schWed').value.trim(),
        thu: document.getElementById('schThu').value.trim(),
        fri: document.getElementById('schFri').value.trim(),
        school_id:  state.profile.school_id,
        sort_order: sortOrder,
    };

    if (editId) {
        await sb.from('takstud_schedules').update(payload).eq('id', editId);
    } else {
        await sb.from('takstud_schedules').insert(payload);
    }
    closeModal('scheduleModal');
    renderSchedule();
    toast('Horário salvo.');
}

/* ─── Modais ─────────────────────────────────────────────────────────────── */
const openModal      = id => document.getElementById(id).classList.add('open');
const closeModal     = id => document.getElementById(id).classList.remove('open');
const closeAllModals = () => document.querySelectorAll('.modal.open').forEach(m => m.classList.remove('open'));

async function openEditStudent(id) {
    const { data: s } = await sb.from('takstud_students').select('*').eq('id', id).single();
    if (!s) return;
    state.editingStudentId = id;
    document.getElementById('sName').value  = s.name;
    document.getElementById('sClass').value = s.cls;
    document.getElementById('sEmail').value = s.email || '';
    document.getElementById('studentModalTitle').textContent = 'Editar Aluno';
    openModal('studentModal');
}

/* ─── CRUD (escritas rejeitadas pelas rules se o perfil não tiver permissão) */
async function saveStudent() {
    const name  = document.getElementById('sName').value.trim();
    const cls   = document.getElementById('sClass').value.trim();
    const email = document.getElementById('sEmail').value.trim();
    if (!name) return toast('Nome é obrigatório.', 'error');
    if (!cls)  return toast('Turma é obrigatória.', 'error');

    if (state.editingStudentId) {
        const { error } = await sb.from('takstud_students')
            .update({ name, cls, email }).eq('id', state.editingStudentId);
        if (error) return toast('Erro ao atualizar: ' + error.message, 'error');
        toast('Aluno atualizado.');
    } else {
        const { error } = await sb.from('takstud_students')
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
    const { error } = await sb.from('takstud_students').delete().eq('id', id);
    if (error) return toast('Erro ao excluir.', 'error');
    renderStudents();
    renderDashboard();
    toast('Aluno removido.', 'warn');
}

async function saveTask() {
    const title   = document.getElementById('tTitle').value.trim();
    const subject = document.getElementById('tSubject').value.trim();
    if (!title) return toast('Título é obrigatório.', 'error');
    const { error } = await sb.from('takstud_tasks').insert({
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
    const { data: t } = await sb.from('takstud_tasks').select('done').eq('id', id).single();
    if (!t) return;
    await sb.from('takstud_tasks').update({ done: !t.done }).eq('id', id);
    renderTasks();
    renderDashboard();
}

async function deleteTask(id) {
    await sb.from('takstud_tasks').delete().eq('id', id);
    renderTasks();
    renderDashboard();
    toast('Tarefa removida.', 'warn');
}

async function saveNotice() {
    const title   = document.getElementById('nTitle').value.trim();
    const content = document.getElementById('nContent').value.trim();
    if (!title)   return toast('Título é obrigatório.', 'error');
    if (!content) return toast('Conteúdo é obrigatório.', 'error');
    const { error } = await sb.from('takstud_notices').insert({
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
    await sb.from('takstud_notices').delete().eq('id', id);
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
    document.getElementById('btnShowForgot').addEventListener('click', showForgotSection);
    document.getElementById('btnBackToLogin').addEventListener('click', showLoginSection);
    document.getElementById('btnForgot').addEventListener('click', forgotPassword);
    document.getElementById('btnResetPassword').addEventListener('click', updatePassword);
    document.getElementById('forgotEmail').addEventListener('keydown', e => { if (e.key === 'Enter') forgotPassword(); });
    document.getElementById('newPassword').addEventListener('keydown', e => { if (e.key === 'Enter') updatePassword(); });
    document.getElementById('confirmPassword').addEventListener('keydown', e => { if (e.key === 'Enter') updatePassword(); });

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

    const printBtn = document.createElement('button');
    printBtn.className = 'btn-export';
    printBtn.innerHTML = '<i class="fas fa-print"></i> Imprimir';
    printBtn.title = 'Imprimir / exportar PDF';
    printBtn.addEventListener('click', printView);
    document.querySelector('.topbar-user').prepend(printBtn);

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

    /* Modal de horário */
    document.getElementById('closeScheduleModal')?.addEventListener('click', () => closeModal('scheduleModal'));
    document.getElementById('saveScheduleRow')?.addEventListener('click', saveScheduleRow);

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

