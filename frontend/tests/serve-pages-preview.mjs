import { createServer } from 'node:http';
import { createReadStream, existsSync, statSync } from 'node:fs';
import { extname, join, normalize, resolve, sep } from 'node:path';

const port = Number(process.env.PORT || 4173);
const host = process.env.HOST || '127.0.0.1';
const basePath = '/FlowDash/';
const distDir = resolve('dist');
const contentTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml'
};

function resolveDistPath(requestPath) {
  if (!requestPath.startsWith(basePath)) {
    return null;
  }

  const relativePath = decodeURIComponent(requestPath.slice(basePath.length));
  const normalizedPath = normalize(relativePath || 'index.html');
  const candidate = resolve(join(distDir, normalizedPath));

  if (candidate !== distDir && !candidate.startsWith(`${distDir}${sep}`)) {
    return null;
  }

  if (existsSync(candidate) && statSync(candidate).isFile()) {
    return candidate;
  }

  return join(distDir, 'index.html');
}

createServer((request, response) => {
  const url = new URL(request.url || '/', `http://${host}:${port}`);
  const filePath = resolveDistPath(url.pathname);

  if (!filePath) {
    response.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
    response.end('Not found');
    return;
  }

  response.writeHead(200, {
    'Content-Type': contentTypes[extname(filePath)] || 'application/octet-stream'
  });
  createReadStream(filePath).pipe(response);
}).listen(port, host, () => {
  process.stdout.write(`FlowDash Pages preview listening at http://${host}:${port}${basePath}\n`);
});
