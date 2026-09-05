#!/bin/sh
set -e
printf '{"email":"%s","password":"%s"}' "$DEMO_ADMIN_EMAIL" "$DEMO_ADMIN_PASSWORD" > /tmp/login-body.json
echo "body_len=$(wc -c < /tmp/login-body.json)"
code=$(curl -sS -o /tmp/login.json -w "%{http_code}" -X POST http://127.0.0.1:8080/api/auth/login \
  -H "Content-Type: application/json" --data-binary @/tmp/login-body.json)
echo "http=$code"
if grep -q '"role":"ADMIN"' /tmp/login.json; then
  echo "login=OK"
  exit 0
fi
echo "login=FAIL"
exit 1
