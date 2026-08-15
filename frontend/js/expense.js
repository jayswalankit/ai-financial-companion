/* ================= RECORDS ================= */
    async function renderRecords(){
      const el = $('#content');
      const f = state.recordsFilter;
      el.innerHTML = `
        <div class="toolbar">
          ${f.categoryLock ? `<span class="badge badge-jade">Category: ${escapeHtml(f.categoryLock.name)} <button id="clear-cat-lock" style="background:none;border:none;color:inherit;cursor:pointer;margin-left:4px;">✕</button></span>` : ''}
          <div class="search-box">
            <span>⌕</span>
            <input type="text" id="rec-search" placeholder="Search by name (dominos) or date (4/8/2026)" value="${escapeHtml(f.keyword)}">
          </div>
          ${!f.categoryLock ? `<select id="rec-cat-filter"><option value="">All categories</option>
            ${state.categories.map(c=>`<option value="${c.id}" ${String(f.categoryId)===String(c.id)?'selected':''}>${escapeHtml(c.name)}</option>`).join('')}
          </select>` : ''}
          <input type="date" id="rec-start" value="${f.startDate}" title="From date">
          <input type="date" id="rec-end" value="${f.endDate}" title="To date">
          <button class="btn btn-primary btn-sm" id="btn-add-expense">+ Add expense</button>
        </div>
        <div id="records-table-wrap"><div class="empty-state"><p>Loading…</p></div></div>
      `;
      $('#clear-cat-lock')?.addEventListener('click', () => { state.recordsFilter = {keyword:'', categoryId:'', startDate:'', endDate:'', page:0, size:10, categoryLock:null, dateFromSearch:false}; renderRecords(); });
      $('#rec-cat-filter')?.addEventListener('change', (e) => { state.recordsFilter.categoryId = e.target.value; state.recordsFilter.page = 0; loadRecordsTable(); });
      $('#rec-start').addEventListener('change', (e) => { state.recordsFilter.startDate = e.target.value; state.recordsFilter.dateFromSearch = false; state.recordsFilter.page = 0; loadRecordsTable(); });
      $('#rec-end').addEventListener('change', (e) => { state.recordsFilter.endDate = e.target.value; state.recordsFilter.dateFromSearch = false; state.recordsFilter.page = 0; loadRecordsTable(); });
      $('#btn-add-expense').addEventListener('click', () => openExpenseModal(null, loadRecordsTable));
      let searchTimer;
      $('#rec-search').addEventListener('input', (e) => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => {
          const v = e.target.value.trim();
          const asDate = parseFlexibleDate(v);
          if(asDate){
            state.recordsFilter.keyword = '';
            state.recordsFilter.startDate = asDate;
            state.recordsFilter.endDate = asDate;
            state.recordsFilter.dateFromSearch = true;
          } else {
            state.recordsFilter.keyword = v;
            // Only clear the date range if WE set it from an earlier date search —
            // don't touch it if the person set it explicitly via the date pickers.
            if(state.recordsFilter.dateFromSearch){
              state.recordsFilter.startDate = '';
              state.recordsFilter.endDate = '';
              state.recordsFilter.dateFromSearch = false;
            }
          }
          state.recordsFilter.page = 0;
          renderRecords();
        }, 400);
      });
      loadRecordsTable();
    }
    async function loadRecordsTable(){
      const wrap = $('#records-table-wrap');
      if(!wrap) return;
      const f = state.recordsFilter;
      const catId = f.categoryLock ? f.categoryLock.id : (f.categoryId || null);
      // Only send a date range if BOTH ends are set — the backend rejects a request
      // with just one of startDate/endDate.
      const hasRange = !!(f.startDate && f.endDate);
      let page;
      try{
        page = await api('/api/expenses', {params:{
          keyword: f.keyword || null, categoryId: catId, startDate: hasRange ? f.startDate : null, endDate: hasRange ? f.endDate : null,
          page: f.page, size: f.size, sort: 'expenseDate,desc'
        }});
      }catch(e){ wrap.innerHTML = errorPanel(e.message, loadRecordsTable); return; }
      const items = page.content || [];
      if(!items.length){
        wrap.innerHTML = `<div class="empty-state"><div class="ic">☰</div><h4>No records found</h4><p>Try clearing filters or add a new expense.</p></div>`;
        return;
      }
      wrap.innerHTML = `
        <div class="table-wrap"><table>
          <thead><tr><th>Date</th><th>Title</th><th>Category</th><th>Description</th><th style="text-align:right;">Amount</th><th></th></tr></thead>
          <tbody>${items.map(x => `
            <tr>
              <td class="mono">${fmtDate(x.expenseDate)}</td>
              <td>${escapeHtml(x.title)}</td>
              <td><span class="badge badge-muted">${escapeHtml(x.categoryName)}</span></td>
              <td style="color:var(--text-muted); max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${escapeHtml(x.description||'—')}</td>
              <td class="mono" style="text-align:right;">${money(x.amount)}</td>
              <td><div class="row-actions">
                <button class="btn-icon" data-edit="${x.id}" title="Edit">✎</button>
                <button class="btn-icon" data-del="${x.id}" title="Delete">🗑</button>
              </div></td>
            </tr>`).join('')}
          </tbody>
        </table></div>
        <div class="pagination">
          <span>Page ${(page.number ?? 0)+1} of ${Math.max(page.totalPages||1,1)} · ${page.totalElements ?? items.length} record${(page.totalElements===1)?'':'s'}</span>
          <div class="pgbtns">
            <button class="btn btn-ghost btn-sm" id="pg-prev" ${page.first?'disabled':''}>← Prev</button>
            <button class="btn btn-ghost btn-sm" id="pg-next" ${page.last?'disabled':''}>Next →</button>
          </div>
        </div>
      `;
      $('#pg-prev')?.addEventListener('click', () => { state.recordsFilter.page = Math.max(0, f.page-1); loadRecordsTable(); });
      $('#pg-next')?.addEventListener('click', () => { state.recordsFilter.page = f.page+1; loadRecordsTable(); });
      $all('[data-edit]', wrap).forEach(b => b.addEventListener('click', async () => {
        try{ const exp = await api('/api/expenses/' + b.dataset.edit); openExpenseModal(exp, loadRecordsTable); }
        catch(e){ toast(e.message,'err'); }
      }));
      $all('[data-del]', wrap).forEach(b => b.addEventListener('click', () => confirmDeleteExpense(b.dataset.del)));
    }
    function confirmDeleteExpense(id){
      openModal(`
        <div class="modal-head"><h3>Delete expense</h3><button class="modal-close" data-close>✕</button></div>
        <div class="modal-body"><p>This expense record will be permanently removed. This can't be undone.</p></div>
        <div class="modal-foot"><button class="btn btn-ghost" data-close>Cancel</button><button class="btn btn-danger" id="btn-confirm-del-exp">Delete</button></div>
      `);
      $('#btn-confirm-del-exp').addEventListener('click', async () => {
        try{ await api('/api/expenses/' + id, {method:'DELETE'}); toast('Expense deleted.'); closeModal();
          if(state.page === 'records') loadRecordsTable(); else if(state.page==='dashboard') renderDashboard();
        }catch(e){ toast(e.message,'err'); }
      });
    }

    /* ================= EXPENSE MODAL (shared: quick add / records / dashboard) ================= */
    function openExpenseModal(expense=null, onSaved=null){
      if(state.expenseSaveInProgress){
        toast('Your expense is still saving. Please wait.', 'err');
        return;
      }
      const isEdit = !!expense;
      if(!state.categories.length){
        toast('Create a category first before adding expenses.', 'err');
        goPage('categories');
        return;
      }
      openModal(`
        <div class="modal-head"><h3>${isEdit?'Edit expense':'Add expense'}</h3><button class="modal-close" data-close>✕</button></div>
        <div class="modal-body">
          <div class="field"><label>Title</label><input type="text" id="exp-title" value="${isEdit?escapeHtml(expense.title):''}" placeholder="e.g. Dominos pizza"></div>
          <div class="field"><label>Amount</label><input type="number" step="0.01" min="0.01" id="exp-amount" value="${isEdit?expense.amount:''}" placeholder="0.00"></div>
          <div class="field"><label>Category</label><select id="exp-category">
            ${state.categories.filter(c=>c.type==='EXPENSE').map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('')}
          </select></div>
          <div class="field"><label>Date</label><input type="date" id="exp-date" value="${isEdit?expense.expenseDate:toISO(new Date())}"></div>
          <div class="field"><label>Description (optional)</label><textarea id="exp-desc" rows="2" style="width:100%; padding:11px 13px; border-radius:8px; border:1px solid var(--line); background:var(--bg-sunk); color:var(--text); resize:vertical;">${isEdit?escapeHtml(expense.description||''):''}</textarea></div>
        </div>
        <div class="modal-foot"><button class="btn btn-ghost" data-close>Cancel</button><button class="btn btn-primary" id="btn-save-exp">${isEdit?'Save changes':'Add expense'}</button></div>
      `);
      if(isEdit){
        const catMatch = state.categories.find(c => c.name === expense.categoryName);
        if(catMatch) $('#exp-category').value = catMatch.id;
      }
      let isSaving = false;
      $('#btn-save-exp').addEventListener('click', async () => {
        if(isSaving || state.expenseSaveInProgress) return;
        const title = $('#exp-title').value.trim();
        const amount = parseFloat($('#exp-amount').value);
        const categoryId = $('#exp-category').value;
        const expenseDate = $('#exp-date').value;
        const description = $('#exp-desc').value.trim();
        if(!title || !amount || amount <= 0 || !categoryId || !expenseDate){ toast('Fill in title, amount, category and date.', 'err'); return; }
        const body = { title, amount, description, expenseDate, categoryId: Number(categoryId) };
        const saveButton = $('#btn-save-exp');
        isSaving = true;
        state.expenseSaveInProgress = true;
        saveButton.disabled = true;
        saveButton.textContent = 'Savingâ€¦';
        $all('#modal-root input, #modal-root select, #modal-root textarea').forEach(control => control.disabled = true);
        try{
          const savedExpense = isEdit
            ? await api('/api/expenses/' + expense.id, {method:'PUT', body})
            : await api('/api/expenses', {method:'POST', body});
          toast(isEdit ? 'Expense updated.' : 'Expense added.');
          closeModal();
          if(onSaved) await onSaved(savedExpense);
          else if(state.page==='dashboard') await renderDashboard();
          state.expenseSaveInProgress = false;
        }catch(e){
          isSaving = false;
          state.expenseSaveInProgress = false;
          saveButton.disabled = false;
          saveButton.textContent = isEdit ? 'Save changes' : 'Add expense';
          $all('#modal-root input, #modal-root select, #modal-root textarea').forEach(control => control.disabled = false);
          toast(e.message,'err');
        }
      });
    }
