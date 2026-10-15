// Builds Google Play listing graphics (icon, feature graphics, framed screenshots) from raw captures.
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');
const sharp = require('sharp');

const ROOT = __dirname;
const PROJECT = 'C:/Users/Usuario/Desktop/Android/RulePlay';
const FONTS = `${PROJECT}/app/src/main/res/font`;
const OUT = `${PROJECT}/store/google-play`;
const CHROME = 'C:/Program Files/Google/Chrome/Application/chrome.exe';
const html = path.join(ROOT, 'html');
fs.mkdirSync(html, { recursive: true });

const fileUrl = p => 'file:///' + p.replace(/\\/g, '/');

const css = `
@font-face { font-family: Bebas; src: url('${fileUrl(FONTS + '/bebas_neue.ttf')}'); }
@font-face { font-family: Inter; src: url('${fileUrl(FONTS + '/inter_variable.ttf')}'); font-weight: 100 900; }
:root { --navy:#0A0F1F; --ink:#05070D; --lime:#C6FF3D; --paper:#F2F0EA; --muted:#A7AEC2; }
* { margin:0; padding:0; box-sizing:border-box; }
html, body { width:100%; height:100%; overflow:hidden; background:var(--ink); }
body { font-family: Inter, sans-serif; color: var(--paper); -webkit-font-smoothing: antialiased; }
.bg { position:absolute; inset:0; background:
   radial-gradient(1200px 900px at var(--gx,80%) var(--gy,8%), rgba(198,255,61,.16), transparent 60%),
   radial-gradient(900px 900px at 0% 100%, rgba(64,96,255,.14), transparent 60%),
   linear-gradient(180deg, #0B1124 0%, #05070D 100%); }
.lines { position:absolute; inset:0; opacity:.5; }
.mark { display:inline-block; }
`;

const markSvg = size => `<svg class="mark" width="${size}" height="${size}" viewBox="30 30 48 48"><path fill="#C6FF3D" d="M43,30h22a13,13 0,0 1,13 13v22a13,13 0,0 1,-13 13h-22a13,13 0,0 1,-13 -13v-22a13,13 0,0 1,13 -13z"/><path fill="#05070D" d="M43.4,41.5h3.9a1.9,1.9 0,0 1,1.9 1.9v21.2a1.9,1.9 0,0 1,-1.9 1.9h-3.9a1.9,1.9 0,0 1,-1.9 -1.9v-21.2a1.9,1.9 0,0 1,1.9 -1.9z"/><path fill="#05070D" d="M51.6,41.5L68.4,54L51.6,66.5Z"/></svg>`;

const wordmark = size => `<div style="display:flex;align-items:center;gap:${size * 0.32}px">${markSvg(size)}<span style="font-family:Bebas;font-size:${size * 1.02}px;letter-spacing:.01em;line-height:1"><span style="color:var(--paper)">RULE</span><span style="color:var(--lime)">PLAY</span></span></div>`;

const phone = (img, width, extra = '') => {
  const r = width * 0.085;
  return `<div style="width:${width}px;border-radius:${r}px;padding:${width * 0.018}px;background:linear-gradient(160deg,#2A3350,#121829 40%,#0A0F1F);
    box-shadow:0 0 0 2px rgba(255,255,255,.06), 0 40px 120px rgba(0,0,0,.65), 0 0 120px rgba(198,255,61,.07);${extra}">
    <img src="${fileUrl(img)}" style="display:block;width:100%;border-radius:${r * 0.8}px"></div>`;
};

function render(name, w, h, body, extraCss = '') {
  const file = path.join(html, name + '.html');
  fs.writeFileSync(file, `<!doctype html><html><head><meta charset="utf-8"><style>${css}${extraCss}</style></head><body>${body}</body></html>`);
  const png = path.join(html, name + '.png');
  execFileSync(CHROME, ['--headless=new', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=1', '--allow-file-access-from-files',
    `--window-size=${w},${h}`, '--virtual-time-budget=3000', `--screenshot=${png}`, fileUrl(file)], { stdio: 'ignore', timeout: 60000 });
  return png;
}

