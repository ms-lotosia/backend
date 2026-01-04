# Postman CSRF Token Setup Guide

## Method 1: Automatic Extraction (Recommended)

### Login Request Tests Tab:
```javascript
// Extract CSRF token from cookies after login
const csrfToken = pm.cookies.get('csrfToken');
pm.environment.set('csrfToken', csrfToken);

// Also extract other tokens if needed
const accessToken = pm.cookies.get('accessToken');
pm.environment.set('accessToken', accessToken);
```

### Logout Request Headers:
```
X-CSRF-Token: {{csrfToken}}
```

## Method 2: Manual Extraction

1. After login, go to Cookies section (bottom left)
2. Find your domain (38.242.144.128)
3. Copy the 'csrfToken' value
4. Add header: X-CSRF-Token: [paste_value_here]

## Method 3: Dynamic Reference

If you have the token in environment:
```
X-CSRF-Token: {{csrfToken}}
```

