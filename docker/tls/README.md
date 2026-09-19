# TLS edge examples (Traefik / Caddy / nginx unprivileged)

App stays **HTTP on 8080** inside the compose network. TLS terminates at the reverse proxy.

## Why

- Connector image does not embed TLS (keeps Java 8 / ops surface small).
- `TRUSTED_PROXIES` must include the proxy peer CIDR so `X-Forwarded-For` / `X-Real-IP` are honored (SPEC §7.2).
- Never publish connector `:8080` to the host when using these stacks (proxy only).

Default internal network: **`172.28.10.0/24`** → set `TRUSTED_PROXIES=172.28.10.0/24`.

API base path unchanged: `/api/v1/oci8j-connector/...`

---

## Traefik + Let's Encrypt (recommended for public hosts)

Requires: public DNS A/AAAA → this machine; **ports 80 and 443** reachable from the internet (HTTP-01).

```bash
cd docker/tls
cp traefik/traefik-le.yml.example traefik/traefik-le.yml
# Set certificatesResolvers.letsencrypt.acme.email in traefik-le.yml

cp docker-compose.traefik-le.example.yml docker-compose.traefik-le.yml
# Replace Host(`oci8j.example.com`) labels with your FQDN; edit Oracle + Basic Auth

touch traefik/acme.json && chmod 600 traefik/acme.json
docker compose -f docker-compose.traefik-le.yml up -d --build
# https://oci8j.example.com/api/v1/oci8j-connector/healthz
```

Tips:

- First run: uncomment `caServer` staging URL in `traefik-le.yml` to avoid LE rate limits; remove for production.
- `acme.json` is gitignored — never commit it.
- Example enables Basic Auth by default (`BASIC_AUTH_ENABLED=true`).

---

## Local file certs (mkcert / openssl)

```bash
cd docker/tls
# mkcert -cert-file certs/fullchain.pem -key-file certs/privkey.pem oci8j.local localhost 127.0.0.1

# Traefik (file certs)
cp traefik/traefik.yml.example traefik/traefik.yml
cp traefik/dynamic.yml.example traefik/dynamic.yml
cp docker-compose.traefik.example.yml docker-compose.traefik.yml
docker compose -f docker-compose.traefik.yml up -d --build
# https://oci8j.local/ … (hosts: 127.0.0.1 oci8j.local)

# Caddy
# cp caddy/Caddyfile.example caddy/Caddyfile
# cp docker-compose.caddy.example.yml docker-compose.caddy.yml && docker compose -f docker-compose.caddy.yml up -d --build

# nginx-unprivileged
# cp nginx/nginx.conf.example nginx/nginx.conf
# cp docker-compose.nginx.example.yml docker-compose.nginx.yml && docker compose -f docker-compose.nginx.yml up -d --build
```

| Stack | HTTPS URL |
|-------|-----------|
| Traefik LE | `https://<your-fqdn>/` |
| Traefik file | `https://oci8j.local/` |
| Caddy | `https://localhost:8443/` |
| nginx | `https://localhost:8443/` |

## Files

| File | Role |
|------|------|
| `docker-compose.traefik-le.example.yml` | Traefik v3 + **Let's Encrypt** (HTTP-01) |
| `traefik/traefik-le.yml.example` | ACME resolver + `:80`/`:443` |
| `docker-compose.traefik.example.yml` | Traefik v3 + file certs (local) |
| `traefik/traefik.yml.example` | Traefik static (file TLS) |
| `traefik/dynamic.yml.example` | Default PEM certificate |
| `docker-compose.caddy.example.yml` | Caddy 2 |
| `docker-compose.nginx.example.yml` | `nginxinc/nginx-unprivileged` |
| `caddy/Caddyfile.example` | Caddy reverse_proxy |
| `nginx/nginx.conf.example` | nginx TLS + proxy headers |
| `certs/` | Local PEM mount (not for LE) |

## Production notes

- Prefer Traefik LE or Caddy automatic HTTPS over self-signed.
- Set strong Basic Auth; keep `OPENAPI_ENABLED=false` on public edges.
- Optionally `ALLOWED_CIDRS` / `PROBES_ALLOWED_CIDRS` per SPEC §7.
