/* ================= CATEGORIES ================= */
    async function renderCategories(){
      const el = $('#content');
      let highlights = [];
      try{
        [state.categories, highlights] = await Promise.all([
          api('/api/categories'),
          api('/api/reports/category-highlights').catch(() => [])
        ]);
      }catch(e){ el.innerHTML = errorPanel(e.message, renderCategories); return; }
      el.innerHTML = `
        <div class="toolbar"><div style="flex:1"></div>
          <button class="btn btn-primary btn-sm" id="btn-add-category">+ New category</button>
        </div>
        ${highlights && highlights.length ? `<div class="grid grid-4" style="margin-bottom:16px;">${renderCategoryHighlights(highlights)}</div>` : ''}
        ${state.categories.length ? `<div class="cat-grid" id="cat-grid"></div>` :
          `<div class="empty-state"><div class="ic">▤</div><h4>No categories yet</h4><p>Create categories like Food, Travel or Rent to start organising expenses.</p>
           <button class="btn btn-primary btn-sm" id="btn-add-category-empty">+ New category</button></div>`}
      `;
      $('#btn-add-category')?.addEventListener('click', () => openCategoryModal());
      $('#btn-add-category-empty')?.addEventListener('click', () => openCategoryModal());
      const grid = $('#cat-grid');
      if(grid){
        grid.innerHTML = state.categories.map(c => `
          <div class="cat-card" data-id="${c.id}">
            <div class="top">
              <div class="cat-ic">${c.type === 'INCOME' ? '↑' : '↓'}</div>
              <button class="menu-dots" data-menu="${c.id}">⋯</button>
            </div>
            <div class="name">${escapeHtml(c.name)}</div>
            <div class="type">${c.type === 'INCOME' ? 'Income' : 'Expense'}${c.predefined ? ' · Predefined' : ''}</div>
          </div>
        `).join('');
        $all('.cat-card', grid).forEach(card => {
          card.addEventListener('click', (e) => {
            if(e.target.closest('[data-menu]')) return;
            const cat = state.categories.find(c => String(c.id) === card.dataset.id);
            state.recordsFilter = { keyword:'', categoryId:String(cat.id), startDate:'', endDate:'', page:0, size:10, categoryLock:cat, dateFromSearch:false };
            goPage('records');
          });
        });
        $all('[data-menu]', grid).forEach(btn => {
          btn.addEventListener('click', (e) => {
            e.stopPropagation();
            openCatMenu(btn, state.categories.find(c => String(c.id) === btn.dataset.menu));
          });
        });
      }
    }
    function renderCategoryHighlights(items){
      return items.map(item => `
        <div class="ledger-stat">
          <div class="lbl">${escapeHtml(item.label)}</div>
          <div class="val" style="font-size:18px;">${escapeHtml(item.categoryName || '—')}</div>
          <div class="delta">${money(item.totalAmount || 0)}</div>
        </div>
      `).join('');
    }
    function openCatMenu(anchor, cat){
      $all('.cat-inline-menu').forEach(m => m.remove());
      const menu = document.createElement('div');
      menu.className = 'cat-inline-menu card';
      menu.style.cssText = 'position:absolute; z-index:30; padding:6px; width:140px;';
      const r = anchor.getBoundingClientRect();
      menu.style.top = (r.bottom + window.scrollY + 4) + 'px';
      menu.style.left = (r.left + window.scrollX - 90) + 'px';
      menu.innerHTML = `
        <button class="nav-item" style="padding:8px 10px;" data-act="edit">Edit</button>
        <button class="nav-item" style="padding:8px 10px; color:var(--brick);" data-act="del">Delete</button>
      `;
      document.body.appendChild(menu);
      menu.querySelector('[data-act="edit"]').addEventListener('click', () => { menu.remove(); openCategoryModal(cat); });
      menu.querySelector('[data-act="del"]').addEventListener('click', () => { menu.remove(); confirmDeleteCategory(cat); });
      setTimeout(() => document.addEventListener('click', function h(ev){ if(!menu.contains(ev.target)){ menu.remove(); document.removeEventListener('click',h);} }), 0);
    }
    function confirmDeleteCategory(cat){
      openModal(`
        <div class="modal-head"><h3>Delete category</h3><button class="modal-close" data-close>✕</button></div>
        <div class="modal-body"><p>Delete <strong>${escapeHtml(cat.name)}</strong>? Expenses in this category will keep the old category reference on the server, but you'll no longer be able to add new ones here.</p></div>
        <div class="modal-foot"><button class="btn btn-ghost" data-close>Cancel</button><button class="btn btn-danger" id="btn-confirm-del-cat">Delete</button></div>
      `);
      $('#btn-confirm-del-cat').addEventListener('click', async () => {
        try{ await api('/api/categories/' + cat.id, {method:'DELETE'}); toast('Category deleted.'); closeModal(); renderCategories(); }
        catch(e){ toast(e.message,'err'); }
      });
    }
    function openCategoryModal(cat=null){
      const isEdit = !!cat;
      const isAdmin = state.user?.role === 'ADMIN';
      openModal(`
        <div class="modal-head"><h3>${isEdit?'Edit category':'New category'}</h3><button class="modal-close" data-close>✕</button></div>
        <div class="modal-body">
          <div class="field"><label>Name</label><input type="text" id="cat-name" value="${isEdit?escapeHtml(cat.name):''}" placeholder="e.g. Groceries"></div>
          ${!isEdit ? `<div class="field"><label>Type</label>
            <select id="cat-type"><option value="EXPENSE">Expense</option><option value="INCOME">Income</option></select>
          </div>${isAdmin ? `<div class="field"><label>Visibility</label>
            <select id="cat-predefined"><option value="false">Personal — only you</option><option value="true">All users — predefined category</option></select>
          </div>` : ''}` : `<div class="field"><label>Type</label><input type="text" value="${cat.type==='INCOME'?'Income':'Expense'}" disabled></div>`}
        </div>
        <div class="modal-foot"><button class="btn btn-ghost" data-close>Cancel</button><button class="btn btn-primary" id="btn-save-cat">${isEdit?'Save changes':'Create category'}</button></div>
      `);
      $('#btn-save-cat').addEventListener('click', async () => {
        const name = $('#cat-name').value.trim();
        if(!name){ toast('Name is required.','err'); return; }
        try{
          if(isEdit){ await api('/api/categories/' + cat.id, {method:'PUT', body:{name}}); toast('Category updated.'); }
          else{ const type = $('#cat-type').value; const predefined = $('#cat-predefined')?.value === 'true'; await api('/api/categories', {method:'POST', body:{name, type, predefined}}); toast(predefined ? 'Category created for all users.' : 'Personal category created.'); }
          closeModal(); renderCategories();
        }catch(e){ toast(e.message,'err'); }
      });
    }
