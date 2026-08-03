/* ================= NOTIFICATIONS ================= */
    async function renderNotifications(){
      const el = $('#content');
      let list, daily, monthly;
      try{
        [list, daily, monthly] = await Promise.all([
          api('/api/notifications').catch(()=>[]),
          api('/api/notifications/daily-summary').catch(()=>null),
          api('/api/notifications/monthly-summary').catch(()=>null),
        ]);
      }catch(e){ el.innerHTML = errorPanel(e.message, renderNotifications); return; }
      state.notificationsShowAll = state.notificationsShowAll || false;
      const todayStr = toISO(new Date());
      const todaysList = (list||[]).filter(n => n.sentAt && n.sentAt.slice(0,10) === todayStr);
      const statusBadgeClass = (s) => (s==='CRITICAL' ? 'badge-brick' : s==='WARNING' ? 'badge-amber' : s==='BUDGET_NOT_SET' ? 'badge-muted' : 'badge-jade');
      el.innerHTML = `
        <div class="grid grid-2" style="margin-bottom:16px; align-items:start;">
          <div class="card"><h3>Today</h3><div class="card-sub">${daily?fmtDate(daily.summaryDate):''}</div>
            ${daily ? `<div class="ledger-stat"><div class="lbl">Spent today</div><div class="val num">${money(daily.todaySpent)}</div>
              <div class="delta">${daily.expenseCount} transaction${daily.expenseCount===1?'':'s'} · top: ${escapeHtml(daily.todayTopCategory||'—')}</div></div>
              <p style="font-size:12.5px; color:var(--text-muted); margin-top:10px;">Recommended daily limit: <span class="mono">${money(daily.recommendedDailyLimit)}</span></p>`
              : `<div class="empty-state"><p>No activity today yet.</p></div>`}
          </div>
          <div class="card"><h3>This month</h3>
            ${monthly ? `<div class="ledger-stat"><div class="lbl">Spent</div><div class="val num">${money(monthly.totalSpent)}</div>
              <div class="delta">Remaining ${money(monthly.remainingBudget)}</div></div>
              <div style="margin-top:10px; display:flex; gap:8px; flex-wrap:wrap;">
                <span class="badge ${statusBadgeClass(monthly.budgetStatus)}">${escapeHtml(monthly.budgetStatus||'')}</span>
                <span class="badge badge-muted">Top category: ${escapeHtml(monthly.topCategory||'—')}</span>
              </div>` : `<div class="empty-state"><p>No summary available.</p></div>`}
          </div>
        </div>
        <div class="card">
          <div class="toolbar" style="margin-bottom:2px;">
            <div><h3>${state.notificationsShowAll ? 'All notifications' : "Today's notifications"}</h3>
              <div class="card-sub">${state.notificationsShowAll ? 'Every notification on record' : fmtDate(todayStr) + ' only'}</div></div>
            <div style="flex:1"></div>
            <button class="btn btn-ghost btn-sm" id="btn-toggle-notif-scope">${state.notificationsShowAll ? 'Show today only' : 'Show all'}</button>
          </div>
          ${(() => { const shown = state.notificationsShowAll ? list : todaysList;
            return shown && shown.length ? shown.map(n => `
            <div class="notif-item ${sevClass(n.severity)}">
              <div class="notif-ic">${sevIcon(n.severity)}</div>
              <div><div class="notif-msg">${escapeHtml(n.message)}</div><div class="notif-time">${fmtDateTime(n.sentAt)}</div></div>
            </div>`).join('') : `<div class="empty-state"><div class="ic">◔</div><h4>No notifications ${state.notificationsShowAll?'yet':'today'}</h4><p>Visit Reports to generate fresh insights — they'll show up here.</p></div>`; })()}
        </div>
      `;
      $('#btn-toggle-notif-scope').addEventListener('click', () => { state.notificationsShowAll = !state.notificationsShowAll; renderNotifications(); });
    }
