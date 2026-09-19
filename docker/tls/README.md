# TLS edge examples (Traefik / Caddy / nginx unprivileged)

App stays **HTTP on 8080** inside the compose network. TLS terminates at the reverse proxy.

## Why

- Connector image does not embed TLS (keeps Java 8 / ops surface small).
- `TRUSTED_PROXIES` must include the proxy peer CIDR so `X-Forwarded-For` / `X-Real-IP` are honored (SPEC §7.2).
- Never publish connector `:8080` to the host when using these stacks (proxy only).

## Shared setup

```bash
cd docker/tls
# Local certs (pick one):
#   mkcert -cert-file certs/fullchain.pem -key-file certs/privkey.pem oci8j.local localhost 127.0.0.1
#   or openssl (self-signed) — see certs/README.md

# Traefik
cp traefik/traefik.yml.example traefik/traefik.yml
cp traefik/dynamic.yml.example traefik/dynamic.yml
cp docker-compose.traefik.example.yml docker-compose.traefik.yml
# Edit Oracle + Basic Auth secrets, then:
docker compose -f docker-compose.traefik.yml up -d --build

# Caddy
# cp caddy/Caddyfile.example caddy/Caddyfile
# cp docker-compose.caddy.example.yml docker-compose.caddy.yml && docker compose -f docker-compose.caddy.yml up -d --build

# nginx-unprivileged
# cp nginx/nginx.conf.example nginx/nginx.conf
# cp docker-compose.nginx.example.yml docker-compose.nginx.yml && docker compose -f docker-compose.nginx.yml up -d --build
```

Default internal network: **`172.28.10.0/24`**. Connector env:

```yaml
TRUSTED_PROXIES: "172.28.10.0/24"
```

Host access examples:

| Stack | HTTPS URL (local) |
|-------|-------------------|
| Traefik | `https://oci8j.local/` (add hosts entry → 127.0.0.1) |
| Caddy | `https://localhost:8443/` |
| nginx | `https://localhost:8443/` |

API base path unchanged: `/api/v1/oci8j-connector/...`

## Files

| File | Role |
|------|------|
| `docker-compose.traefik.example.yml` | Traefik v3 + file certs |
| `traefik/traefik.yml.example` | Traefik static config |
| `traefik/dynamic.yml.example` | Default TLS certificate |
| `docker-compose.caddy.example.yml` | Caddy 2 + `Caddyfile` |
| `docker-compose.nginx.example.yml` | `nginxinc/nginx-unprivileged` + TLS |
| `caddy/Caddyfile.example` | Caddy reverse_proxy config |
| `nginx/nginx.conf.example` | nginx TLS + proxy headers |
| `certs/` | Mount point for `fullchain.pem` / `privkey.pem` (not committed) |

## Production notes

- Prefer real CA / ACME (Traefik `certificatesResolvers`, Caddy automatic HTTPS) over self-signed.
- Set `BASIC_AUTH_ENABLED=true` and strong credentials.
- Optionally set `ALLOWED_CIDRS` for clients; keep probes reachable from the orchestrator via `PROBES_ALLOWED_CIDRS` if locked down.
- Keep `OPENAPI_ENABLED=false` behind public TLS unless gated by auth.
