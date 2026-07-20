// commitcard — tema claro/escuro compartilhado entre as páginas
(function(){
  "use strict";

  function applyTheme(theme){
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('commitcard-theme', theme);
  }

  function initTheme(){
    const saved = localStorage.getItem('commitcard-theme');
    if (saved){ applyTheme(saved); return; }
    const prefersLight = window.matchMedia('(prefers-color-scheme: light)').matches;
    applyTheme(prefersLight ? 'light' : 'dark');
  }

  initTheme();

  document.addEventListener('DOMContentLoaded', () => {
    const toggle = document.getElementById('theme-toggle');
    if (!toggle) return;
    toggle.addEventListener('click', () => {
      const current = document.documentElement.getAttribute('data-theme');
      applyTheme(current === 'light' ? 'dark' : 'light');
    });
  });
})();