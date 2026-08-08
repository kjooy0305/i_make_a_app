// App initialization
document.addEventListener('DOMContentLoaded', async () => {
  // Register service worker
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js').catch(() => {});
  }

  // Block keyboard refresh (F5, Ctrl+R, Cmd+R)
  window.addEventListener('keydown', e => {
    if (e.key === 'F5' || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'r')) {
      e.preventDefault();
      toast('새로고침은 앱에서 지원하지 않습니다');
    }
  });

  // Init DB
  await DB.open();
  requestPersistentStorage();

  // Register routes
  Router.register('/', renderHome);
  Router.register('/editor', renderEditor);
  Router.register('/reader', renderReader);
  Router.register('/character', renderCharacter);
  Router.register('/dice', renderDicePanel);
  Router.register('/scenarios', renderScenarioList);

  // Initial route
  await Router.resolve();
});

async function requestPersistentStorage() {
  if (!navigator.storage || !navigator.storage.persist) return;
  try {
    const alreadyPersisted = await navigator.storage.persisted();
    if (!alreadyPersisted) await navigator.storage.persist();
  } catch (err) {
    console.warn('Persistent storage request failed', err);
  }
}
