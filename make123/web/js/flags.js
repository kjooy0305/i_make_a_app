// Cross-scenario flag system
const Flags = {
  async set(key, value = true) {
    await DB.put('flags', { key, value, ts: Date.now() });
  },
  async get(key) {
    const r = await DB.get('flags', key);
    return r ? r.value : null;
  },
  async check(key) {
    return !!(await this.get(key));
  },
  async getAll() {
    return await DB.getAll('flags');
  },
  async clear(key) {
    await DB.delete('flags', key);
  }
};

// Unlock conditions: scenarioId → required flags
const UNLOCK_CONDITIONS = {
  // doksupu endings unlock things in future scenarios
  'doksupu_survived': ['doksupu_ending_survive'],
  'doksupu_truth': ['doksupu_ending_truth']
};

async function checkUnlock(conditionKey) {
  const required = UNLOCK_CONDITIONS[conditionKey];
  if (!required) return true;
  for (const flag of required) {
    if (!(await Flags.check(flag))) return false;
  }
  return true;
}

window.Flags = Flags;
window.checkUnlock = checkUnlock;