const pitchLines = (w, h, cx, cy, r) => `<svg class="lines" width="${w}" height="${h}"><circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="rgba(198,255,61,.10)" stroke-width="3"/><line x1="0" y1="${cy}" x2="${w}" y2="${cy}" stroke="rgba(198,255,61,.06)" stroke-width="3"/></svg>`;

const shots = {
  en: [
    ['en-discover', 'Every sport. Every rule.', 'Every sport has a <em>story</em>', 'The rules of 40 sports, explained in plain language. Works offline.'],
    ['en-detail2', 'The game in 30 seconds', 'Get the gist <em>fast</em>', 'Every sport starts with a four-line summary, the objective and the essentials.'],
    ['en-detail4', 'Start here', 'The rules that <em>matter</em>', 'Key rules come first, written to be understood at a glance.'],
    ['en-reader2', 'Rule reader', 'Tap for the <em>full story</em>', 'Clear explanations, worked examples and related rules.'],
    ['en-search', 'Search everything', 'Find any rule <em>fast</em>', 'Offside, tie-break, LBW… search across every sport at once.'],
    ['en-search-open', 'Football · Law 11', 'Offside, <em>finally</em> explained', 'With the official law it comes from and a link to the source.'],
    ['en-discover4', '12 categories', 'Pick your <em>arena</em>', 'Team, racket, combat, water, winter, motorsport and more.'],
    ['en-gems', 'Off the beaten track', 'Discover hidden <em>gems</em>', 'Curling, kabaddi, sepak takraw… your next favourite sport is in here.'],
  ],
  es: [
    ['es-discover', 'Cada deporte. Cada regla.', 'Cada deporte tiene su <em>historia</em>', 'Las reglas de 40 deportes, explicadas en lenguaje sencillo. Sin conexión.'],
    ['es-detail2', 'El juego en 30 segundos', 'Entiéndelo <em>rápido</em>', 'Cada deporte empieza con un resumen de cuatro líneas, el objetivo y lo esencial.'],
    ['es-detail4', 'Empieza aquí', 'Las reglas que <em>importan</em>', 'Primero las reglas clave, escritas para entenderse de un vistazo.'],
    ['es-reader2', 'Lector de reglas', 'Toca para leer <em>más</em>', 'Explicaciones claras, ejemplos prácticos y reglas relacionadas.'],
    ['es-search', 'Busca de todo', 'Encuentra cualquier <em>regla</em>', 'Fuera de juego, tie-break, LBW… busca en todos los deportes a la vez.'],
    ['es-search-open', 'Fútbol · Regla 11', 'El fuera de juego, <em>por fin</em> claro', 'Con la regla oficial de la que procede y un enlace a la fuente.'],
    ['es-discover4', '12 categorías', 'Elige tu <em>terreno</em>', 'Equipo, raqueta, combate, agua, invierno, motor y mucho más.'],
    ['es-gems', 'Fuera de lo habitual', 'Descubre joyas <em>ocultas</em>', 'Curling, kabaddi, sepak takraw… tu próximo deporte favorito está aquí.'],
  ],
};

const screenshotCss = `
.head { position:absolute; left:84px; right:84px; top:104px; }
.over { font-weight:700; font-size:30px; letter-spacing:.22em; text-transform:uppercase; color:var(--lime); display:flex; align-items:center; gap:16px; }
.over:before { content:''; width:14px; height:14px; border-radius:50%; background:var(--lime); box-shadow:0 0 18px rgba(198,255,61,.8); }
h1 { font-family:Bebas; font-weight:400; font-size:132px; line-height:.9; margin-top:22px; text-transform:uppercase; letter-spacing:.005em; }
h1 em { font-style:normal; color:var(--lime); }
.sub { margin-top:26px; font-size:36px; line-height:1.35; color:var(--muted); max-width:880px; }
.device { position:absolute; left:50%; transform:translateX(-50%); }
`;

