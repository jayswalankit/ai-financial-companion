/* ---------------- SESSION STORAGE ---------------- */
const AUTH_STORAGE_KEY = 'ledger_auth';
const AUTH_DRAFT_KEY = 'ledger_auth_draft';

function saveSession(){
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({
    token: state.token,
    user: state.user,
  }));
}

function readSession(){
  const rawSession = localStorage.getItem(AUTH_STORAGE_KEY);
  if(!rawSession) return null;

  try{
    const session = JSON.parse(rawSession);
    if(!session?.token || !session?.user?.username || !session?.user?.email){
      clearSession();
      return null;
    }
    return session;
  }catch(e){
    clearSession();
    return null;
  }
}

function clearSession(){
  localStorage.removeItem(AUTH_STORAGE_KEY);
}

function saveAuthDraft(){
  localStorage.setItem(AUTH_DRAFT_KEY, JSON.stringify({
    pendingSignupEmail: state.pendingSignupEmail || null,
    pendingResetEmail: state.pendingResetEmail || null,
  }));
}

function readAuthDraft(){
  const rawDraft = localStorage.getItem(AUTH_DRAFT_KEY);
  if(!rawDraft) return null;

  try{
    const draft = JSON.parse(rawDraft);
    return {
      pendingSignupEmail: draft?.pendingSignupEmail || null,
      pendingResetEmail: draft?.pendingResetEmail || null,
    };
  }catch(e){
    localStorage.removeItem(AUTH_DRAFT_KEY);
    return null;
  }
}

function clearAuthDraft(){
  localStorage.removeItem(AUTH_DRAFT_KEY);
}
