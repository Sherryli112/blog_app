import {
  env,
  createExecutionContext,
  waitOnExecutionContext,
} from 'cloudflare:test';
import { describe, it, expect, vi } from 'vitest';
import worker from '../src/index';

const IncomingRequest = Request<unknown, IncomingRequestCfProperties>;

describe('Funtime Blog Worker', () => {
  it('轉發 /articles 請求到 Strapi 並帶上 API Key', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ data: [], meta: {} }), {
        headers: { 'Content-Type': 'application/json' },
      })
    );
    vi.stubGlobal('fetch', mockFetch);

    const request = new IncomingRequest(
      'http://worker.test/articles?pagination[pageSize]=1'
    );
    const ctx = createExecutionContext();
    await worker.fetch(
      request,
      {
        ...env,
        STRAPI_API_KEY: 'test-key',
        STRAPI_BASE_URL: 'https://mgmt.funtime.com.tw',
      },
      ctx
    );
    await waitOnExecutionContext(ctx);

    expect(mockFetch).toHaveBeenCalledWith(
      'https://mgmt.funtime.com.tw/api/articles?pagination[pageSize]=1',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer test-key',
        }),
      })
    );

    vi.unstubAllGlobals();
  });

  it('轉發 /articles/slug/my-article 請求到 Strapi', async () => {
    const mockFetch = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ data: {} }), {
        headers: { 'Content-Type': 'application/json' },
      })
    );
    vi.stubGlobal('fetch', mockFetch);

    const request = new IncomingRequest(
      'http://worker.test/articles/slug/my-article'
    );
    const ctx = createExecutionContext();
    await worker.fetch(
      request,
      {
        ...env,
        STRAPI_API_KEY: 'test-key',
        STRAPI_BASE_URL: 'https://mgmt.funtime.com.tw',
      },
      ctx
    );
    await waitOnExecutionContext(ctx);

    expect(mockFetch).toHaveBeenCalledWith(
      'https://mgmt.funtime.com.tw/api/articles/slug/my-article',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer test-key',
        }),
      })
    );

    vi.unstubAllGlobals();
  });

  it('Strapi 回傳錯誤時，Worker 回傳 502', async () => {
    const mockFetch = vi.fn().mockRejectedValue(new Error('Network error'));
    vi.stubGlobal('fetch', mockFetch);

    const request = new IncomingRequest('http://worker.test/articles');
    const ctx = createExecutionContext();
    const response = await worker.fetch(
      request,
      {
        ...env,
        STRAPI_API_KEY: 'test-key',
        STRAPI_BASE_URL: 'https://mgmt.funtime.com.tw',
      },
      ctx
    );
    await waitOnExecutionContext(ctx);

    expect(response.status).toBe(502);

    vi.unstubAllGlobals();
  });
});
