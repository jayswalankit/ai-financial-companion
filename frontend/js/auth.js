/* ---------------- AUTH SCREEN ---------------- */
    function showAuthView(name){
      $all('.auth-view').forEach(v => v.classList.add('hidden'));
      $('#view-' + name).classList.remove('hidden');
    }
    function authMsg(elId, msg, type='error'){
      const el = $(elId);
      el.innerHTML = msg ? `<div class="${type==='error'?'auth-error':'auth-success'}">${escapeHtml(msg)}</div>` : '';
    }

    $all('[data-go]').forEach(btn => btn.addEventListener('click', () => showAuthView(btn.dataset.go)));

    $('#btn-check-api').addEventListener('click', checkApiHealth);
    $('#api-base-input').addEventListener('change', e => { state.apiBase = e.target.value.trim(); checkApiHealth(); });
    async function checkApiHealth(){
      const dot = $('#api-dot');
      try{
        const r = await fetch(state.apiBase.replace(/\/$/,'') + '/api/health');
        if(!r.ok) throw new Error('API health check failed');
        dot.classList.add('ok');
      }catch(e){
        dot.classList.remove('ok');
      }
    }

    $('#btn-login').addEventListener('click', async () => {
      const email = $('#login-email').value.trim();
      const password = $('#login-password').value;
      authMsg('#login-msg','');
      if(!email || !password){ authMsg('#login-msg','Enter your email and password.'); return; }
      const btn = $('#btn-login'); btn.disabled = true; btn.textContent = 'Signing in…';
      try{
        const res = await api('/api/auth/login', {method:'POST', auth:false, body:{email,password}});
        state.token = res.token;
        state.user = {username: res.username, email: res.email, role: res.role};
        saveSession();
        await afterLogin();
      }catch(e){
        authMsg('#login-msg', e.message);
      }finally{ btn.disabled=false; btn.textContent='Sign in'; }
    });

    $('#btn-signup').addEventListener('click', async () => {
      const username = $('#signup-username').value.trim();
      const email = $('#signup-email').value.trim();
      const password = $('#signup-password').value;
      authMsg('#signup-msg','');
      if(!username || !email || !password){ authMsg('#signup-msg','Fill in all fields.'); return; }
      const btn = $('#btn-signup'); btn.disabled=true; btn.textContent='Sending code…';
      try{
        await api('/api/auth/signup', {method:'POST', auth:false, body:{username,email,password}});
        state.pendingSignupEmail = email;
        saveAuthDraft();
        $('#verify-email-label').textContent = email;
        showAuthView('verify');
      }catch(e){
        authMsg('#signup-msg', e.message);
      }finally{ btn.disabled=false; btn.textContent='Send verification code'; }
    });

    $('#btn-verify').addEventListener('click', async () => {
      const otp = $('#verify-otp').value.trim();
      authMsg('#verify-msg','');
      if(!otp){ authMsg('#verify-msg','Enter the code.'); return; }
      const btn = $('#btn-verify'); btn.disabled=true; btn.textContent='Verifying…';
      try{
        const res = await api('/api/auth/verify-signup', {method:'POST', auth:false, body:{email: state.pendingSignupEmail, otp, purpose:'SIGNUP'}});
        state.token = res.token;
        state.user = {username: res.username, email: res.email, role: res.role};
        saveSession();
        await afterLogin();
      }catch(e){
        authMsg('#verify-msg', e.message);
      }finally{ btn.disabled=false; btn.textContent='Verify & continue'; }
    });

    $('#btn-resend-signup').addEventListener('click', async () => {
      try{
        if(!state.pendingSignupEmail){
          const draft = readAuthDraft();
          state.pendingSignupEmail = draft?.pendingSignupEmail || null;
        }
        if(!state.pendingSignupEmail){
          toast('Please enter your email again and send signup code first.', 'err');
          return;
        }
        await api('/api/otp/resend', {method:'POST', auth:false, body:{email: state.pendingSignupEmail}, params:{purpose:'SIGNUP'}});
        toast('Verification code resent.');
      }catch(e){ toast(e.message,'err'); }
    });

    $('#btn-forgot').addEventListener('click', async () => {
      const email = $('#forgot-email').value.trim();
      authMsg('#forgot-msg','');
      if(!email){ authMsg('#forgot-msg','Enter your email.'); return; }
      const btn = $('#btn-forgot'); btn.disabled=true; btn.textContent='Sending…';
      try{
        await api('/api/auth/forgot-password', {method:'POST', auth:false, body:{email}});
        state.pendingResetEmail = email;
        $('#reset-email-label').textContent = email;
        showAuthView('reset');
      }catch(e){
        authMsg('#forgot-msg', e.message);
      }finally{ btn.disabled=false; btn.textContent='Send reset code'; }
    });

    $('#btn-reset').addEventListener('click', async () => {
      const otp = $('#reset-otp').value.trim();
      const newPassword = $('#reset-newpass').value;
      const confirmPassword = $('#reset-confirmpass').value;
      authMsg('#reset-msg','');
      if(!otp || !newPassword || !confirmPassword){ authMsg('#reset-msg','Fill in all fields.'); return; }
      if(newPassword !== confirmPassword){ authMsg('#reset-msg','Passwords do not match.'); return; }
      const btn = $('#btn-reset'); btn.disabled=true; btn.textContent='Updating…';
      try{
        await api('/api/auth/change-password', {method:'POST', auth:false, body:{email: state.pendingResetEmail, otp, newPassword, confirmPassword}});
        authMsg('#reset-msg','Password updated — you can sign in now.','success');
        setTimeout(()=>showAuthView('login'), 1200);
      }catch(e){
        authMsg('#reset-msg', e.message);
      }finally{ btn.disabled=false; btn.textContent='Update password'; }
    });

/* ---------------- SESSION BOOT ---------------- */
    async function afterLogin(){
      $('#auth-screen').style.display = 'none';
      $('#app').classList.add('active');
      $('#sidebar-name').textContent = state.user.username;
      $('#sidebar-email').textContent = state.user.email;
      $('#sidebar-avatar').textContent = initials(state.user.username);
      try{ state.categories = await api('/api/categories'); }catch(e){ state.categories = []; }
      try{ state.preferences = await api('/api/preferences'); }catch(e){ state.preferences = {userMode:'NORMAL', notificationMode:'NORMAL'}; }
      try{ state.customModes = await api('/api/custom-modes'); }catch(e){ state.customModes = []; }
      goPage('dashboard');
    }
    function doLogout(){
      clearSession();
      clearAuthDraft();
      state.token = null; state.user = null; state.categories = []; state.preferences = null; state.insightsCache = null;
      state.pendingSignupEmail = null;
      state.pendingResetEmail = null;
      $('#app').classList.remove('active');
      $('#auth-screen').style.display = 'flex';
      showAuthView('login');
    }
    $('#btn-logout').addEventListener('click', doLogout);
