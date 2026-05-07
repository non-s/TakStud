const DB = {
  get students(){ return JSON.parse(localStorage.getItem('ts_students')||'[]') },
  set students(v){ localStorage.setItem('ts_students',JSON.stringify(v)) },
  get tasks(){ return JSON.parse(localStorage.getItem('ts_tasks')||'[]') },
  set tasks(v){ localStorage.setItem('ts_tasks',JSON.stringify(v)) },
  get notices(){ return JSON.parse(localStorage.getItem('ts_notices')||'[]') },
  set notices(v){ localStorage.setItem('ts_notices',JSON.stringify(v)) },
};

let taskFilter='all', studentSearch='', editingStudentId=null;

const SCHEDULE=[
  {time:'07:00',mon:'Matemática',tue:'Português',wed:'História',thu:'Ciências',fri:'Ed. Física'},
  {time:'08:00',mon:'Português',tue:'Matemática',wed:'Ciências',thu:'Matemática',fri:'Artes'},
  {time:'09:00',mon:'História',tue:'Ciências',wed:'Matemática',thu:'Português',fri:'Inglês'},
  {time:'10:30',mon:'Ciências',tue:'História',wed:'Inglês',thu:'História',fri:'Matemática'},
  {time:'11:30',mon:'Inglês',tue:'Ed. Física',wed:'Português',thu:'Artes',fri:'Português'},
];

if(!localStorage.getItem('ts_seeded')){
  DB.students=[
    {id:1,name:'Ana Souza',cls:'3A',email:'ana@escola.edu.br'},
    {id:2,name:'Bruno Lima',cls:'3A',email:'bruno@escola.edu.br'},
    {id:3,name:'Carla Santos',cls:'2B',email:'carla@escola.edu.br'},
  ];
  DB.tasks=[
    {id:1,title:'Lista de Exercícios — Cap. 5',subject:'Matemática',due:'2025-05-15',desc:'Exercícios pares da pág. 120-130.',done:false},
    {id:2,title:'Redação: Meio Ambiente',subject:'Português',due:'2025-05-20',desc:'Mínimo 30 linhas.',done:true},
    {id:3,title:'Mapa Conceitual — Células',subject:'Ciências',due:'2025-05-22',desc:'Usar caneta e régua.',done:false},
  ];
  DB.notices=[
    {id:1,title:'Reunião de Pais — 20/05',content:'A reunião será às 19h no auditório. Confirme presença.',date:'2025-05-07'},
    {id:2,title:'Simulado ENEM — 28/05',content:'Traga documento com foto. Início às 8h pontualmente.',date:'2025-05-07'},
  ];
  localStorage.setItem('ts_seeded','1');
}

function showView(view){
  document.querySelectorAll('.view').forEach(v=>v.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n=>n.classList.remove('active'));
  document.getElementById(`view-${view}`).classList.add('active');
  document.querySelector(`[data-view="${view}"]`).classList.add('active');
  const renders={dashboard:renderDashboard,students:renderStudents,tasks:renderTasks,notices:renderNotices,schedule:renderSchedule};
  renders[view]?.();
}

function renderDashboard(){
  const tasks=DB.tasks,students=DB.students,notices=DB.notices;
  document.getElementById('statStudents').textContent=students.length;
  document.getElementById('statTasks').textContent=tasks.length;
  document.getElementById('statDone').textContent=tasks.filter(t=>t.done).length;
  document.getElementById('statNotices').textContent=notices.length;
  const pending=tasks.filter(t=>!t.done).slice(0,5);
  document.getElementById('dashPendingTasks').innerHTML=pending.length
    ?pending.map(t=>`<li><i class="fas fa-circle"></i>${t.title}</li>`).join('')
    :'<li class="empty">Nenhuma tarefa pendente.</li>';
  document.getElementById('dashNotices').innerHTML=notices.slice(0,3).length
    ?notices.slice(0,3).map(n=>`<li><i class="fas fa-circle"></i>${n.title}</li>`).join('')
    :'<li class="empty">Nenhum comunicado.</li>';
}

