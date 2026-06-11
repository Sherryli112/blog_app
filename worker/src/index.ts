export interface Env {
  STRAPI_API_KEY: string;
  STRAPI_BASE_URL: string;
}

const ALLOWED_STRAPI_HOSTS = ['mgmt.funtime.com.tw', 'ft-test02.funtravel.com.tw'];

const PUBLIC_PATHS = ['/articles', '/uploads', '/ft-regions', '/blog-highlight-items'];
const AUTH_PATHS = ['/auth', '/users'];

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const base = new URL(env.STRAPI_BASE_URL);
    if (!ALLOWED_STRAPI_HOSTS.includes(base.hostname)) {
      return new Response(JSON.stringify({ error: 'Bad Gateway' }), {
        status: 502,
        headers: { 'Content-Type': 'application/json' },
      });
    }

    const url = new URL(request.url);

    const isPublic = PUBLIC_PATHS.some(p => url.pathname.startsWith(p));
    const isAuth = AUTH_PATHS.some(p => url.pathname.startsWith(p));

    if (!isPublic && !isAuth) {
      return new Response('Not Found', { status: 404 });
    }

    const isUpload = url.pathname.startsWith('/uploads');
    const strapiUrl = isUpload
      ? `${env.STRAPI_BASE_URL}${url.pathname}`
      : `${env.STRAPI_BASE_URL}/api${url.pathname}${url.search}`;

    // Auth paths: forward the user's JWT (if present); no API key.
    // Public paths: always use the API key.
    const authHeader = isAuth
      ? (request.headers.get('Authorization') ?? '')
      : `Bearer ${env.STRAPI_API_KEY}`;

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (authHeader) {
      headers['Authorization'] = authHeader;
    }

    try {
      const strapiResponse = await fetch(strapiUrl, {
        method: request.method,
        headers,
        body: ['GET', 'HEAD'].includes(request.method) ? undefined : request.body,
      });

      return new Response(strapiResponse.body, {
        status: strapiResponse.status,
        headers: {
          'Content-Type': strapiResponse.headers.get('Content-Type') ?? 'application/json',
          'Access-Control-Allow-Origin': '*',
        },
      });
    } catch {
      return new Response(JSON.stringify({ error: 'Failed to reach Strapi' }), {
        status: 502,
        headers: { 'Content-Type': 'application/json' },
      });
    }
  },
};
