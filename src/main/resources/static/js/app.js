(() => {
  const $ = (selector) => document.querySelector(selector);
  const $$ = (selector) => [...document.querySelectorAll(selector)];
  const colors = ['#806bdc','#9b89e7','#c0b4f0','#5ab993','#f0a77d','#edcc65','#6ca4ca','#dd84aa','#8a9b70','#e18e73'];
  const icons = {Food:'◉',Groceries:'◌','Dining Out':'◒',Transport:'↗',Travel:'⌁',Shopping:'◇',Healthcare:'✚',Housing:'⌂',Utilities:'ϟ',Education:'▤',Entertainment:'♫',Fitness:'✦','Personal Care':'✧',Gifts:'♡',Subscriptions:'▣',Insurance:'⬡',Taxes:'％',Investments:'↗',Other:'✳'};
  let token = null;
  let username = null;
  let categories = [];
  let expenses = [];
  let editingId = null;
  let authMode = 'login';
  const today = new Date();
  const isoToday = new Date(today.getTime() - today.getTimezoneOffset() * 60000).toISOString().slice(0,10);
  const monthInput = $('#month-select');
  monthInput.value = isoToday.slice(0,7);
  $('#expense-date').value = isoToday;
  $('#today-label').textContent = today.toLocaleDateString(undefined,{weekday:'short',month:'short',day:'numeric'});
  const formatMoney = value => new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',minimumFractionDigits:2}).format(Number(value)||0);
  const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  function alertMessage(message,type='error') { const box=$('#alert'); box.textContent=message; box.className=`alert ${type}`; window.scrollTo({top:0,behavior:'smooth'}); }
  function clearAlert(){ $('#alert').className='alert hidden'; }
  function openModal(id){ $('#'+id).classList.remove('hidden'); }
  function closeModal(id){ $('#'+id).classList.add('hidden'); }
  async function api(path, options={}) {
    const headers = {...(options.body?{'Content-Type':'application/json'}:{}),...(token?{Authorization:`Bearer ${token}`}:{})};
    const response = await fetch(path,{...options,headers:{...headers,...options.headers}});
    if(response.status===204) return null;
    const type=response.headers.get('content-type')||'';
    const body=type.includes('application/json')?await response.json():await response.text();
    if(!response.ok){let message=typeof body==='string'?body:(body.message||body.error||'Something went wrong.');if(response.status===401||response.status===403)message='Please sign in again to continue.';throw new Error(message);}
    return body;
  }
  function setUser(){
    $('#user-name').textContent=username||'Guest'; $('#avatar').textContent=(username||'G').trim().charAt(0).toUpperCase();
    $('#user-email').textContent=token?'Your personal workspace':'Sign in to get started'; $('#logout').classList.toggle('hidden',!token);
    $('#welcome').textContent=token?`Good to see you, ${username||'there'}.`:'A clearer view of your spending.';
  }
  async function loadCategories(){const select=$('#expense-category');select.innerHTML='<option value="">Loading categories…</option>';select.disabled=true;try{categories=await api('/api/categories');if(!Array.isArray(categories)||categories.length===0){select.innerHTML='<option value="">No categories available</option>';return;}select.innerHTML='<option value="">Choose a category</option>'+categories.map(c=>`<option value="${escapeHtml(c.name)}">${escapeHtml(c.name)}</option>`).join('');}catch(error){categories=[];select.innerHTML='<option value="">Could not load categories</option>';alertMessage(`Could not load categories: ${error.message}`);}finally{select.disabled=false;}}
  function range(){const [year,month]=monthInput.value.split('-').map(Number);const last=new Date(year,month,0).getDate();return {from:`${monthInput.value}-01`,to:`${monthInput.value}-${String(last).padStart(2,'0')}`};}
  async function loadDashboard(){
    if(!token){expenses=[];render();return;}
    const {from,to}=range();
    try{[expenses]=await Promise.all([api(`/api/expense?from=${from}&to=${to}`),api(`/api/expense/summary?from=${from}&to=${to}`)]);render();}
    catch(error){alertMessage(error.message);if(error.message.includes('sign in again'))logout();}
  }
  function render(){
    const total=expenses.reduce((sum,e)=>sum+Number(e.amount),0);$('#total-spent').textContent=formatMoney(total);$('#transaction-count').textContent=expenses.length;
    const totals={};expenses.forEach(e=>totals[e.category]=(totals[e.category]||0)+Number(e.amount));const entries=Object.entries(totals).sort((a,b)=>b[1]-a[1]);
    $('#top-category').textContent=entries[0]?.[0]||'—';$('#top-category-foot').textContent=entries[0]?`${formatMoney(entries[0][1])} this period`:'No spending yet';$('#donut-count').textContent=entries.length;
    const monthDate=new Date(`${monthInput.value}-02T12:00:00`);$('#period-pill').textContent=monthDate.toLocaleDateString(undefined,{month:'long',year:'numeric'});
    const sum=entries.reduce((n,e)=>n+e[1],0);let offset=0;const segments=entries.map(([,v],i)=>{const start=offset;offset+=sum?v/sum*100:0;return `${colors[i%colors.length]} ${start}% ${offset}%`;});$('#donut').style.background=entries.length?`conic-gradient(${segments.join(',')})`:'';
    $('#legend').innerHTML=entries.length?entries.slice(0,5).map(([name,value],i)=>`<div class="legend-item"><span class="legend-dot" style="background:${colors[i%colors.length]}"></span><span class="legend-name">${escapeHtml(name)}</span><span class="legend-value">${formatMoney(value)}</span></div>`).join(''):'<div class="empty-state small">Add an expense to see your breakdown.</div>';
    $('#recent-list').innerHTML=expenses.length?expenses.slice(0,4).map(e=>recentHtml(e)).join(''):'<div class="empty-state">Your expenses will show up here.</div>';
    renderTable($('#search-input').value||'');
  }
  function recentHtml(e){return `<div class="recent-item"><div class="category-icon">${icons[e.category]||'✳'}</div><div><div class="recent-name">${escapeHtml(e.category)}</div><div class="recent-date">${formatDate(e.expenseDate)}${e.note?` · ${escapeHtml(e.note)}`:''}</div></div><div class="recent-amount">−${formatMoney(e.amount)}</div></div>`;}
  function formatDate(date){return new Date(`${date}T12:00:00`).toLocaleDateString(undefined,{month:'short',day:'numeric',year:'numeric'});}
  function renderTable(query=''){
    const filtered=expenses.filter(e=>`${e.category} ${e.note||''}`.toLowerCase().includes(query.toLowerCase()));
    $('#expense-rows').innerHTML=filtered.map(e=>`<tr><td><span class="category-icon" style="display:inline-grid;vertical-align:middle;margin-right:8px">${icons[e.category]||'✳'}</span>${escapeHtml(e.category)}</td><td>${formatDate(e.expenseDate)}</td><td>${escapeHtml(e.note||'—')}</td><td class="amount">${formatMoney(e.amount)}</td><td><div class="row-actions"><button data-edit="${e.id}" aria-label="Edit expense">Edit</button><button data-delete="${e.id}" aria-label="Delete expense">Delete</button></div></td></tr>`).join('');
    $('#table-empty').classList.toggle('hidden',filtered.length>0);
  }
  async function ensureAuth(){if(!token)openModal('auth-modal');}
  function logout(){token=null;username=null;localStorage.removeItem('ledger_token');localStorage.removeItem('ledger_username');setUser();expenses=[];render();alertMessage('You have signed out. Sign in to view your expenses.','success');}
  function setAuthMode(mode){authMode=mode;const isRegister=mode==='register';$('#auth-title').textContent=isRegister?'Create your account.':'Good to see you.';$('#auth-subtitle').textContent=isRegister?'A fresh start for your finances.':'Sign in to your account to continue.';$('#username-field').classList.toggle('hidden',!isRegister);$('#auth-username').required=isRegister;$('#auth-password').autocomplete(isRegister?'new-password':'current-password');$('#auth-submit').innerHTML=`${isRegister?'Create account':'Sign in'} <span>→</span>`;$$('[data-auth-mode]').forEach(b=>b.classList.toggle('selected',b.dataset.authMode===mode));}
  $('#auth-form').addEventListener('submit',async event=>{event.preventDefault();clearAlert();const submit=$('#auth-submit');submit.disabled=true;try{if(authMode==='register'){const result=await api('/api/auth/register',{method:'POST',body:JSON.stringify({userName:$('#auth-username').value.trim(),email:$('#auth-email').value.trim(),password:$('#auth-password').value})});if(typeof result==='string'&&result.startsWith('Error:'))throw new Error(result.replace('Error:','').trim());setAuthMode('login');$('#auth-password').value='';alertMessage('Account created. Sign in to continue.','success');}else{const result=await api('/api/auth/login',{method:'POST',body:JSON.stringify({email:$('#auth-email').value.trim(),password:$('#auth-password').value})});token=result.token;username=result.username;localStorage.setItem('ledger_token',token);localStorage.setItem('ledger_username',username);setUser();closeModal('auth-modal');clearAlert();await loadDashboard();}}catch(error){$('#auth-modal').classList.remove('hidden');alertMessage(error.message);}finally{submit.disabled=false;}});
  async function openExpense(expense){if(!token){ensureAuth();return;}editingId=expense?.id??null;$('#expense-title').textContent=editingId?'Edit expense':'Add an expense';$('#expense-submit').innerHTML=editingId?'Save changes <span>→</span>':'Save expense <span>→</span>';$('#expense-amount').value=expense?.amount??'';$('#expense-category').value='';$('#expense-date').value=expense?.expenseDate??isoToday;$('#expense-note').value=expense?.note??'';openModal('expense-modal');await loadCategories();$('#expense-category').value=expense?.category??'';}
  $('#expense-form').addEventListener('submit',async event=>{event.preventDefault();clearAlert();const body={amount:Number($('#expense-amount').value),category_name:$('#expense-category').value,note:$('#expense-note').value.trim()||null,expenseDate:$('#expense-date').value};const button=$('#expense-submit');button.disabled=true;try{await api(editingId?`/api/expense/${editingId}`:'/api/expense',{method:editingId?'PUT':'POST',body:JSON.stringify(body)});closeModal('expense-modal');alertMessage(editingId?'Expense updated.':'Expense added.','success');await loadDashboard();}catch(error){alertMessage(error.message);}finally{button.disabled=false;}});
  document.addEventListener('click',async event=>{const close=event.target.closest('[data-close]');if(close)closeModal(close.dataset.close);const mode=event.target.closest('[data-auth-mode]');if(mode)setAuthMode(mode.dataset.authMode);const edit=event.target.closest('[data-edit]');if(edit){const expense=expenses.find(e=>String(e.id)===edit.dataset.edit);if(expense)openExpense(expense);}const del=event.target.closest('[data-delete]');if(del){const expense=expenses.find(e=>String(e.id)===del.dataset.delete);if(!expense)return;if(!window.confirm(`Delete this ${expense.category.toLowerCase()} expense?`))return;try{await api(`/api/expense/${expense.id}`,{method:'DELETE'});alertMessage('Expense deleted.','success');await loadDashboard();}catch(error){alertMessage(error.message);}}});
  $('#top-add').addEventListener('click',()=>openExpense());$('#table-add').addEventListener('click',()=>openExpense());$('#month-select').addEventListener('change',loadDashboard);$('#search-input').addEventListener('input',e=>renderTable(e.target.value));$('#logout').addEventListener('click',logout);
  function setView(view){const transactions=view==='expenses';$('#transactions-panel').classList.toggle('hidden',!transactions);$('.dashboard-grid').classList.toggle('hidden',transactions);$('.stats-grid').classList.toggle('hidden',transactions);$('#crumb').textContent=transactions?'Transactions':'Overview';$$('.nav-item').forEach(item=>item.classList.toggle('active',item.dataset.view===view));if(transactions&&!token)ensureAuth();}
  $$('.nav-item').forEach(item=>item.addEventListener('click',()=>setView(item.dataset.view)));$('#view-all').addEventListener('click',()=>setView('expenses'));
  $$('.modal-backdrop').forEach(backdrop=>backdrop.addEventListener('click',e=>{if(e.target===backdrop)closeModal(backdrop.id);}));document.addEventListener('keydown',e=>{if(e.key==='Escape')$$('.modal-backdrop').forEach(m=>m.classList.add('hidden'));});
  localStorage.removeItem('ledger_token');localStorage.removeItem('ledger_username');
  setUser();setAuthMode('login');render();
})();