function renderStudents(){
  const students=DB.students.filter(s=>!studentSearch||s.name.toLowerCase().includes(studentSearch.toLowerCase()));
  document.getElementById('studentsBody').innerHTML=students.length
    ?students.map(s=>`<tr><td>${s.name}</td><td><span class="badge-class">${s.cls}</span></td><td>${s.email||'—'}</td>
       <td><div class="td-actions">
         <button class="btn-icon-sm edit" onclick="editStudent(${s.id})" title="Editar"><i class="fas fa-edit"></i></button>
         <button class="btn-icon-sm" onclick="deleteStudent(${s.id})" title="Excluir"><i class="fas fa-trash"></i></button>
       </div></td></tr>`).join('')
    :`<tr><td colspan="4"><div class="empty-state"><i class="fas fa-users"></i><p>Nenhum aluno encontrado.</p></div></td></tr>`;
}

function editStudent(id){
  const s=DB.students.find(s=>s.id===id); if(!s) return;
  editingStudentId=id;
  document.getElementById('sName').value=s.name;
  document.getElementById('sClass').value=s.cls;
  document.getElementById('sEmail').value=s.email||'';
  document.getElementById('studentModalTitle').textContent='Editar Aluno';
  openModal('studentModal');
}

function deleteStudent(id){
  if(!confirm('Excluir este aluno?')) return;
  DB.students=DB.students.filter(s=>s.id!==id);
  renderStudents(); renderDashboard();
}

function renderTasks(){
  let tasks=DB.tasks;
  if(taskFilter==='pending') tasks=tasks.filter(t=>!t.done);
  if(taskFilter==='done')    tasks=tasks.filter(t=>t.done);
  document.getElementById('tasksList').innerHTML=tasks.length
    ?tasks.map(t=>`<div class="task-item ${t.done?'done':''}">
      <div class="task-check ${t.done?'checked':''}" onclick="toggleTask(${t.id})">${t.done?'<i class="fas fa-check"></i>':''}</div>
      <div class="task-body">
        <div class="task-title">${t.title}</div>
        <div class="task-meta"><span><i class="fas fa-book"></i>${t.subject}</span>${t.due?`<span><i class="fas fa-calendar"></i>${formatDate(t.due)}</span>`:''}</div>
        ${t.desc?`<div class="task-desc">${t.desc}</div>`:''}
      </div>
      <button class="task-del" onclick="deleteTask(${t.id})"><i class="fas fa-times"></i></button>
    </div>`).join('')
    :'<div class="empty-state"><i class="fas fa-tasks"></i><p>Nenhuma tarefa.</p></div>';
}

function toggleTask(id){
  const tasks=DB.tasks,t=tasks.find(t=>t.id===id); if(t) t.done=!t.done;
  DB.tasks=tasks; renderTasks(); renderDashboard();
}

function deleteTask(id){ DB.tasks=DB.tasks.filter(t=>t.id!==id); renderTasks(); renderDashboard(); }

function renderNotices(){
  document.getElementById('noticesList').innerHTML=DB.notices.length
    ?DB.notices.map(n=>`<div class="notice-item">
      <div class="notice-header">
        <span class="notice-title">${n.title}</span>
        <div style="display:flex;align-items:center;gap:.7rem">
          <span class="notice-date">${n.date}</span>
          <button class="btn-icon-sm" onclick="deleteNotice(${n.id})"><i class="fas fa-trash"></i></button>
        </div>
      </div>
      <div class="notice-content">${n.content}</div>
    </div>`).join('')
    :'<div class="empty-state"><i class="fas fa-bullhorn"></i><p>Nenhum comunicado.</p></div>';
}

function deleteNotice(id){ DB.notices=DB.notices.filter(n=>n.id!==id); renderNotices(); renderDashboard(); }

function renderSchedule(){
  const days=['Segunda','Terça','Quarta','Quinta','Sexta'],keys=['mon','tue','wed','thu','fri'];
  let html='<div class="sch-header">Hora</div>';
  days.forEach(d=>html+=`<div class="sch-header">${d}</div>`);
  SCHEDULE.forEach(row=>{
    html+=`<div class="sch-time">${row.time}</div>`;
    keys.forEach(k=>html+=`<div class="sch-cell"><span class="sch-subject">${row[k]}</span></div>`);
  });
  document.getElementById('scheduleGrid').innerHTML=html;
}

