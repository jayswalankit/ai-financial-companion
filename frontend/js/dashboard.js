/* ================= DASHBOARD ================= */
    async function renderDashboard(forceInsights=false){
      const el = $('#content');
      let summary, budgetStatus, insights, topCats;
      try{
        // NOTE: /api/reports/insights has a write side-effect on the backend (it logs a
        // notification row on every call). To avoid flooding the Notifications page with
        // duplicates, we only fetch it once per session unless the user explicitly refreshes.
        const insightsPromise = (state.insightsCache && !forceInsights)
          ? Promise.resolve(state.insightsCache)
          : api('/api/reports/insights').catch(()=>[]).then(r => { state.insightsCache = r; return r; });
        [summary, budgetStatus, insights, topCats] = await Promise.all([
          api('/api/reports/dashboard'),
          api('/api/reports/budget-status').catch(()=>null),
          insightsPromise,
          api('/api/reports/top-categories').catch(()=>[]),
        ]);
      }catch(e){
        el.innerHTML = errorPanel(e.message, renderDashboard);
        return;
      }
      const momUp = Number(summary.monthOverMonthDifference) > 0;
      el.innerHTML = `
        <div class="grid grid-4" style="margin-bottom:20px;">
          <div class="ledger-stat">
            <div class="lbl">Current month</div>
            <div class="val num">${money(summary.currentMonthExpense)}</div>
            <div class="delta">Last month ${money(summary.previousMonthExpense)}</div>
            <div class="delta ${momUp?'up':'down'}">${momUp?'▲':'▼'} ${money(Math.abs(summary.monthOverMonthDifference||0))} vs last month</div>
          </div>
          <div class="ledger-stat amber">
            <div class="lbl">Average expense</div>
            <div class="val num">${money(summary.averageExpense)}</div>
            <div class="delta">per day this month</div>
          </div>
          <div class="ledger-stat amber">
            <div class="lbl">Daily limit</div>
            <div class="val num">${money(budgetStatus?.recommendedLimit || 0)}</div>
            <div class="delta">Based on current budget</div>
          </div>
          <div class="ledger-stat">
            <div class="lbl">This year</div>
            <div class="val num">${money(summary.thisYearExpense)}</div>
            <div class="delta">Jan – Dec</div>
          </div>
        </div>

        <div class="grid grid-2" style="align-items:start;">
          <div class="card">
            <h3>Budget status</h3>
            <div class="card-sub">This month's limit and how you're tracking</div>
            ${budgetStatus ? renderBudgetStatusBlock(budgetStatus) : `<div class="empty-state"><div class="ic">◐</div><h4>No budget set yet</h4><p>Set a monthly budget to see how your spending compares.</p><button class="btn btn-primary btn-sm" data-page="budget">Set a budget</button></div>`}
          </div>
          <div class="card">
            <h3>Top categories</h3>
            <div class="card-sub">This month’s top categories</div>
            ${topCats && topCats.length ? renderTopCatsList(topCats) : `<div class="empty-state"><div class="ic">▤</div><h4>No expenses yet</h4><p>Add your first expense to see a breakdown.</p></div>`}
          </div>
        </div>

        <div class="card" style="margin-top:16px;">
          <div class="toolbar" style="margin-bottom:2px;">
            <div><h3>Insights</h3><div class="card-sub">Quick observations about your spending</div></div>
            <div style="flex:1"></div>
            <button class="btn btn-ghost btn-sm" id="btn-refresh-insights" title="Re-checking creates a new notification on the server, so this is manual rather than automatic">↻ Refresh</button>
          </div>
          ${insights && insights.length ? insights.map(i => `
            <div class="notif-item ${sevClass(i.severity)}">
              <div class="notif-ic">${sevIcon(i.severity)}</div>
              <div><div class="notif-msg">${escapeHtml(i.message)}</div></div>
            </div>`).join('') : `<div class="empty-state"><div class="ic">◔</div><p>No insights yet — add a few expenses first.</p></div>`}
        </div>
      `;
      $all('[data-page]', el).forEach(b => b.addEventListener('click', () => goPage(b.dataset.page)));
      $('#btn-refresh-insights').addEventListener('click', () => renderDashboard(true));
    }
    function sevClass(sev){ sev=(sev||'').toUpperCase(); return sev==='CRITICAL'?'crit':sev==='WARNING'?'warn':'info'; }
    function sevIcon(sev){ sev=(sev||'').toUpperCase(); return sev==='CRITICAL'?'⚠':sev==='WARNING'?'!':'i'; }
    function renderBudgetStatusBlock(b){
      const pct = b.monthlyBudget && Number(b.monthlyBudget) > 0 ? Math.min(100, (Number(b.currentSpent||0)/Number(b.monthlyBudget))*100) : 0;
      const over = Number(b.currentSpent||0) > Number(b.monthlyBudget||0);
      return `
        <div style="margin-bottom:10px; display:flex; justify-content:space-between; font-size:13px;">
          <span class="mono">${money(b.currentSpent)} spent</span><span class="mono" style="color:var(--text-muted)">of ${money(b.monthlyBudget)}</span>
        </div>
        <div style="height:8px; background:var(--bg-sunk); border-radius:6px; overflow:hidden; margin-bottom:12px;">
          <div style="height:100%; width:${pct}%; background:${over?'var(--brick)':'var(--jade)'};"></div>
        </div>
        <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:10px;">
          <span class="badge ${over?'badge-brick':'badge-jade'}">${escapeHtml(b.status||(over?'Over budget':'On track'))}</span>
          <span class="badge badge-muted">Remaining ${money(b.remainingBudget)}</span>
          ${b.recommendedLimit ? `<span class="badge badge-amber">Daily limit ~${money(b.recommendedLimit)}</span>` : ''}
        </div>
        ${b.advice ? `<p style="font-size:13px; color:var(--text-muted); line-height:1.5;">${escapeHtml(b.advice)}</p>` : ''}
      `;
    }
    function renderTopCatsList(cats){
      const total = cats.reduce((s,c)=>s+Number(c.totalAmount||0),0) || 1;
      return cats.slice(0,6).map(c => {
        const pct = Math.round((Number(c.totalAmount||0)/total)*100);
        return `<div style="margin-bottom:12px;">
          <div style="display:flex; justify-content:space-between; font-size:13px; margin-bottom:5px;">
            <span>${escapeHtml(c.categoryName)}</span><span class="mono">${money(c.totalAmount)}</span>
          </div>
          <div style="height:6px; background:var(--bg-sunk); border-radius:6px; overflow:hidden;">
            <div style="height:100%; width:${pct}%; background:var(--jade);"></div>
          </div>
        </div>`;
      }).join('');
    }
    function errorPanel(msg, retryFn){
      return `<div class="empty-state"><div class="ic">⚠</div><h4>Couldn't load this</h4><p>${escapeHtml(msg)}</p>
        <button class="btn btn-ghost btn-sm" onclick="(${retryFn.name})()">Try again</button></div>`;
    }
