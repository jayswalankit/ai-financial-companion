/* ---------------- SESSION STORAGE ---------------- */
const AUTH_STORAGE_KEY = 'ledger_auth';

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
