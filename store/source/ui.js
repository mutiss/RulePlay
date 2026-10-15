// Drives the emulator by visible text. Usage: node ui.js <cmd> [args]...  (commands chained: tap:Text shot:name wait:2 swipe:x1,y1,x2,y2 back key:CODE dump)
const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const ADB = path.join(process.env.LOCALAPPDATA, 'Android/Sdk/platform-tools/adb.exe');
const OUT = path.join(__dirname, 'raw');
fs.mkdirSync(OUT, { recursive: true });

const adb = (...a) => execFileSync(ADB, ['-s', 'emulator-5554', ...a], { timeout: 60000, maxBuffer: 64 << 20 });
const sleep = s => execFileSync('node', ['-e', `setTimeout(()=>{},${s * 1000})`]);

function nodes() {
  adb('shell', 'uiautomator', 'dump', '/sdcard/ui.xml');
  const xml = adb('exec-out', 'cat', '/sdcard/ui.xml').toString();
  return [...xml.matchAll(/<node [^>]*>/g)].map(m => {
    const a = k => (m[0].match(new RegExp(` ${k}="([^"]*)"`)) || [])[1] || '';
    const b = a('bounds').match(/\d+/g).map(Number);
    return { text: a('text'), desc: a('content-desc'), x: (b[0] + b[2]) / 2, y: (b[1] + b[3]) / 2, b };
  });
}

function tap(label) {
  const [want, idx] = label.split('#');
  const all = nodes().filter(n => n.text === want || n.desc === want);
  const fuzzy = all.length ? all : nodes().filter(n => n.text.includes(want) || n.desc.includes(want));
  const n = fuzzy[Number(idx || 0)];
  if (!n) throw new Error('not found: ' + label);
  adb('shell', 'input', 'tap', String(Math.round(n.x)), String(Math.round(n.y)));
}

for (const step of process.argv.slice(2)) {
  const [cmd, ...rest] = step.split(':');
  const arg = rest.join(':');
  if (cmd === 'tap') tap(arg);
  else if (cmd === 'wait') sleep(Number(arg));
  else if (cmd === 'swipe') adb('shell', 'input', 'swipe', ...arg.split(','), '700');
  else if (cmd === 'back') adb('shell', 'input', 'keyevent', '4');
  else if (cmd === 'type') adb('shell', 'input', 'text', arg.replace(/ /g, '%s'));
  else if (cmd === 'shot') fs.writeFileSync(path.join(OUT, arg + '.png'), adb('exec-out', 'screencap', '-p'));
  else if (cmd === 'dump') { for (const n of nodes()) { if (n.text || n.desc) console.log(n.b.join(','), JSON.stringify(n.text), JSON.stringify(n.desc)); } }
  else throw new Error('bad step ' + step);
  sleep(0.6);
}
