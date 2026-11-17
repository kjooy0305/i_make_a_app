// extension/content.js 를 그대로 가져와 Tampermonkey/Violentmonkey 용 유저스크립트로 포장한다.
// 사용: node adskip/tools/build-userscript.js  (버전은 extension/manifest.json 에서 읽음)
const fs = require('fs');
const path = require('path');
const root = path.join(__dirname, '..');
const manifest = JSON.parse(fs.readFileSync(path.join(root, 'extension/manifest.json'), 'utf8'));
const body = fs.readFileSync(path.join(root, 'extension/content.js'), 'utf8');
const header = `// ==UserScript==
// @name         광고 건너뛰기 (AdSkip)
// @namespace    https://kjooy0305.github.io/i_make_a_app/adskip/
// @version      ${manifest.version}
// @description  유튜브 영상 광고가 나오면 '건너뛰기' 버튼을 대신 누르고, 버튼이 없는 광고는 끝으로 넘기고, '이어서 시청하시겠어요?' 창도 자동으로 확인합니다.
// @author       kjooy0305
// @match        *://www.youtube.com/*
// @match        *://m.youtube.com/*
// @match        *://music.youtube.com/*
// @match        *://youtube.com/*
// @match        *://www.youtube-nocookie.com/*
// @icon         https://kjooy0305.github.io/i_make_a_app/adskip/icon.png
// @downloadURL  https://kjooy0305.github.io/i_make_a_app/adskip/userscript/adskip.user.js
// @updateURL    https://kjooy0305.github.io/i_make_a_app/adskip/userscript/adskip.user.js
// @grant        GM_getValue
// @grant        GM_setValue
// @run-at       document-start
// ==/UserScript==

`;
fs.writeFileSync(path.join(root, 'userscript/adskip.user.js'), header + body);
console.log('userscript/adskip.user.js  v' + manifest.version);
