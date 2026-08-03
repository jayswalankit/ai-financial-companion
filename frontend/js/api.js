/* ---------------- API ---------------- */
    async function api(path, {method='GET', body=null, auth=true, params=null} = {}){
      let url = state.apiBase.replace(/\/$/,'') + path;
      if(params){
        const qs = new URLSearchParams();
        Object.entries(params).forEach(([k,v]) => { if(v !== null && v !== undefined && v !== '') qs.append(k,v); });
        const qsStr = qs.toString();
        if(qsStr) url += (url.includes('?') ? '&' : '?') + qsStr;
      }
      const headers = {'Content-Type':'application/json'};
      if(auth && state.token) headers['Authorization'] = 'Bearer ' + state.token;
      let res;
      try{
        res = await fetch(url, { method, headers, body: body ? JSON.stringify(body) : undefined });
      }catch(e){
        throw new Error('Could not reach the API at ' + state.apiBase + '. Check the API Base URL and that the server is running (and CORS-enabled).');
      }
      if(res.status === 401){
        if(state.token){ toast('Session expired — please sign in again.', 'err'); doLogout(); }
        throw new Error('Unauthorized');
      }
      if(res.status === 204) return null;
      let data = null;
      const text = await res.text();
      try{ data = text ? JSON.parse(text) : null; }catch(e){ data = text; }
      if(!res.ok){
        const msg = (data && (data.message || data.error || data.messege)) || `Request failed (${res.status})`;
        throw new Error(msg);
      }
      return data;
    }
