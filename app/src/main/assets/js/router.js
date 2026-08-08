const ROUTES = {};
let _current = null;

function register(path, handler) {
  ROUTES[path] = handler;
}

function navigate(path) {
  window.location.hash = path;
}

async function resolve() {
  const hash = window.location.hash.slice(1) || '/';
  const [base, ...params] = hash.split('/').filter(Boolean);
  const route = '/' + (base || '');
  const handler = ROUTES[route] || ROUTES['/'];
  if (!handler) return;
  _current = route;
  const page = document.getElementById('page');
  page.innerHTML = '';
  await handler(page, params);
  document.querySelectorAll('nav button[data-route]').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.route === route);
  });
}

window.addEventListener('hashchange', resolve);

window.Router = { register, navigate, resolve, current: () => _current };
