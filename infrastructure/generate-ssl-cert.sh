#!/bin/bash

# SSL Certificate Generation Script for Lotosia Backend
# This script generates self-signed SSL certificates for local development
#
# Usage: ./generate-ssl-cert.sh [domain] [days]
#   domain: Domain name for the certificate (default: localhost)
#   days: Number of days the certificate is valid (default: 365)
#
# Examples:
#   ./generate-ssl-cert.sh localhost 365
#   ./generate-ssl-cert.sh api.lotosia.local 730

set -e

# Default values
DOMAIN="${1:-localhost}"
DAYS="${2:-365}"
SSL_DIR="./ssl"
KEY_FILE="$SSL_DIR/selfsigned.key"
CRT_FILE="$SSL_DIR/selfsigned.crt"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if OpenSSL is installed
if ! command -v openssl &> /dev/null; then
    print_error "OpenSSL is not installed. Please install OpenSSL to generate certificates."
    exit 1
fi

# Create SSL directory if it doesn't exist
if [ ! -d "$SSL_DIR" ]; then
    print_info "Creating SSL directory: $SSL_DIR"
    mkdir -p "$SSL_DIR"
fi

# Check if certificate files already exist
if [ -f "$KEY_FILE" ] || [ -f "$CRT_FILE" ]; then
    print_warning "SSL certificate files already exist:"
    [ -f "$KEY_FILE" ] && echo "  - $KEY_FILE"
    [ -f "$CRT_FILE" ] && echo "  - $CRT_FILE"
    echo
    read -p "Do you want to overwrite them? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Certificate generation cancelled."
        exit 0
    fi
fi

print_info "Generating self-signed SSL certificate for domain: $DOMAIN"
print_info "Certificate will be valid for: $DAYS days"
echo

# Generate private key and certificate
print_info "Generating private key and certificate..."
openssl req -x509 -newkey rsa:4096 -keyout "$KEY_FILE" -out "$CRT_FILE" -days "$DAYS" -nodes \
    -subj "/C=AZ/ST=Baku/L=Baku/O=Lotosia/OU=Development/CN=$DOMAIN" \
    -addext "subjectAltName=DNS:$DOMAIN,DNS:localhost,DNS:127.0.0.1"

# Set proper permissions
chmod 600 "$KEY_FILE"
chmod 644 "$CRT_FILE"

print_success "SSL certificate generated successfully!"
echo
print_info "Generated files:"
echo "  Private key: $KEY_FILE"
echo "  Certificate: $CRT_FILE"
echo
print_info "Security notes:"
echo "  - Private key is only readable by owner (chmod 600)"
echo "  - Certificate is readable by all (chmod 644)"
echo "  - These files are ignored by .gitignore for security"
echo "  - Never commit private keys to version control"
echo
print_info "Next steps:"
echo "  - Update your nginx.conf to use these certificate files"
echo "  - Restart your services that use SSL"
echo "  - For production, use proper CA-signed certificates"