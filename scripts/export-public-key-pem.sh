#!/usr/bin/env bash
# Экспорт публичного сертификата для GitHub Secret SIGNATURE_PUBLIC_KEY_PEM
set -euo pipefail
KEYSTORE="${1:-src/main/resources/keystore.p12}"
ALIAS="${2:-laba6_key}"
PASS="${KEYSTORE_PASSWORD:?set KEYSTORE_PASSWORD}"

keytool -exportcert -alias "$ALIAS" -keystore "$KEYSTORE" -storepass "$PASS" -file /tmp/lab3-cert.der
openssl x509 -inform der -in /tmp/lab3-cert.der -out /tmp/lab3-cert.pem
echo "----- Copy to GitHub Secret SIGNATURE_PUBLIC_KEY_PEM -----"
cat /tmp/lab3-cert.pem