const openModal=id=>document.getElementById(id).classList.add('open');
const closeModal=id=>document.getElementById(id).classList.remove('open');
const uid=()=>Date.now()+Math.floor(Math.random()*1000);
const formatDate=d=>{ if(!d)return''; const[y,m,day]=d.split('-'); return`${day}/${m}/${y}`; };

document.addEventListener('DOMContentLoaded',()=>{
  document.querySelectorAll('.nav-item').forEach(btn=>btn.addEventListener('click',()=>showView(btn.dataset.view)));

  document.querySelectorAll('.role-btn').forEach(btn=>btn.addEventListener('click',()=>{
    document.querySelectorAll('.role-btn').forEach(b=>b.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById('currentRole').textContent={teacher:'Prof. Demo',student:'Aluno Demo',admin:'Admin'}[btn.dataset.role];
  }));

  document.getElementById('openAddStudent').addEventListener('click',()=>{
    editingStudentId=null;
    ['sName','sClass','sEmail'].forEach(id=>document.getElementById(id).value='');
    document.getElementById('studentModalTitle').textContent='Adicionar Aluno';
    openModal('studentModal');
  });
  document.getElementById('closeStudentModal').addEventListener('click',()=>closeModal('studentModal'));
  document.getElementById('saveStudent').addEventListener('click',()=>{
    const name=document.getElementById('sName').value.trim();
    const cls=document.getElementById('sClass').value.trim();
    const email=document.getElementById('sEmail').value.trim();
    if(!name||!cls) return alert('Nome e turma são obrigatórios.');
    const students=DB.students;
    if(editingStudentId){ const s=students.find(s=>s.id===editingStudentId); if(s){s.name=name;s.cls=cls;s.email=email;} }
    else students.push({id:uid(),name,cls,email});
    DB.students=students; closeModal('studentModal'); renderStudents(); renderDashboard();
  });
  document.getElementById('studentSearch').addEventListener('input',e=>{ studentSearch=e.target.value; renderStudents(); });

  document.getElementById('openAddTask').addEventListener('click',()=>openModal('taskModal'));
  document.getElementById('closeTaskModal').addEventListener('click',()=>closeModal('taskModal'));
  document.getElementById('saveTask').addEventListener('click',()=>{
    const title=document.getElementById('tTitle').value.trim();
    const subject=document.getElementById('tSubject').value.trim();
    if(!title) return alert('Título é obrigatório.');
    const tasks=DB.tasks;
    tasks.unshift({id:uid(),title,subject,due:document.getElementById('tDue').value,desc:document.getElementById('tDesc').value.trim(),done:false});
    DB.tasks=tasks;
    ['tTitle','tSubject','tDue','tDesc'].forEach(id=>document.getElementById(id).value='');
    closeModal('taskModal'); renderTasks(); renderDashboard();
  });
  document.querySelectorAll('.filter-btn').forEach(btn=>btn.addEventListener('click',()=>{
    taskFilter=btn.dataset.filter;
    document.querySelectorAll('.filter-btn').forEach(b=>b.classList.remove('active'));
    btn.classList.add('active'); renderTasks();
  }));

  document.getElementById('openAddNotice').addEventListener('click',()=>openModal('noticeModal'));
  document.getElementById('closeNoticeModal').addEventListener('click',()=>closeModal('noticeModal'));
  document.getElementById('saveNotice').addEventListener('click',()=>{
    const title=document.getElementById('nTitle').value.trim();
    const content=document.getElementById('nContent').value.trim();
    if(!title||!content) return alert('Título e conteúdo são obrigatórios.');
    const notices=DB.notices;
    notices.unshift({id:uid(),title,content,date:new Date().toISOString().split('T')[0]});
    DB.notices=notices;
    ['nTitle','nContent'].forEach(id=>document.getElementById(id).value='');
    closeModal('noticeModal'); renderNotices(); renderDashboard();
  });

  document.querySelectorAll('.modal').forEach(m=>m.addEventListener('click',e=>{ if(e.target===m) m.classList.remove('open'); }));

  showView('dashboard');
});
