import { expect, test } from '@playwright/test';

const DIR = '/home/jovyan/work/example_folder';
const REAL = `${DIR}/화성형 그냥드림 실적(주별보고, 연계실적 샘플).xlsx`;
const SAMPLE = `${DIR}/화성형 그냥드림 물품·재고(주별, 누계 샘플).xlsx`;

async function upload(page, file: string) {
  await page.goto('/files/upload');
  await expect(page.getByRole('heading', { name: '자료 올리기' })).toBeVisible();
  await page.setInputFiles('input[type="file"]', file);
}

test('실제 실적 파일 — 주별/누계/연계 3시트를 모두 읽는다', async ({ page }) => {
  await upload(page, REAL);
  await expect(page.getByRole('heading', { name: '자료를 확인했어요' })).toBeVisible({ timeout: 20_000 });
  await expect(page.getByText('업로드할 수 있습니다')).toBeVisible();
  await expect(page.getByText('3개 자료 · 60건')).toBeVisible();
  await expect(page.getByText('이중 집계를 막기 위해 집계에서 제외', { exact: false })).toBeVisible();
  await expect(page.getByText('11개 열 중 11개 인식')).toBeVisible();
  await page.screenshot({ path: '/tmp/claude-1000/-home-jovyan-work/c8afcb1a-b01a-4a7c-b3a4-95a65cd7f5fa/scratchpad/shots/real-perf.png', fullPage: true });
});

test('실제 실적 파일 — 개인정보는 가려서 보여준다', async ({ page }) => {
  await upload(page, REAL);
  await expect(page.getByRole('heading', { name: '자료를 확인했어요' })).toBeVisible({ timeout: 20_000 });
  await page.getByRole('button', { name: /2차 의뢰 연계/ }).click();
  const table = page.getByRole('table').first();
  await expect(table.getByText('홍*동')).toBeVisible();
  await expect(table.getByText('홍길동')).toHaveCount(0);
  await expect(table.getByText('1981-04-01')).toHaveCount(0);
  await expect(table.getByText('2026-05-07').first()).toBeVisible();
  await page.screenshot({ path: '/tmp/claude-1000/-home-jovyan-work/c8afcb1a-b01a-4a7c-b3a4-95a65cd7f5fa/scratchpad/shots/real-referral.png', fullPage: true });
});

test('물품 샘플 — 병합·소계·안내시트를 처리하고 11건을 저장 대상으로 만든다', async ({ page }) => {
  await upload(page, SAMPLE);
  await expect(page.getByRole('heading', { name: '자료를 확인했어요' })).toBeVisible({ timeout: 20_000 });
  await expect(page.getByText('2개 자료 · 22건')).toBeVisible();
  await expect(page.getByText('9개 열 중 8개 인식')).toBeVisible();
  await expect(page.getByText('1개 저장 안 함')).toBeVisible();
  const table = page.getByRole('table').first();
  await expect(table.getByText('봉담읍').first()).toBeVisible();
  await expect(table.getByText('쌀(10kg)')).toBeVisible();
  await expect(table.getByText('130')).toHaveCount(0);
  await page.screenshot({ path: '/tmp/claude-1000/-home-jovyan-work/c8afcb1a-b01a-4a7c-b3a4-95a65cd7f5fa/scratchpad/shots/inventory.png', fullPage: true });
});
