/* ================= REPORTS ================= */
    function destroyChart(id){ if(state.charts[id]){ state.charts[id].destroy(); delete state.charts[id]; } }
    function mkChart(id, config){
      destroyChart(id);
      const ctx = document.getElementById(id);
      if(!ctx) return;
      state.charts[id] = new Chart(ctx, config);
    }
    function chartTextColor(){ return getComputedStyle(document.documentElement).getPropertyValue('--text').trim() || '#1b2027'; }
    function chartGridColor(){ return getComputedStyle(document.documentElement).getPropertyValue('--line').trim() || '#ddd'; }

    function renderReports(){
      const el = $('#content');
      el.innerHTML = `
        <div class="toolbar" style="justify-content:space-between;">
          <div class="tabs" id="report-tabs">
            <button class="tab-btn" data-tab="weekly">Weekly</button>
            <button class="tab-btn" data-tab="monthly">Monthly</button>
            <button class="tab-btn" data-tab="custom">Custom range</button>
          </div>
          <div class="search-box" style="max-width:280px;">
            <span>⌕</span>
            <input type="text" id="rep-search" placeholder="Search date (4/8/2026) or name" value="${escapeHtml(state.reportsSearch)}">
          </div>
        </div>
        <div id="custom-range-row" class="toolbar hidden">
          <input type="date" id="rep-start"><span style="color:var(--text-muted); font-size:13px;">to</span><input type="date" id="rep-end">
          <button class="btn btn-primary btn-sm" id="btn-apply-range">Apply range</button>
        </div>
        <div class="toolbar">
          <div class="view-switch" id="view-switch">
            <button class="view-chip" data-view="bar">📊 Bar graph</button>
            <button class="view-chip" data-view="donut">🍩 Donut chart</button>
            <button class="view-chip" data-view="trend">📈 Trend line</button>
            <button class="view-chip" data-view="stats">🧮 Stats cards</button>
            <button class="view-chip" data-view="table">📋 Table view</button>
          </div>
        </div>
        <div id="reports-body"><div class="empty-state"><p>Loading…</p></div></div>
      `;
      $all('#report-tabs .tab-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.tab === state.reportsTab);
        b.addEventListener('click', () => { state.reportsTab = b.dataset.tab; renderReports(); });
      });
      $('#custom-range-row').classList.toggle('hidden', state.reportsTab !== 'custom');
      if(state.reportsTab === 'custom'){
        $('#rep-start').value = state.reportsCustomRange.start;
        $('#rep-end').value = state.reportsCustomRange.end;
        $('#btn-apply-range').addEventListener('click', () => {
          state.reportsCustomRange.start = $('#rep-start').value;
          state.reportsCustomRange.end = $('#rep-end').value;
          loadReportsBody();
        });
      }
      $all('#view-switch .view-chip').forEach(b => {
        b.classList.toggle('active', b.dataset.view === state.reportsView);
        b.addEventListener('click', () => { state.reportsView = b.dataset.view; loadReportsBody(); });
      });
      let t; $('#rep-search').addEventListener('input', (e) => {
        clearTimeout(t); t = setTimeout(() => { state.reportsSearch = e.target.value.trim(); loadReportsBody(); }, 400);
      });
      loadReportsBody();
    }

    async function loadReportsBody(){
      const body = $('#reports-body');
      if(!body) return;
      body.innerHTML = `<div class="empty-state"><p>Crunching numbers…</p></div>`;
      const tab = state.reportsTab;
      try{
        if(tab === 'weekly') await renderWeeklyReport(body);
        else if(tab === 'monthly') await renderMonthlyReport(body);
        else await renderCustomReport(body);
      }catch(e){
        body.innerHTML = errorPanel(e.message, loadReportsBody);
      }
    }

    async function renderWeeklyReport(body){
      const view = state.reportsView;
      if(view === 'bar' || view === 'trend'){
        const trend = await api('/api/reports/weekly-trend');
        body.innerHTML = `<div class="card"><h3>Weekly spending trend</h3><div class="card-sub">Total expense by week</div><div class="chart-box"><canvas id="chart-weekly"></canvas></div></div>`;
        mkChart('chart-weekly', {
          type: view === 'bar' ? 'bar' : 'line',
          data: { labels: trend.map(t=>t.weekLabel), datasets: [{ label:'Weekly expense', data: trend.map(t=>Number(t.totalExpense||0)),
            backgroundColor: view==='bar' ? CHART_COLORS[0] : 'rgba(31,122,92,.15)', borderColor: CHART_COLORS[0], borderWidth:2, tension:.35, fill: view!=='bar' }] },
          options: chartOpts()
        });
      } else if(view === 'donut'){
        const cats = await api('/api/reports/top-categories');
        renderDonut(body, cats, 'All-time category breakdown');
      } else if(view === 'stats'){
        const [health, patterns] = await Promise.all([api('/api/reports/financialHealth'), api('/api/reports/spendingPatterns')]);
        body.innerHTML = statsGrid(health, patterns);
      } else {
        await renderTableView(body, null, null);
      }
    }

    async function renderMonthlyReport(body){
      const view = state.reportsView;
      if(view === 'bar'){
        const cmp = await api('/api/reports/monthlyComparison');
        body.innerHTML = `<div class="card"><h3>This month vs last month</h3><div class="card-sub">${escapeHtml(cmp.trend||'')} · ${cmp.percentageChange!=null?Number(cmp.percentageChange).toFixed(1)+'% change':''}</div><div class="chart-box"><canvas id="chart-monthly-bar"></canvas></div></div>`;
        mkChart('chart-monthly-bar', { type:'bar', data:{ labels:['Previous month','Current month'], datasets:[{ data:[Number(cmp.previousMonthExpense||0), Number(cmp.currentMonthExpense||0)], backgroundColor:[CHART_COLORS[3], CHART_COLORS[0]] }] }, options: chartOpts() });
      } else if(view === 'trend'){
        const growth = await api('/api/reports/categoryGrowth');
        body.innerHTML = `<div class="card"><h3>Category growth — current vs previous month</h3><div class="chart-box"><canvas id="chart-monthly-trend"></canvas></div></div>`;
        mkChart('chart-monthly-trend', { type:'line', data:{ labels: growth.map(g=>g.categoryName), datasets:[
          { label:'Previous month', data: growth.map(g=>Number(g.previousMonthAmount||0)), borderColor: CHART_COLORS[3], backgroundColor:'transparent', tension:.3 },
          { label:'Current month', data: growth.map(g=>Number(g.currentMonthAmount||0)), borderColor: CHART_COLORS[0], backgroundColor:'transparent', tension:.3 },
        ]}, options: chartOpts() });
      } else if(view === 'donut'){
        const cats = await api('/api/reports/top-categories');
        renderDonut(body, cats, 'This period category breakdown');
      } else if(view === 'stats'){
        const [health, patterns, cmp] = await Promise.all([api('/api/reports/financialHealth'), api('/api/reports/spendingPatterns'), api('/api/reports/monthlyComparison')]);
        body.innerHTML = statsGrid(health, patterns) + `
          <div class="card" style="margin-top:16px;"><h3>Month over month</h3>
            <div class="grid grid-3">
              <div class="ledger-stat"><div class="lbl">Previous month</div><div class="val num">${money(cmp.previousMonthExpense)}</div></div>
              <div class="ledger-stat"><div class="lbl">Current month</div><div class="val num">${money(cmp.currentMonthExpense)}</div></div>
              <div class="ledger-stat ${Number(cmp.percentageChange)>0?'brick':'amber'}"><div class="lbl">Change</div><div class="val num">${Number(cmp.percentageChange||0).toFixed(1)}%</div><div class="delta">${escapeHtml(cmp.trend||'')}</div></div>
            </div>
          </div>`;
      } else {
        const growth = await api('/api/reports/categoryGrowth');
        body.innerHTML = `<div class="card" style="margin-bottom:16px;"><h3>Category growth</h3>
          <div class="table-wrap"><table><thead><tr><th>Category</th><th>Current month</th><th>Previous month</th><th>Growth</th><th>Trend</th></tr></thead>
          <tbody>${growth.map(g=>`<tr><td>${escapeHtml(g.categoryName)}</td><td class="mono">${money(g.currentMonthAmount)}</td><td class="mono">${money(g.previousMonthAmount)}</td>
          <td class="mono">${Number(g.growthPercentage||0).toFixed(1)}%</td><td><span class="badge ${g.trend==='INCREASING'?'badge-brick':g.trend==='DECREASING'?'badge-jade':'badge-muted'}">${escapeHtml(g.trend||'—')}</span></td></tr>`).join('')}</tbody>
          </table></div></div><div id="reports-table-inner"></div>`;
        await renderTableView($('#reports-table-inner'), null, null, true);
      }
    }

    async function renderCustomReport(body){
      const {start, end} = state.reportsCustomRange;
      if(!start || !end){
        body.innerHTML = `<div class="empty-state"><div class="ic">▲</div><h4>Pick a date range</h4><p>Choose a start and end date above, then click Apply range.</p></div>`;
        return;
      }
      const expenses = await fetchAllExpensesInRange(start, end);
      const filtered = filterBySearch(expenses, state.reportsSearch);
      const view = state.reportsView;
      if(view === 'bar' || view === 'trend'){
        const byDay = {};
        filtered.forEach(x => { byDay[x.expenseDate] = (byDay[x.expenseDate]||0) + Number(x.amount||0); });
        const days = Object.keys(byDay).sort();
        body.innerHTML = `<div class="card"><h3>Daily spending — ${fmtDate(start)} to ${fmtDate(end)}</h3><div class="chart-box"><canvas id="chart-custom"></canvas></div></div>`;
        mkChart('chart-custom', { type: view==='bar'?'bar':'line', data:{ labels: days.map(d=>fmtDate(d)), datasets:[{ label:'Daily expense', data: days.map(d=>byDay[d]),
          backgroundColor: view==='bar'?CHART_COLORS[0]:'rgba(31,122,92,.15)', borderColor:CHART_COLORS[0], borderWidth:2, tension:.35, fill:view!=='bar' }] }, options: chartOpts() });
      } else if(view === 'donut'){
        const byCat = {};
        filtered.forEach(x => { byCat[x.categoryName] = (byCat[x.categoryName]||0) + Number(x.amount||0); });
        const cats = Object.entries(byCat).map(([categoryName,totalAmount]) => ({categoryName, totalAmount}));
        renderDonut(body, cats, `Category breakdown — ${fmtDate(start)} to ${fmtDate(end)}`);
      } else if(view === 'stats'){
        const total = filtered.reduce((s,x)=>s+Number(x.amount||0),0);
        const days = Math.max(1, (new Date(end) - new Date(start)) / 86400000 + 1);
        const byCat = {}; filtered.forEach(x => { byCat[x.categoryName] = (byCat[x.categoryName]||0) + Number(x.amount||0); });
        const topCat = Object.entries(byCat).sort((a,b)=>b[1]-a[1])[0];
        const topExp = filtered.slice().sort((a,b)=>Number(b.amount)-Number(a.amount))[0];
        body.innerHTML = `<div class="grid grid-4">
          <div class="ledger-stat"><div class="lbl">Total spent</div><div class="val num">${money(total)}</div><div class="delta">${filtered.length} transactions</div></div>
          <div class="ledger-stat amber"><div class="lbl">Average per day</div><div class="val num">${money(total/days)}</div></div>
          <div class="ledger-stat"><div class="lbl">Top category</div><div class="val" style="font-size:18px;">${topCat?escapeHtml(topCat[0]):'—'}</div><div class="delta">${topCat?money(topCat[1]):''}</div></div>
          <div class="ledger-stat brick"><div class="lbl">Highest single expense</div><div class="val num">${topExp?money(topExp.amount):'—'}</div><div class="delta">${topExp?escapeHtml(topExp.title):''}</div></div>
        </div>`;
      } else {
        body.innerHTML = renderExpenseTableHtml(filtered);
      }
    }
    async function fetchAllExpensesInRange(start, end){
      let page = 0; const all = []; let totalPages = 1;
      do{
        const res = await api('/api/expenses', {params:{startDate:start, endDate:end, page, size:100, sort:'expenseDate,desc'}});
        all.push(...(res.content||[]));
        totalPages = res.totalPages || 1; page++;
      }while(page < totalPages && page < 20);
      return all;
    }
    function filterBySearch(expenses, search){
      if(!search) return expenses;
      const asDate = parseFlexibleDate(search);
      if(asDate) return expenses.filter(x => x.expenseDate === asDate);
      const q = search.toLowerCase();
      return expenses.filter(x => (x.title||'').toLowerCase().includes(q) || (x.description||'').toLowerCase().includes(q));
    }
    function renderExpenseTableHtml(items){
      if(!items.length) return `<div class="empty-state"><div class="ic">📋</div><h4>No matching records</h4><p>Try a different search term or range.</p></div>`;
      return `<div class="table-wrap"><table><thead><tr><th>Date</th><th>Title</th><th>Category</th><th style="text-align:right;">Amount</th></tr></thead>
        <tbody>${items.map(x=>`<tr><td class="mono">${fmtDate(x.expenseDate)}</td><td>${escapeHtml(x.title)}</td><td><span class="badge badge-muted">${escapeHtml(x.categoryName)}</span></td><td class="mono" style="text-align:right;">${money(x.amount)}</td></tr>`).join('')}</tbody>
      </table></div>`;
    }
    async function renderTableView(container, _a, _b, append=false){
      const params = { keyword: state.reportsSearch && !parseFlexibleDate(state.reportsSearch) ? state.reportsSearch : null,
        startDate: parseFlexibleDate(state.reportsSearch) || null, endDate: parseFlexibleDate(state.reportsSearch) || null,
        page:0, size:15, sort:'expenseDate,desc' };
      const res = await api('/api/expenses', {params});
      const html = `<div class="card"><h3>Recent matching records</h3><div class="card-sub">Showing up to 15 most recent</div>${renderExpenseTableHtml(res.content||[])}</div>`;
      if(append) container.innerHTML = html; else container.innerHTML = html;
    }
    function renderDonut(body, cats, label){
      if(!cats || !cats.length){ body.innerHTML = `<div class="empty-state"><div class="ic">🍩</div><h4>Nothing to chart yet</h4><p>Add some expenses first.</p></div>`; return; }
      body.innerHTML = `<div class="card"><h3>${escapeHtml(label)}</h3><div class="chart-box"><canvas id="chart-donut"></canvas></div></div>`;
      mkChart('chart-donut', { type:'doughnut', data:{ labels: cats.map(c=>c.categoryName), datasets:[{ data: cats.map(c=>Number(c.totalAmount||0)), backgroundColor: CHART_COLORS, borderWidth:2, borderColor: getComputedStyle(document.documentElement).getPropertyValue('--bg-card') }] },
        options:{ responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ color:chartTextColor() } } } } });
    }
    function statsGrid(health, patterns){
      return `<div class="grid grid-4">
        <div class="ledger-stat"><div class="lbl">Total spent</div><div class="val num">${money(health.totalSpent)}</div></div>
        <div class="ledger-stat amber"><div class="lbl">Budget usage</div><div class="val num">${Number(health.budgetUsagePercentage||0).toFixed(0)}%</div><div class="delta">${escapeHtml(health.financialStatus||'')}</div></div>
        <div class="ledger-stat"><div class="lbl">Remaining budget</div><div class="val num">${money(health.remainingBudget)}</div></div>
        <div class="ledger-stat brick"><div class="lbl">Highest single expense</div><div class="val num">${money(patterns.highestSingleExpense)}</div><div class="delta">${fmtDate(patterns.highestExpenseDate)}</div></div>
        <div class="ledger-stat"><div class="lbl">All-time top category</div><div class="val" style="font-size:17px;">${escapeHtml(patterns.allTimeHighestSpendingCategory||'—')}</div><div class="delta">${money(patterns.allTimeHighestSpendingAmount)}</div></div>
        <div class="ledger-stat"><div class="lbl">This month's top category</div><div class="val" style="font-size:17px;">${escapeHtml(patterns.currentMonthHighestCategory||'—')}</div><div class="delta">${money(patterns.currentMonthHighestCategoryAmount)}</div></div>
        <div class="ledger-stat amber"><div class="lbl">Average daily spend</div><div class="val num">${money(patterns.averageDailyExpenses)}</div></div>
      </div>`;
    }
    function chartOpts(){
      return { responsive:true, maintainAspectRatio:false,
        plugins:{ legend:{ labels:{ color: chartTextColor() } } },
        scales:{ x:{ ticks:{ color: chartTextColor() }, grid:{ color: chartGridColor() } }, y:{ ticks:{ color: chartTextColor() }, grid:{ color: chartGridColor() } } } };
    }
