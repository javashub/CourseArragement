// 通过 CDP 量取页面关键元素尺寸，用于让 mockup 精确落在 1440×900 / 980 视口内
import { spawn } from 'node:child_process';

const CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const [url, width = '1440', height = '900', selector = 'body'] = process.argv.slice(2);
const port = 9333;

const chrome = spawn(
  CHROME,
  [
    '--headless',
    '--disable-gpu',
    '--no-sandbox',
    '--hide-scrollbars',
    '--no-first-run',
    '--force-device-scale-factor=2',
    '--remote-debugging-port=' + port,
    '--user-data-dir=/tmp/ca-cdp-profile',
    `--window-size=${width},${height}`,
    'about:blank'
  ],
  { stdio: 'ignore' }
);

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function waitPort() {
  for (let i = 0; i < 50; i++) {
    try {
      const res = await fetch(`http://127.0.0.1:${port}/json/version`);
      if (res.ok) return;
    } catch {
      /* retry */
    }
    await sleep(100);
  }
  throw new Error('chrome devtools not ready');
}

await waitPort();
const target = await (
  await fetch(`http://127.0.0.1:${port}/json/new?${encodeURIComponent(url)}`, { method: 'PUT' })
).json();

const ws = new WebSocket(target.webSocketDebuggerUrl);
await new Promise((resolve, reject) => {
  ws.onopen = resolve;
  ws.onerror = reject;
});

let id = 0;
const pending = new Map();
ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (msg.id && pending.has(msg.id)) {
    pending.get(msg.id)(msg);
    pending.delete(msg.id);
  }
};
function send(method, params = {}) {
  const messageId = ++id;
  ws.send(JSON.stringify({ id: messageId, method, params }));
  return new Promise((resolve) => pending.set(messageId, resolve));
}

await send('Page.enable');
await send('Runtime.enable');
await sleep(900);

const expression = `(() => {
  const rect = (sel) => {
    const el = document.querySelector(sel);
    if (!el) return null;
    const r = el.getBoundingClientRect();
    return { sel, x: Math.round(r.x), y: Math.round(r.y), w: Math.round(r.width), h: Math.round(r.height), bottom: Math.round(r.bottom) };
  };
  const out = {
    viewport: { w: innerWidth, h: innerHeight, scrollH: document.documentElement.scrollHeight },
    body: { scrollH: document.body.scrollHeight },
    items: ${JSON.stringify(selector.split(','))}.map((sel) => rect(sel))
  };
  return JSON.stringify(out, null, 2);
})()`;

const result = await send('Runtime.evaluate', { expression, returnByValue: true });
console.log(result.result?.result?.value || JSON.stringify(result, null, 2));

ws.close();
chrome.kill();
process.exit(0);
