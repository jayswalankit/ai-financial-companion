/* ---------------- UTIL ---------------- */
    const $ = (sel, root=document) => root.querySelector(sel);
    const $all = (sel, root=document) => Array.from(root.querySelectorAll(sel));
    const money = (v) => {
      const n = Number(v || 0);
      return '₹' + n.toLocaleString('en-IN', {minimumFractionDigits:2, maximumFractionDigits:2});
    };
    const fmtDate = (d) => {
      if(!d) return '—';
      const dt = new Date(d);
      if(isNaN(dt)) return d;
      return dt.toLocaleDateString('en-IN', {day:'numeric', month:'short', year:'numeric'});
    };
    const fmtDateTime = (d) => {
      if(!d) return '—';
      const dt = new Date(d);
      if(isNaN(dt)) return d;
      return dt.toLocaleString('en-IN', {day:'numeric', month:'short', hour:'2-digit', minute:'2-digit'});
    };
    const toISO = (d) => { // Date -> yyyy-mm-dd
      return d.toISOString().slice(0,10);
    };
    // Parses "4/8/2026" (day/month/year), "04-08-2026", or "2026-08-04" into ISO yyyy-mm-dd. Returns null if not date-like.
    function parseFlexibleDate(str){
      str = (str||'').trim();
      if(!str) return null;
      let m = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
      if(m) return `${m[1]}-${String(m[2]).padStart(2,'0')}-${String(m[3]).padStart(2,'0')}`;
      m = str.match(/^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})$/);
      if(m){
        const day = String(m[1]).padStart(2,'0');
        const month = String(m[2]).padStart(2,'0');
        return `${m[3]}-${month}-${day}`;
      }
      return null;
    }
    function toast(msg, type='ok'){
      const wrap = $('#toast-wrap');
      const el = document.createElement('div');
      el.className = 'toast ' + (type==='err'?'err':type==='ok'?'ok':'');
      el.textContent = msg;
      wrap.appendChild(el);
      setTimeout(()=>{ el.style.opacity='0'; el.style.transition='opacity .25s'; setTimeout(()=>el.remove(),250); }, 3200);
    }
    function escapeHtml(s){
      return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    }
    function initials(name){
      if(!name) return '?';
      return name.trim().split(/\s+/).map(w=>w[0]).slice(0,2).join('').toUpperCase();
    }
