/* ---------------- ROUTER ---------------- */
    function goPage(page, opts={}){
      state.page = page;
      $all('.nav-item').forEach(b => b.classList.toggle('active', b.dataset.page === page));
      $all('.bottom-nav button').forEach(b => b.classList.toggle('active', b.dataset.page === page));
      $('#page-title').textContent = PAGE_META[page].title;
      $('#page-sub').textContent = PAGE_META[page].sub;
      $('#sidebar').classList.remove('open');
      const renderers = {
        dashboard: renderDashboard, categories: renderCategories, records: () => renderRecords(opts),
        reports: renderReports, budget: renderBudget, notifications: renderNotifications, settings: renderSettings,
      };
      $('#content').innerHTML = '<div class="empty-state"><div class="ic">…</div><p>Loading…</p></div>';
      renderers[page]();
    }

/* ---------------- NAV WIRING ---------------- */
    $all('.nav-item, .bottom-nav button').forEach(btn => {
      btn.addEventListener('click', () => goPage(btn.dataset.page));
    });
    $('#btn-open-sidebar').addEventListener('click', () => $('#sidebar').classList.toggle('open'));

    /* ---------------- DARK MODE ---------------- */
    $('#btn-dark-toggle').addEventListener('click', () => {
      state.theme = state.theme === 'light' ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', state.theme);
    });

    /* ---------------- QUICK ADD ---------------- */
    $('#btn-quick-add').addEventListener('click', () => openExpenseModal());

/* ================= BOOT ================= */
    $('#api-base-input').value = state.apiBase;
    checkApiHealth();
