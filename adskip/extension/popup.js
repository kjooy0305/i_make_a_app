var DEFAULTS = { enabled: true, fastForward: true, hideBanners: true, badge: true, autoResume: true };
var keys = Object.keys(DEFAULTS);
function render(cfg, stats) {
  keys.forEach(function (k) { document.getElementById(k).checked = cfg[k] !== false; });
  document.getElementById('count').textContent = (stats && stats.count) || 0;
}
chrome.storage.local.get(['cfg', 'stats'], function (r) {
  var cfg = Object.assign({}, DEFAULTS, r.cfg || {});
  render(cfg, r.stats);
  keys.forEach(function (k) {
    document.getElementById(k).addEventListener('change', function (e) {
      cfg[k] = e.target.checked;
      chrome.storage.local.set({ cfg: cfg });
    });
  });
});
chrome.storage.onChanged.addListener(function (ch) {
  if (ch.stats) document.getElementById('count').textContent = (ch.stats.newValue && ch.stats.newValue.count) || 0;
});
document.getElementById('reset').addEventListener('click', function () {
  chrome.storage.local.set({ stats: { count: 0 } });
  document.getElementById('count').textContent = '0';
});
document.getElementById('open').addEventListener('click', function () {
  chrome.tabs.create({ url: 'https://www.youtube.com/' });
});
