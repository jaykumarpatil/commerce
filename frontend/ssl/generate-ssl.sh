#!/bin/bash

# Generate self-signed SSL certificate for development
openssl req -x509 -newkey rsa:2048 -keyout server.key -out server.crt -days 365 -nodes -subj "/CN=localhost" -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo "SSL certificate generated successfully!"
echo "Files created: server.key, server.crt"
