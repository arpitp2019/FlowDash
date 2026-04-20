const jsonHeaders = {
  'Content-Type': 'application/json'
};

const safeMethods = new Set(['GET', 'HEAD', 'OPTIONS']);

let csrfTokenState = null;
let csrfTokenPromise = null;

async function request(path, options = {}) {
  const method = options.method || 'GET';
  const csrfHeaders = await csrfHeadersFor(method);
  const response = await fetch(path, {
    credentials: 'include',
    ...options,
    headers: {
      ...jsonHeaders,
      ...csrfHeaders,
      ...(options.headers || {})
    }
  });

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const body = contentType.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    if (response.status === 403 && !safeMethods.has(method.toUpperCase())) {
      csrfTokenState = null;
    }
    const message = typeof body === 'string' ? body : body?.message || response.statusText;
    throw new Error(message || 'Request failed');
  }

  return body;
}

export async function apiGetMe() {
  return request('/api/me', { method: 'GET', headers: {} });
}

export async function apiLogin(payload) {
  return request('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) });
}

export async function apiRegister(payload) {
  return request('/api/auth/register', { method: 'POST', body: JSON.stringify(payload) });
}

export async function apiLogout() {
  const response = await request('/api/auth/logout', { method: 'POST', headers: {} });
  csrfTokenState = null;
  return response;
}

export function apiList(resource) {
  return request(resource, { method: 'GET', headers: {} });
}

export function apiCreate(resource, payload) {
  return request(resource, { method: 'POST', body: JSON.stringify(payload) });
}

export function apiUpdate(resource, id, payload) {
  return request(`${resource}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
}

export function apiDelete(resource, id) {
  return request(`${resource}/${id}`, { method: 'DELETE', headers: {} });
}

export async function apiCreateDecisionThread(payload) {
  return request('/api/decisions', { method: 'POST', body: JSON.stringify(payload) });
}

export async function apiListDecisionThreads() {
  return request('/api/decisions', { method: 'GET', headers: {} });
}

export async function apiListDecisionMessages(threadId) {
  return request(`/api/decisions/${threadId}/messages`, { method: 'GET', headers: {} });
}

export async function apiDeleteDecisionThread(threadId) {
  return request(`/api/decisions/${threadId}`, { method: 'DELETE', headers: {} });
}

export async function streamDecisionChat(payload, onChunk) {
  const csrfHeaders = await csrfHeadersFor('POST');
  const response = await fetch('/api/ai/chat', {
    method: 'POST',
    credentials: 'include',
    headers: {
      ...jsonHeaders,
      ...csrfHeaders,
      Accept: 'text/event-stream'
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok || !response.body) {
    const text = await response.text();
    throw new Error(text || response.statusText);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let fullText = '';

  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });

    let separatorIndex = buffer.indexOf('\n\n');
    while (separatorIndex >= 0) {
      const eventBlock = buffer.slice(0, separatorIndex).trim();
      buffer = buffer.slice(separatorIndex + 2);
      separatorIndex = buffer.indexOf('\n\n');

      if (!eventBlock) {
        continue;
      }

      const dataLine = eventBlock
        .split('\n')
        .find((line) => line.startsWith('data:'));
      if (!dataLine) {
        continue;
      }

      const rawData = dataLine.replace(/^data:\s?/, '');
      const parsed = tryParseJson(rawData);
      if (parsed?.content) {
        fullText += parsed.content;
        onChunk?.(parsed);
      }
      if (parsed?.done) {
        return fullText;
      }
    }
  }

  return fullText;
}

function tryParseJson(value) {
  try {
    return JSON.parse(value);
  } catch {
    return { content: value };
  }
}

async function csrfHeadersFor(method) {
  if (safeMethods.has(method.toUpperCase())) {
    return {};
  }
  const csrf = await ensureCsrfToken();
  return csrf?.headerName && csrf?.token ? { [csrf.headerName]: csrf.token } : {};
}

async function ensureCsrfToken() {
  if (csrfTokenState) {
    return csrfTokenState;
  }
  if (!csrfTokenPromise) {
    csrfTokenPromise = fetch('/api/auth/csrf', {
      credentials: 'include',
      headers: {
        Accept: 'application/json'
      }
    })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error('Unable to initialize security token');
        }
        return response.json();
      })
      .then((token) => {
        csrfTokenState = token;
        return token;
      })
      .finally(() => {
        csrfTokenPromise = null;
      });
  }
  return csrfTokenPromise;
}
