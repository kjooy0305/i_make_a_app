/* KeywordScan 엔진: 다중 키워드 스캔 / EPUB·TXT 읽기 / 파일명 태깅 / 유사 파일명 검사
 * 브라우저(window.KSEngine)와 Node(module.exports) 양쪽에서 동작한다. */
(function (root, factory) {
  if (typeof module === 'object' && module.exports) module.exports = factory();
  else root.KSEngine = factory();
})(typeof self !== 'undefined' ? self : this, function () {
  'use strict';

  const SEGMENTS = 10;

  const isSpace = (c) => c <= 32 || c === 0xa0 || c === 0x3000 || c === 0x200b || c === 0xfeff;

  /* ---------- 키워드 → 검색어 목록 ---------- */
  function termsOf(kw) {
    const out = [];
    const seen = new Set();
    [kw.name].concat(kw.synonyms || []).forEach((t) => {
      const n = normalizeTerm(t);
      if (n && !seen.has(n)) { seen.add(n); out.push(n); }
    });
    return out;
  }
  // termsOf 와 같은 순서의 화면 표시용 이름 (이름, 유의어...). 하위 키워드별 횟수를 보여줄 때 쓴다.
  function termLabels(kw) {
    const out = [];
    const seen = new Set();
    [kw.name].concat(kw.synonyms || []).forEach((t) => {
      const n = normalizeTerm(t);
      if (n && !seen.has(n)) { seen.add(n); out.push(String(t).trim()); }
    });
    return out;
  }
  function normalizeTerm(t) {
    return String(t || '').toLowerCase().replace(/[\s 　​﻿]+/g, '');
  }
  /* 검색어마다 '키워드 안에서 공백이 있던 자리'(공백 앞 글자의 번호)를 함께 돌려준다. termsOf 와 같은 순서.
   * 본문을 비교할 때 공백은 무시하지만, 키워드에 공백이 없는데 본문의 글자 사이에 공백이 끼어 있으면(= 다른 낱말 사이)
   * 맞은 것으로 치지 않는다. 예) 분만대 가 '한 분만 대외로' 에서 걸리면 안 된다. */
  function termSpecs(kw) {
    const out = [];
    const byTerm = new Map();
    [kw.name].concat(kw.synonyms || []).forEach((raw) => {
      let term = '';
      const gaps = [];
      const low = String(raw || '').toLowerCase();
      for (let i = 0; i < low.length; i++) {
        if (isSpace(low.charCodeAt(i))) { if (term.length) gaps[term.length - 1] = true; } else term += low[i];
      }
      if (!term) return;
      gaps.length = Math.max(0, term.length - 1); // 맨 끝의 공백은 의미 없다
      const prev = byTerm.get(term);
      if (prev) { gaps.forEach((g, i) => { if (g) prev.gaps[i] = true; }); return; } // 같은 말: 공백 허용 자리를 합친다
      const spec = { term, gaps };
      byTerm.set(term, spec); out.push(spec);
    });
    return out;
  }
  // 결과 형식이 바뀔 때 올린다: 올리면 저장된 분석 결과가 '재분석 필요'로 표시된다.
  const MATCH_VERSION = 'm2';
  function termsSignature(kw) {
    return MATCH_VERSION + ':' + termsOf(kw).slice().sort().join('|');
  }

  /* ---------- Aho-Corasick: 모든 키워드를 한 번에 비교 ---------- */
  function buildMatcher(keywords) {
    const nodes = [{ next: new Map(), fail: 0, out: null }];
    const ids = keywords.map((k) => k.id);
    keywords.forEach((kw, idx) => {
      termsOf(kw).forEach((term, ti) => {
        let cur = 0;
        for (let i = 0; i < term.length; i++) {
          const ch = term[i];
          let nx = nodes[cur].next.get(ch);
          if (nx === undefined) {
            nx = nodes.length;
            nodes.push({ next: new Map(), fail: 0, out: null });
            nodes[cur].next.set(ch, nx);
          }
          cur = nx;
        }
        (nodes[cur].out || (nodes[cur].out = [])).push([idx, term.length, ti]);
      });
    });
    // fail 링크 (BFS) + 출력 병합(긴 것 우선)
    const queue = [];
    nodes[0].next.forEach((n) => { nodes[n].fail = 0; queue.push(n); });
    for (let qi = 0; qi < queue.length; qi++) {
      const cur = queue[qi];
      nodes[cur].next.forEach((nx, ch) => {
        let f = nodes[cur].fail;
        while (f && !nodes[f].next.has(ch)) f = nodes[f].fail;
        const target = nodes[f].next.get(ch);
        nodes[nx].fail = target !== undefined && target !== nx ? target : 0;
        queue.push(nx);
      });
      const fo = nodes[nodes[cur].fail].out;
      if (fo) {
        const merged = (nodes[cur].out || []).concat(fo);
        merged.sort((a, b) => b[1] - a[1]);
        nodes[cur].out = merged;
      }
    }
    const specs = keywords.map((k) => termSpecs(k));
    return { nodes, ids, size: keywords.length, termLens: specs.map((sp) => sp.length), gaps: specs.map((sp) => sp.map((t) => t.gaps)) };
  }

  /* ---------- 본문 스캔 (1회 통과, 줄 단위, 공백 무시, 대소문자 무시) ---------- */
  async function scanText(text, matcher, opts) {
    opts = opts || {};
    const n = matcher.size;
    const counts = new Array(n).fill(0);
    const segs = [];
    for (let i = 0; i < n; i++) segs.push(new Array(SEGMENTS).fill(0));
    const lastEnd = new Array(n).fill(-1);
    // 하위 키워드(이름·유의어)별 횟수: 각 말을 따로 센다 (키워드 합계와 달리 서로 겹쳐도 각각 센다)
    const termCounts = matcher.termLens.map((len) => new Array(len).fill(0));
    const termEnd = matcher.termLens.map((len) => new Array(len).fill(-1));
    const lines = text.split(/\r\n|\r|\n/);

    let total = 0;
    for (let li = 0; li < lines.length; li++) {
      const s = lines[li];
      for (let i = 0; i < s.length; i++) if (!isSpace(s.charCodeAt(i))) total++;
    }
    if (!total) return { totalChars: 0, ids: matcher.ids, counts, segs, terms: termCounts };

    const nodes = matcher.nodes;
    const gapsOf = matcher.gaps;
    let origIdx = new Int32Array(1024); // 공백을 뺀 n번째 글자가 줄의 몇 번째 글자였는지
    let pos = 0;
    let budget = 0;
    const CHUNK = opts.chunk || 300000;
    for (let li = 0; li < lines.length; li++) {
      const s = lines[li].toLowerCase();
      if (origIdx.length < s.length) origIdx = new Int32Array(Math.max(s.length, origIdx.length * 2));
      let lp = 0; // 이 줄에서 공백을 뺀 글자 수
      let state = 0;
      for (let i = 0; i < s.length; i++) {
        if (isSpace(s.charCodeAt(i))) continue;
        origIdx[lp++] = i;
        const ch = s[i];
        while (state && !nodes[state].next.has(ch)) state = nodes[state].fail;
        const nx = nodes[state].next.get(ch);
        state = nx === undefined ? 0 : nx;
        pos++;
        const out = nodes[state].out;
        if (out) {
          for (let k = 0; k < out.length; k++) {
            const idx = out[k][0];
            const len = out[k][1];
            const start = pos - len;
            const ti = out[k][2];
            // 맞은 구간의 글자 사이에 공백이 끼어 있으면, 키워드에도 그 자리에 공백이 있을 때만 인정한다
            if (len > 1 && origIdx[lp - 1] - origIdx[lp - len] > len - 1) {
              const g = gapsOf[idx][ti];
              let bad = false;
              for (let q = 0; q < len - 1; q++) {
                if (origIdx[lp - len + q + 1] - origIdx[lp - len + q] > 1 && !g[q]) { bad = true; break; }
              }
              if (bad) continue;
            }
            if (start >= termEnd[idx][ti]) { termEnd[idx][ti] = pos; termCounts[idx][ti]++; }
            if (start >= lastEnd[idx]) { // 같은 키워드의 동의어끼리 겹치면 1회만 센다
              lastEnd[idx] = pos;
              counts[idx]++;
              let sg = Math.floor(((pos - 1) / total) * SEGMENTS);
              if (sg >= SEGMENTS) sg = SEGMENTS - 1;
              segs[idx][sg]++;
            }
          }
        }
        if (++budget >= CHUNK) {
          budget = 0;
          if (opts.yieldFn) await opts.yieldFn(pos / total);
        }
      }
    }
    return { totalChars: total, ids: matcher.ids, counts, segs, terms: termCounts };
  }

  /* ---------- 결과 해석: 임계값 → (!)/(?) ---------- */
  function statusOf(count, threshold) {
    if (!count) return '';
    return count >= Math.max(1, threshold || 1) ? '!' : '?';
  }
  function densityPer10k(count, totalChars) {
    return totalChars ? (count / totalChars) * 10000 : 0;
  }
  // 분포: 소설 전체에 퍼져 있는지 / 특정 구간에 몰려 있는지
  function distributionOf(segCounts) {
    const total = segCounts.reduce((a, b) => a + b, 0);
    if (!total) return { kind: '', spread: 0, peak: -1 };
    const spread = segCounts.filter((v) => v > 0).length;
    let peak = 0;
    segCounts.forEach((v, i) => { if (v > segCounts[peak]) peak = i; });
    const share = segCounts[peak] / total;
    let kind = '분산';
    if (total >= 3 && (share >= 0.5 || spread <= 2)) kind = '집중';
    else if (total < 3) kind = '드문드문';
    return { kind, spread, peak, share };
  }

  function tagFileName(title, upperHits) {
    const parts = upperHits.filter((h) => h.status).map((h) => h.name + '(' + h.status + ')');
    return parts.length ? title + '_' + parts.join('_') : title;
  }

  /* ---------- 파일명 정리 / 유사도 ---------- */
  function baseTitle(fileName) {
    return String(fileName || '').replace(/\.(txt|epub)$/i, '').trim();
  }
  function normalizeTitle(fileName) {
    let s = baseTitle(fileName).toLowerCase();
    s = s.replace(/\((?:!|\?)\)/g, ' ');                  // (!) (?) 태그
    s = s.replace(/[\[\(【\{][^\]\)】\}]*[\]\)】\}]/g, ' '); // [...] (...) 부가 정보
    s = s.replace(/^[\s\d._\-#]+(?=\D)/, '');             // 앞쪽 번호
    return s.replace(/[^\p{L}\p{N}]+/gu, '');
  }
  function bigrams(s) {
    const m = new Map();
    if (s.length < 2) { if (s) m.set(s, 1); return m; }
    for (let i = 0; i < s.length - 1; i++) {
      const g = s.substr(i, 2);
      m.set(g, (m.get(g) || 0) + 1);
    }
    return m;
  }
  function dice(a, b) {
    if (!a || !b) return 0;
    const A = bigrams(a), B = bigrams(b);
    let inter = 0, ta = 0, tb = 0;
    A.forEach((v, k) => { ta += v; if (B.has(k)) inter += Math.min(v, B.get(k)); });
    B.forEach((v) => { tb += v; });
    return (2 * inter) / (ta + tb);
  }
  function commonSuffixLen(a, b) {
    let i = 0;
    while (i < a.length && i < b.length && a[a.length - 1 - i] === b[b.length - 1 - i]) i++;
    return i;
  }
  function tokens(fileName) {
    return baseTitle(fileName).toLowerCase()
      .replace(/\((?:!|\?)\)/g, ' ')
      .split(/[^\p{L}\p{N}]+/u).filter((t) => t.length >= 2);
  }
  function titleSimilarity(nameA, nameB) {
    const a = normalizeTitle(nameA), b = normalizeTitle(nameB);
    if (!a || !b) return 0;
    if (a === b) return 1;
    let score = dice(a, b);
    const [short, long] = a.length <= b.length ? [a, b] : [b, a];
    if (short.length >= 3 && long.includes(short)) score = Math.max(score, 0.85); // 한쪽이 다른 쪽에 포함
    const suf = commonSuffixLen(a, b);                                              // 뒷부분이 같음
    if (suf >= 4 && suf / short.length >= 0.6) score = Math.max(score, 0.75);
    const ta = new Set(tokens(nameA)), tb = new Set(tokens(nameB));   // 주요 제목 단어 겹침
    if (ta.size && tb.size) {
      let common = 0;
      ta.forEach((t) => { if (tb.has(t)) common++; });
      const jac = common / (ta.size + tb.size - common);
      if (common >= 1 && jac >= 0.5) score = Math.max(score, 0.6 + 0.4 * jac * 0.5);
    }
    return Math.min(score, 0.99);
  }
  function findSimilar(fileName, existing, threshold) {
    threshold = threshold == null ? 0.6 : threshold;
    const out = [];
    existing.forEach((e) => {
      const sc = titleSimilarity(fileName, e.fileName || e.title);
      if (sc >= threshold) out.push({ item: e, score: sc });
    });
    out.sort((x, y) => y.score - x.score);
    return out;
  }

  /* ---------- 텍스트 디코딩 ---------- */
  function decodeText(buf) {
    const u8 = buf instanceof Uint8Array ? buf : new Uint8Array(buf);
    if (u8.length >= 2 && u8[0] === 0xff && u8[1] === 0xfe) return new TextDecoder('utf-16le').decode(u8.subarray(2));
    if (u8.length >= 2 && u8[0] === 0xfe && u8[1] === 0xff) return new TextDecoder('utf-16be').decode(u8.subarray(2));
    try {
      return new TextDecoder('utf-8', { fatal: true }).decode(u8);
    } catch (e) {
      try { return new TextDecoder('euc-kr').decode(u8); } catch (e2) { return new TextDecoder('utf-8').decode(u8); }
    }
  }

  /* ---------- ZIP / EPUB (외부 라이브러리 없이) ---------- */
  async function inflateRaw(data) {
    const ds = new DecompressionStream('deflate-raw');
    const stream = new Blob([data]).stream().pipeThrough(ds);
    return new Uint8Array(await new Response(stream).arrayBuffer());
  }
  function readZipDirectory(u8) {
    const dv = new DataView(u8.buffer, u8.byteOffset, u8.byteLength);
    let eocd = -1;
    for (let i = u8.length - 22; i >= Math.max(0, u8.length - 65557); i--) {
      if (dv.getUint32(i, true) === 0x06054b50) { eocd = i; break; }
    }
    if (eocd < 0) throw new Error('ZIP 구조를 찾을 수 없습니다');
    const count = dv.getUint16(eocd + 10, true);
    let p = dv.getUint32(eocd + 16, true);
    const entries = new Map();
    const dec = new TextDecoder('utf-8');
    for (let i = 0; i < count; i++) {
      if (dv.getUint32(p, true) !== 0x02014b50) break;
      const flags = dv.getUint16(p + 8, true);
      const method = dv.getUint16(p + 10, true);
      const csize = dv.getUint32(p + 20, true);
      const nlen = dv.getUint16(p + 28, true);
      const elen = dv.getUint16(p + 30, true);
      const clen = dv.getUint16(p + 32, true);
      const lho = dv.getUint32(p + 42, true);
      const name = dec.decode(u8.subarray(p + 46, p + 46 + nlen));
      entries.set(name, { name, method, csize, lho, flags });
      p += 46 + nlen + elen + clen;
    }
    return { dv, entries };
  }
  async function zipRead(u8, dir, name) {
    const e = dir.entries.get(name);
    if (!e) return null;
    const dv = dir.dv;
    const nlen = dv.getUint16(e.lho + 26, true);
    const elen = dv.getUint16(e.lho + 28, true);
    const start = e.lho + 30 + nlen + elen;
    const raw = u8.subarray(start, start + e.csize);
    if (e.method === 0) return raw;
    if (e.method === 8) return inflateRaw(raw);
    throw new Error('지원하지 않는 압축 방식: ' + e.method);
  }
  function resolvePath(base, rel) {
    const parts = (base ? base.split('/').slice(0, -1) : []).concat(decodeURIComponent(rel.split('#')[0]).split('/'));
    const out = [];
    parts.forEach((x) => { if (x === '..') out.pop(); else if (x && x !== '.') out.push(x); });
    return out.join('/');
  }
  const ENT = { amp: '&', lt: '<', gt: '>', quot: '"', apos: "'", nbsp: ' ' };
  function htmlToText(html) {
    let s = String(html)
      .replace(/<head[\s\S]*?<\/head>/gi, ' ')
      .replace(/<(script|style)[\s\S]*?<\/\1>/gi, ' ')
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/<\/(p|div|h[1-6]|li|tr|section|blockquote)>/gi, '\n')
      .replace(/<[^>]+>/g, '');
    s = s.replace(/&(#x?[0-9a-f]+|[a-z]+);/gi, (m, g) => {
      if (g[0] === '#') {
        const code = g[1].toLowerCase() === 'x' ? parseInt(g.slice(2), 16) : parseInt(g.slice(1), 10);
        return code ? String.fromCodePoint(code) : m;
      }
      return ENT[g.toLowerCase()] !== undefined ? ENT[g.toLowerCase()] : m;
    });
    return s;
  }
  async function epubToText(buf) {
    const u8 = buf instanceof Uint8Array ? buf : new Uint8Array(buf);
    const dir = readZipDirectory(u8);
    const dec = new TextDecoder('utf-8');
    const container = await zipRead(u8, dir, 'META-INF/container.xml');
    let opfPath = null;
    if (container) {
      const m = /full-path\s*=\s*["']([^"']+)["']/i.exec(dec.decode(container));
      if (m) opfPath = m[1];
    }
    let order = [];
    if (opfPath && dir.entries.has(opfPath)) {
      const opf = dec.decode(await zipRead(u8, dir, opfPath));
      const manifest = new Map();
      opf.replace(/<item\b[^>]*>/gi, (tag) => {
        const id = /\bid\s*=\s*["']([^"']+)["']/i.exec(tag);
        const href = /\bhref\s*=\s*["']([^"']+)["']/i.exec(tag);
        if (id && href) manifest.set(id[1], href[1]);
        return '';
      });
      opf.replace(/<itemref\b[^>]*>/gi, (tag) => {
        const r = /\bidref\s*=\s*["']([^"']+)["']/i.exec(tag);
        if (r && manifest.has(r[1])) order.push(resolvePath(opfPath, manifest.get(r[1])));
        return '';
      });
    }
    if (!order.length) {
      order = Array.from(dir.entries.keys()).filter((n) => /\.(x?html?)$/i.test(n)).sort();
    }
    const chunks = [];
    for (const name of order) {
      if (!dir.entries.has(name)) continue;
      const data = await zipRead(u8, dir, name);
      if (data) chunks.push(htmlToText(dec.decode(data)));
    }
    return chunks.join('\n');
  }
  async function fileToText(fileName, buf) {
    if (/\.epub$/i.test(fileName)) return epubToText(buf);
    return decodeText(buf);
  }

  return {
    SEGMENTS, termsOf, termLabels, termsSignature, buildMatcher, scanText,
    statusOf, densityPer10k, distributionOf, tagFileName,
    baseTitle, normalizeTitle, titleSimilarity, findSimilar,
    decodeText, htmlToText, epubToText, fileToText,
  };
});
