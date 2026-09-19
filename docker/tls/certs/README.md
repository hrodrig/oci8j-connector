# TLS certificates (local)

Place PEM files here (gitignored contents except this README):

- `fullchain.pem` — certificate (+ chain if needed)
- `privkey.pem` — private key

## mkcert (recommended for local)

```bash
mkcert -install
mkcert -cert-file fullchain.pem -key-file privkey.pem oci8j.local localhost 127.0.0.1
```

## openssl (self-signed)

```bash
openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
  -keyout privkey.pem -out fullchain.pem \
  -subj "/CN=localhost"
```

Do **not** commit real keys.
