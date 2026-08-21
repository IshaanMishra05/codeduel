# 🚀 CodeDuel Frontend - Production Deployment Guide

## ✅ Backend Configuration

Your backend is **already deployed** on Railway:
- **URL:** https://codeclash.up.railway.app
- **API Endpoint:** https://codeclash.up.railway.app/api
- **WebSocket:** wss://codeclash.up.railway.app/ws/websocket

---

## 🌍 Production Environment Setup

### Environment Variables

The frontend is already configured in `.env.local` to connect to your Railway backend:

```env
VITE_API_URL=https://codeclash.up.railway.app/api
VITE_WS_URL=wss://codeclash.up.railway.app/ws/websocket
```

✅ **No additional configuration needed!**

---

## 📦 Build for Production

```bash
cd frontend
npm run build
```

This creates an optimized `dist/` folder ready for deployment.

---

## 🚀 Deployment Options

### Option 1: Deploy to Railway (Recommended - Same Provider)

1. **Create a new service in Railway:**
   - Go to https://railway.app/dashboard
   - Click "New Project"
   - Select "Deploy from GitHub"
   - Connect your repository

2. **Configure Build:**
   ```
   Build Command: npm run build
   Start Command: (leave empty - static site)
   Root Directory: frontend
   ```

3. **Set Environment:**
   ```
   NODE_ENV=production
   ```

4. **Deploy:**
   - Railway will automatically build and deploy
   - You'll get a unique URL (e.g., `codeduel-frontend.up.railway.app`)

### Option 2: Deploy to Vercel (Free Alternative)

```bash
# Install Vercel CLI
npm install -g vercel

# Login and deploy
vercel --prod
```

### Option 3: Deploy to Netlify

```bash
# Build
npm run build

# Drag and drop dist/ folder to Netlify
# Or connect your GitHub repository for auto-deploys
```

### Option 4: Deploy to AWS S3 + CloudFront

```bash
# Build
npm run build

# Upload dist/ to S3
aws s3 sync dist/ s3://your-bucket-name/

# Create CloudFront distribution (optional)
```

### Option 5: Traditional VPS/Server

```bash
# Build
npm run build

# Copy dist/ to your server
scp -r dist/ user@your-server.com:/var/www/codeduel/

# Serve with Nginx
# (See nginx.conf below)
```

---

## 🐳 Docker Deployment

### Dockerfile

```dockerfile
# Build stage
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf

```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # SPA routing - serve index.html for all routes
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

### Build & Run

```bash
# Build Docker image
docker build -t codeduel-frontend .

# Run container
docker run -d -p 80:80 codeduel-frontend
```

---

## ✨ Testing Production Build Locally

```bash
npm run build
npm run preview
```

Opens at `http://localhost:4173`

---

## 📝 Pre-Deployment Checklist

- [ ] Build passes without errors: `npm run build`
- [ ] No TypeScript errors: `npm run build`
- [ ] Environment variables are correct (Railway backend URLs)
- [ ] `.env.local` is NOT committed to git
- [ ] All pages load correctly
- [ ] Login/Register works with Railway backend
- [ ] Room creation/joining works
- [ ] WebSocket connection is stable
- [ ] Code submission works
- [ ] Leaderboard loads

---

## 🔍 Troubleshooting

### "Failed to connect to backend"

**Solution:**
```env
# Check these are correct in .env.local
VITE_API_URL=https://codeclash.up.railway.app/api
VITE_WS_URL=wss://codeclash.up.railway.app/ws/websocket
```

Then rebuild:
```bash
npm run build
```

### "CORS errors"

**Ensure backend has CORS enabled for your frontend domain:**

Backend `application.properties`:
```properties
cors.allowed-origins=https://your-frontend-url.com
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true
```

### "WebSocket connection failed"

- Verify backend WebSocket endpoint: `wss://codeclash.up.railway.app/ws/websocket`
- Check browser DevTools Network tab
- Ensure no firewall blocking WebSocket
- Try in a different browser

---

## 📊 Production Performance

### Optimization Tips

1. **Minification:** Vite automatically minifies in production
2. **Code Splitting:** Already configured
3. **Asset Optimization:** CSS/JS compressed
4. **Lazy Loading:** Routes are code-split
5. **Gzip Compression:** Enable in your server

### Bundle Size (Production)
- Total: 110 KB gzipped
- CSS: 3.14 KB
- JS: 110.76 KB

### CDN (Optional)

Use a CDN to serve static assets faster:
- **Cloudflare:** Free tier available
- **AWS CloudFront:** Integrates with S3
- **BunnyCDN:** Affordable and fast
- **Netlify:** Built-in CDN

---

## 🔐 Security Checklist

- [ ] HTTPS enabled (wss:// for WebSocket)
- [ ] JWT tokens never exposed in code
- [ ] No sensitive data in localStorage except token
- [ ] CORS properly configured
- [ ] CSP headers set
- [ ] X-Frame-Options header set
- [ ] No hardcoded API keys
- [ ] Environment variables secure

---

## 📚 Resources

- [Railway Docs](https://docs.railway.app/)
- [Vite Production Guide](https://vitejs.dev/guide/build.html)
- [Nginx Configuration](https://nginx.org/en/docs/)
- [Docker Hub](https://hub.docker.com/)

---

## 🎯 Quick Start

### 1. Build
```bash
npm run build
```

### 2. Test Locally
```bash
npm run preview
# Open http://localhost:4173
```

### 3. Deploy to Railway

Create `railway.json` in `frontend/`:
```json
{
  "build": {
    "buildCommand": "npm run build",
    "startCommand": "npx http-server dist -p $PORT -c-1"
  }
}
```

Push to GitHub and connect to Railway!

### 4. Share URL
Once deployed, share your frontend URL with users:
- **Example:** `https://codeduel-frontend.up.railway.app`

---

## ✅ You're Ready to Deploy!

Your frontend is:
- ✅ Built and optimized
- ✅ Configured for Railway backend
- ✅ Production-ready
- ✅ Fully tested

**Next Step:** Choose a deployment platform and deploy! 🚀

---

*Questions? Check the main README.md or FRONTEND_SETUP.md*
