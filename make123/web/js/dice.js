const DICE_TYPES = [4, 6, 8, 10, 12, 20, 100];

function rollDice(sides) {
  return Math.floor(Math.random() * sides) + 1;
}

function rollMultiple(sides, count = 1) {
  const rolls = Array.from({ length: count }, () => rollDice(sides));
  return { rolls, total: rolls.reduce((a, b) => a + b, 0), sides, count };
}

const DiceHistory = {
  _list: [],
  add(entry) {
    this._list.unshift(entry);
    if (this._list.length > 20) this._list.pop();
  },
  get() { return this._list; }
};

const DICE_ICONS = {
  4: '◆', 6: '⬡', 8: '◈', 10: '◉', 12: '✦', 20: '⬟', 100: '%'
};

window.Dice = { DICE_TYPES, roll: rollDice, rollMultiple, history: DiceHistory, DICE_ICONS };
