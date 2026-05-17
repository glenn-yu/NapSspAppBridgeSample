import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';
import assert from 'node:assert/strict';

const root = process.cwd();
const bridgePath = path.join(root, 'docs/integration-package/web/napssp-bridge.js');
const webSamplePath = path.join(root, 'docs/integration-package/web/sample.html');
const iosSamplePath = path.join(root, 'ios/Sources/NapSspIOSSample/index.html');

function read(relPath) {
  return fs.readFileSync(relPath, 'utf8');
}

function createBridgeSandbox(extraWindow = {}) {
  const warnings = [];
  const errors = [];
  const timers = [];
  const window = { ...extraWindow };
  const sandbox = {
    window,
    console: {
      warn: (...args) => warnings.push(args.join(' ')),
      error: (...args) => errors.push(args.join(' ')),
      log: () => {},
    },
    setTimeout: (fn, _ms) => {
      timers.push(fn);
      return timers.length;
    },
    Date,
  };
  sandbox.globalThis = sandbox;
  vm.createContext(sandbox);
  vm.runInContext(read(bridgePath), sandbox, { filename: bridgePath });
  return { window, warnings, errors, timers };
}

function assertHtmlStructure(filePath) {
  const html = read(filePath);
  assert.match(html, /<body[\s>]/i, `${filePath}: <body> is required`);
  assert.match(html, /<\/body>/i, `${filePath}: </body> is required`);
  assert.match(html, /<\/html>/i, `${filePath}: </html> is required`);

  const bodyClose = html.toLowerCase().lastIndexOf('</body>');
  const htmlClose = html.toLowerCase().lastIndexOf('</html>');
  assert.ok(bodyClose < htmlClose, `${filePath}: </body> must be before </html>`);

  const afterBody = html.slice(bodyClose + '</body>'.length, htmlClose).trim();
  assert.equal(afterBody, '', `${filePath}: no content is allowed after </body>`);

  const requiredControls = [
    /Initialize SDK|SDK 초기화/i,
    /Load Banner|배너/i,
    /Load Native|네이티브/i,
    /Outstream Video|비디오/i,
    /Reward Video|보상형/i,
    /Interstitial Video|전면 비디오/i,
    /Interstitial Banner|전면 배너/i,
    /Clear All Ads|광고 해제/i,
  ];

  for (const pattern of requiredControls) {
    assert.match(html, pattern, `${filePath}: missing browser QA control matching ${pattern}`);
  }
}

function runStandaloneSafariTest() {
  const { window, warnings } = createBridgeSandbox();
  assert.equal(window.NapSspBridge.isAvailable(), false, 'standalone Safari must not report bridge availability');
  assert.equal(window.NapSspBridge.init(), false, 'standalone Safari init must fail safely');
  assert.ok(warnings.some((line) => line.includes('Native 브릿지를 찾을 수 없습니다')), 'standalone Safari should warn bridge not found');
}

function runWebKitPostMessageTest() {
  const messages = [];
  const { window } = createBridgeSandbox({
    webkit: {
      messageHandlers: {
        NapSspBridge: {
          postMessage: (payload) => messages.push(payload),
        },
      },
    },
  });

  assert.equal(window.NapSspBridge.isAvailable(), true, 'WKWebView bridge should be available');
  assert.equal(window.NapSspBridge.loadAd('banner', '104704'), true, 'loadAd should be sent through WebKit');
  assert.equal(typeof messages[0], 'string', 'WKScriptMessageHandler payload must be a JSON string');
  assert.deepEqual(JSON.parse(messages[0]), {
    action: 'loadAd',
    params: { format: 'banner', adUnitId: '104704' },
  });
}

function runResponseParsingTest() {
  const { window, errors } = createBridgeSandbox();
  const responses = [];
  const events = [];
  let raw = null;

  window.NapSspBridge.onResponse = (...args) => responses.push(args);
  window.NapSspBridge.onEvent = (...args) => events.push(args);
  window.NapSspBridge.onRawMessage = (msg) => { raw = msg; };

  window.onNapSspMessage(JSON.stringify({ action: 'init', status: 'success', data: 'SDK Initialized' }));
  assert.deepEqual(responses[0], ['init', 'success', 'SDK Initialized']);
  assert.equal(JSON.stringify(raw), JSON.stringify({ action: 'init', status: 'success', data: 'SDK Initialized' }));

  window.onNapSspMessage(JSON.stringify({ action: 'event', status: 'success', data: '[banner] loaded: 104704' }));
  assert.deepEqual(events[0], ['loaded', 'banner', '104704']);

  assert.doesNotThrow(() => window.onNapSspMessage('{not-json'), 'malformed native response should not throw in Safari/WebKit');
  assert.ok(errors.some((line) => line.includes('응답 파싱 오류')), 'malformed response should be logged');
}

assertHtmlStructure(webSamplePath);
assertHtmlStructure(iosSamplePath);
runStandaloneSafariTest();
runWebKitPostMessageTest();
runResponseParsingTest();

console.log('browser-webkit-qa: PASS');
