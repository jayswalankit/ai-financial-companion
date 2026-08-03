/* ================= MODAL HELPERS ================= */
    function openModal(innerHtml){
      const root = $('#modal-root');
      root.innerHTML = `<div class="modal-overlay" id="modal-overlay"><div class="modal">${innerHtml}</div></div>`;
      $all('[data-close]', root).forEach(b => b.addEventListener('click', closeModal));
      $('#modal-overlay').addEventListener('click', (e) => { if(e.target.id === 'modal-overlay') closeModal(); });
    }
    function closeModal(){ $('#modal-root').innerHTML = ''; }
