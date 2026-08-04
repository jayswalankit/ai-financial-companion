/* ================= SETTINGS ================= */
    async function renderSettings(){
      const el = $('#content');
      let profile;
      try{ profile = await api('/api/users/me'); }catch(e){ profile = state.user; }
      const prefs = state.preferences || {userMode:'NORMAL', notificationMode:'NORMAL'};
      el.innerHTML = `
        <div class="grid grid-2" style="align-items:start;">
          <div class="card">
            <h3>Profile</h3><div class="card-sub">Your account details</div>
            <div class="opt-row"><div><div class="opt-label">Username</div></div><div class="mono">${escapeHtml(profile.username)}</div></div>
            <div class="opt-row"><div><div class="opt-label">Email</div></div><div class="mono">${escapeHtml(profile.email)}</div></div>
            <div class="opt-row"><div><div class="opt-label">Appearance</div><div class="opt-desc">Switch between light and dark</div></div>
              <label class="switch"><input type="checkbox" id="toggle-dark" ${state.theme==='dark'?'checked':''}><span class="slider"></span></label>
            </div>
          </div>

          <div class="card">
            <h3>Preferences</h3><div class="card-sub">Mode and notification behaviour</div>
            <div class="field"><label>App mode <span id="pref-mode-active" class="badge badge-jade">Active: ${prefs.userMode === 'NORMAL' ? 'Default' : prefs.userMode === 'CUSTOM' ? 'Custom mode' : escapeHtml(prefs.userMode.charAt(0) + prefs.userMode.slice(1).toLowerCase())}</span></label>
              <select id="pref-mode">
                <option value="NORMAL" ${prefs.userMode==='NORMAL'?'selected':''}>Default</option>
                <option value="TRIP" ${prefs.userMode==='TRIP'?'selected':''}>Trip</option>
                <option value="MEDICAL" ${prefs.userMode==='MEDICAL'?'selected':''}>Medical</option>
                <option value="CUSTOM" ${prefs.userMode==='CUSTOM'?'selected':''}>Custom mode</option>
              </select>
              <div class="hint">Choose "Default" for everyday tracking, or "Custom mode" to use one of your own saved modes below.</div>
            </div>
            <div class="opt-row"><div><div class="opt-label">Notifications</div><div class="opt-desc">Silent mutes alerts, Normal shows them</div></div>
              <label class="switch"><input type="checkbox" id="toggle-notif" ${prefs.notificationMode==='NORMAL'?'checked':''}><span class="slider"></span></label>
            </div>
            <button class="btn btn-primary btn-sm" style="margin-top:14px;" id="btn-save-prefs">Save preferences</button>
          </div>
        </div>

        <div class="card" style="margin-top:16px;">
          <div class="toolbar" style="margin-bottom:6px;"><div><h3>Custom modes</h3><div class="card-sub">Saved notification presets you can switch on from "App mode" above</div></div>
            <div style="flex:1"></div><button class="btn btn-primary btn-sm" id="btn-add-mode">+ New custom mode</button></div>
          <div id="custom-modes-list"></div>
        </div>
      `;
      $('#toggle-dark').addEventListener('change', (e) => {
        state.theme = e.target.checked ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', state.theme);
      });
      $('#pref-mode').addEventListener('change', () => {
        const selectedMode = $('#pref-mode').value;
        const modeLabels = {NORMAL:'Default', TRIP:'Trip', MEDICAL:'Medical', CUSTOM:'Custom mode'};
        $('#pref-mode-active').textContent = `Active: ${modeLabels[selectedMode]}`;
        renderCustomModesList(selectedMode);
      });
      $('#btn-save-prefs').addEventListener('click', async () => {
        const userMode = $('#pref-mode').value;
        const notificationMode = $('#toggle-notif').checked ? 'NORMAL' : 'SILENT';
        try{
          state.preferences = await api('/api/preferences', {method:'PUT', body:{userMode, notificationMode}});
          toast('Preferences saved.');
          renderCustomModesList(userMode);
        }catch(e){ toast(e.message,'err'); }
      });
      $('#btn-add-mode').addEventListener('click', () => openCustomModeModal());
      renderCustomModesList();
    }
    async function renderCustomModesList(selectedMode = $('#pref-mode')?.value || state.preferences?.userMode){
      const wrap = $('#custom-modes-list');
      let modes, active;
      try{
        modes = await api('/api/custom-modes');
        active = await api('/api/custom-modes/active').catch(()=>null);
      }catch(e){ wrap.innerHTML = errorPanel(e.message, renderCustomModesList); return; }
      state.customModes = modes;
      if(!modes.length){ wrap.innerHTML = `<div class="empty-state"><div class="ic">⚙</div><p>No custom modes yet. Create one for situations like travel or a strict savings sprint.</p></div>`; return; }
      const customModeIsSelected = selectedMode === 'CUSTOM';
      wrap.innerHTML = modes.map(m => `
        <div class="opt-row">
          <div><div class="opt-label">${escapeHtml(m.modeName)} ${customModeIsSelected && active && active.modeId===m.id ? '<span class="badge badge-jade">Active</span>' : ''}</div>
            <div class="opt-desc">Notifications: ${m.notificationMode === 'NORMAL' ? 'Normal' : 'Silent'}</div></div>
          <div class="row-actions">
            ${!(customModeIsSelected && active && active.modeId===m.id) ? `<button class="btn btn-ghost btn-sm" data-activate="${m.id}">Activate</button>` : ''}
            <button class="btn-icon" data-edit-mode="${m.id}" title="Edit">✎</button>
            <button class="btn-icon" data-del-mode="${m.id}" title="Delete">🗑</button>
          </div>
        </div>
      `).join('');
      $all('[data-activate]', wrap).forEach(b => b.addEventListener('click', async () => {
        try{
          await api(`/api/custom-modes/${b.dataset.activate}/activate`, {method:'PUT'});
          state.preferences = {...(state.preferences || {}), userMode:'CUSTOM'};
          toast('Custom mode activated.');
          renderSettings();
        }
        catch(e){ toast(e.message,'err'); }
      }));
      $all('[data-edit-mode]', wrap).forEach(b => b.addEventListener('click', () => openCustomModeModal(modes.find(m=>String(m.id)===b.dataset.editMode))));
      $all('[data-del-mode]', wrap).forEach(b => b.addEventListener('click', async () => {
        try{ await api('/api/custom-modes/' + b.dataset.delMode, {method:'DELETE'}); toast('Custom mode deleted.'); renderCustomModesList(); }
        catch(e){ toast(e.message,'err'); }
      }));
    }
    function openCustomModeModal(mode=null){
      const isEdit = !!mode;
      openModal(`
        <div class="modal-head"><h3>${isEdit?'Edit custom mode':'New custom mode'}</h3><button class="modal-close" data-close>✕</button></div>
        <div class="modal-body">
          <div class="field"><label>Mode name</label><input type="text" id="mode-name" value="${isEdit?escapeHtml(mode.modeName):''}" placeholder="e.g. Weekend trip"></div>
          <div class="field"><label>Notification style</label><select id="mode-notif">
            <option value="NORMAL" ${isEdit && mode.notificationMode==='NORMAL'?'selected':''}>Normal</option>
            <option value="SILENT" ${isEdit && mode.notificationMode==='SILENT'?'selected':''}>Silent</option>
          </select></div>
        </div>
        <div class="modal-foot"><button class="btn btn-ghost" data-close>Cancel</button><button class="btn btn-primary" id="btn-save-mode">${isEdit?'Save changes':'Create mode'}</button></div>
      `);
      $('#btn-save-mode').addEventListener('click', async () => {
        const modeName = $('#mode-name').value.trim();
        const notificationMode = $('#mode-notif').value;
        if(!modeName){ toast('Name is required.','err'); return; }
        try{
          if(isEdit) await api('/api/custom-modes/' + mode.id, {method:'PUT', body:{modeName, notificationMode}});
          else await api('/api/custom-modes', {method:'POST', body:{modeName, notificationMode}});
          toast('Custom mode saved.'); closeModal(); renderCustomModesList();
        }catch(e){ toast(e.message,'err'); }
      });
    }
