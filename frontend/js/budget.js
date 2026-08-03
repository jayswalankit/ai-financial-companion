/* ================= BUDGET ================= */
    async function renderBudget(){
      const el = $('#content');
      const now = new Date();
      const isPast = (budgetSel.year < now.getFullYear()) || (budgetSel.year === now.getFullYear() && budgetSel.month < (now.getMonth()+1));
      el.innerHTML = `
        <div class="toolbar">
          <select id="budget-month">${MONTH_NAMES.map((m,i)=>`<option value="${i+1}" ${budgetSel.month===i+1?'selected':''}>${m}</option>`).join('')}</select>
          <select id="budget-year">${[budgetSel.year-1,budgetSel.year,budgetSel.year+1].map(y=>`<option value="${y}" ${budgetSel.year===y?'selected':''}>${y}</option>`).join('')}</select>
          ${isPast ? `<span class="badge badge-muted">Past month — read only</span>` : ''}
        </div>
        <div class="grid grid-2" style="align-items:start;">
          <div class="card" id="budget-view-card"><h3>Budget for this period</h3><div class="card-sub">Loading…</div></div>
          <div class="card">
            <h3>Set / update budget</h3>
            <div class="card-sub">Applies to the selected month and year</div>
            ${isPast ? `<div class="empty-state" style="padding:24px 10px;"><div class="ic">◐</div><p>Past months are locked so old records stay accurate — you can only set or update the budget for the current month onward.</p></div>` : `
            <div class="field"><label>Monthly budget amount</label><input type="number" min="0.1" step="0.01" id="budget-amount" placeholder="e.g. 25000"></div>
            <button class="btn btn-primary" id="btn-save-budget">Save budget</button>`}
          </div>
        </div>
        <div class="card" style="margin-top:16px;" id="budget-status-card"><h3>Latest budget on file</h3><div class="card-sub">Loading…</div></div>
      `;
      $('#budget-month').addEventListener('change', e => { budgetSel.month = Number(e.target.value); renderBudget(); });
      $('#budget-year').addEventListener('change', e => { budgetSel.year = Number(e.target.value); renderBudget(); });
      $('#btn-save-budget')?.addEventListener('click', async () => {
        const amt = parseFloat($('#budget-amount').value);
        if(!amt || amt <= 0){ toast('Enter a valid amount.','err'); return; }
        try{
          await api('/api/budget', {method:'POST', body:{ month: budgetSel.month, year: budgetSel.year, budgetAmount: amt }});
          toast('Budget saved.'); loadBudgetView();
        }catch(e){ toast(e.message,'err'); }
      });
      loadBudgetView();
      loadBudgetStatus();
    }
    async function loadBudgetView(){
      const card = $('#budget-view-card');
      try{
        const b = await api('/api/budget', {params:{month:budgetSel.month, year:budgetSel.year}});
        card.innerHTML = `<h3>Budget for ${MONTH_NAMES[budgetSel.month-1]} ${budgetSel.year}</h3>
          <div class="ledger-stat" style="margin-top:10px;"><div class="lbl">Amount</div><div class="val num">${money(b.budgetAmount)}</div>
          <div class="delta">Last updated ${fmtDateTime(b.updatedAt)}</div></div>`;
        $('#budget-amount') && ($('#budget-amount').value = b.budgetAmount);
      }catch(e){
        card.innerHTML = `<h3>Budget for ${MONTH_NAMES[budgetSel.month-1]} ${budgetSel.year}</h3><div class="empty-state"><div class="ic">◐</div><p>No budget set for this period yet.</p></div>`;
      }
    }
    async function loadBudgetStatus(){
      const card = $('#budget-status-card');
      try{
        const s = await api('/api/budget/status');
        card.innerHTML = s.budgetExist ? `<h3>Latest budget on file</h3>
          <div class="grid grid-3">
            <div class="ledger-stat"><div class="lbl">Amount</div><div class="val num">${money(s.lastBudgetAmount)}</div></div>
            <div class="ledger-stat"><div class="lbl">Month</div><div class="val" style="font-size:18px;">${MONTH_NAMES[s.lastBudgetMonth-1]}</div></div>
            <div class="ledger-stat"><div class="lbl">Year</div><div class="val" style="font-size:18px;">${s.lastBudgetYear}</div></div>
          </div>` : `<h3>Latest budget on file</h3><div class="empty-state"><p>No budgets created yet.</p></div>`;
      }catch(e){ card.innerHTML = `<h3>Latest budget on file</h3><div class="empty-state"><p>Couldn't load this.</p></div>`; }
    }