(async () => {
  fs.mkdirSync(OUT, { recursive: true });

  // 1. App icon, 512×512, full bleed (Play applies its own mask).
  const iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="18 18 72 72">
    <defs><radialGradient id="g" cx="0.7" cy="0.2" r="0.9"><stop offset="0" stop-color="#16203D"/><stop offset="1" stop-color="#05070D"/></radialGradient></defs>
    <rect x="18" y="18" width="72" height="72" fill="url(#g)"/>
    <circle cx="54" cy="54" r="30" fill="none" stroke="#C6FF3D" stroke-opacity=".16" stroke-width="0.9"/>
    <path d="M18,54H90" stroke="#C6FF3D" stroke-opacity=".10" stroke-width="0.9"/>
    <path fill="#C6FF3D" d="M43,30h22a13,13 0,0 1,13 13v22a13,13 0,0 1,-13 13h-22a13,13 0,0 1,-13 -13v-22a13,13 0,0 1,13 -13z"/>
    <path fill="#05070D" d="M43.4,41.5h3.9a1.9,1.9 0,0 1,1.9 1.9v21.2a1.9,1.9 0,0 1,-1.9 1.9h-3.9a1.9,1.9 0,0 1,-1.9 -1.9v-21.2a1.9,1.9 0,0 1,1.9 -1.9z"/>
    <path fill="#05070D" d="M51.6,41.5L68.4,54L51.6,66.5Z"/></svg>`;
  await sharp(Buffer.from(iconSvg)).resize(512, 512).png().toFile(`${OUT}/icon-512.png`);

  // 2. Phone screenshots, 1080×1920.
  for (const [lang, list] of Object.entries(shots)) {
    fs.mkdirSync(`${OUT}/${lang}/phone`, { recursive: true });
    for (let i = 0; i < list.length; i++) {
      const [raw, over, title, sub] = list[i];
      const gx = i % 2 ? '15%' : '85%';
      const body = `<div class="bg" style="--gx:${gx}"></div>${pitchLines(1080, 1920, i % 2 ? 150 : 930, 1500, 420)}
        <div class="head"><div class="over">${over}</div><h1>${title}</h1><div class="sub">${sub}</div></div>
        <div class="device" style="top:640px">${phone(path.join(ROOT, 'raw', raw + '.png'), 800)}</div>`;
      const png = render(`${lang}-${i + 1}`, 1080, 1920, body, screenshotCss);
      await sharp(png).flatten({ background: '#05070D' }).png().toFile(`${OUT}/${lang}/phone/${String(i + 1).padStart(2, '0')}-${raw.replace(/^..-/, '')}.png`);
    }
  }

  // 3. Feature graphics, 1024×500.
  const fg = {
    en: ['Every sport.', 'Every rule.', 'The rules of 40 sports, explained simply.'],
    es: ['Cada deporte.', 'Cada regla.', 'Las reglas de 40 deportes, explicadas fácil.'],
  };
  for (const [lang, [a, b, sub]] of Object.entries(fg)) {
    const body = `<div class="bg" style="--gx:78%;--gy:30%"></div>${pitchLines(1024, 500, 800, 250, 230)}
      <div style="position:absolute;left:64px;top:74px">${wordmark(46)}
        <div style="font-family:Bebas;font-size:${lang === 'es' ? 88 : 104}px;line-height:.88;margin-top:34px;text-transform:uppercase">${a}<br><span style="color:var(--lime)">${b}</span></div>
        <div style="margin-top:22px;font-size:24px;color:var(--muted)">${sub}</div></div>
      <div style="position:absolute;left:600px;top:96px;transform:rotate(-8deg)">${phone(path.join(ROOT, 'raw', lang + '-discover.png'), 220)}</div>
      <div style="position:absolute;left:790px;top:40px;transform:rotate(6deg)">${phone(path.join(ROOT, 'raw', lang + '-detail4.png'), 220)}</div>`;
    const png = render(`fg-${lang}`, 1024, 500, body);
    await sharp(png).flatten({ background: '#05070D' }).png().toFile(`${OUT}/feature-graphic-${lang}.png`);
  }
  console.log('done');
})();
