// commitcard — autenticação (login com GitHub) compartilhada entre as páginas
(function(){
  "use strict";

  function escapeHtml(str){
    const div = document.createElement('div');
    div.textContent = str == null ? '' : String(str);
    return div.innerHTML;
  }

  function renderizar(dados){
    const slot = document.getElementById('auth-slot');
    if (!slot) return;

    if (!dados.autenticado){
      slot.innerHTML = `<a class="btn sm" href="/oauth2/authorization/github"><span class="auth-cta-full">Entrar com GitHub</span><span class="auth-cta-short">Entrar</span></a>`;
      return;
    }

    const nomeExibido = escapeHtml(dados.githubUsername || dados.nome || '');

    slot.innerHTML = `
      <div class="auth-menu">
        <button class="auth-trigger" id="auth-trigger" aria-haspopup="true" aria-expanded="false">
          <img src="${escapeHtml(dados.avatarUrl || '')}" alt="" class="auth-avatar">
          <span class="auth-name">${nomeExibido}</span>
        </button>
        <div class="auth-dropdown" id="auth-dropdown" hidden>
          <a href="/app.html?user=${encodeURIComponent(dados.githubUsername || '')}">Meu portfólio</a>
          <a href="/logout">Sair</a>
        </div>
      </div>
    `;

    const trigger = document.getElementById('auth-trigger');
    const dropdown = document.getElementById('auth-dropdown');

    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      const aberto = !dropdown.hidden;
      dropdown.hidden = aberto;
      trigger.setAttribute('aria-expanded', String(!aberto));
    });

    document.addEventListener('click', (e) => {
      if (!trigger.contains(e.target) && !dropdown.contains(e.target)) {
        dropdown.hidden = true;
        trigger.setAttribute('aria-expanded', 'false');
      }
    });
  }

  async function carregar(){
    try{
      const res = await fetch('/api/auth/me');
      const dados = await res.json();
      window.commitcardAuth = dados;
      renderizar(dados);
      document.dispatchEvent(new CustomEvent('commitcard:auth', { detail: dados }));
    } catch(e){
      window.commitcardAuth = { autenticado: false };
      renderizar(window.commitcardAuth);
    }
  }

  document.addEventListener('DOMContentLoaded', carregar);
})();