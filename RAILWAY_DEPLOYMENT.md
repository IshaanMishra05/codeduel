# 🚀 Deploy CodeDuel Frontend to Railway

## ✅ Prerequisites

- GitHub account
- Railway account (free tier available at https://railway.app)
- This repository pushed to GitHub

---

## 📝 Step-by-Step Deployment

### Step 1: Connect GitHub to Railway

1. Go to https://railway.app/dashboard
2. Click **"Create New Project"**
3. Select **"Deploy from GitHub"**
4. Authorize Railway to access your GitHub
5. Select the `codeduel` repository

### Step 2: Configure Build Settings

1. Railway auto-detects `railway.json`
2. Verify these settings:
   - **Root Directory:** `frontend` (already set in railway.json)
   - **Build Command:** `npm run build`
   - **Start Command:** `npx http-server dist -p $PORT -c-1 --gzip`

3. Click **"Deploy"**

### Step 3: Set Environment Variables (Optional)

If you need to override the API/WebSocket URLs:

1. Go to your service settings
2. Click **"Variables"**
3. Add (if needed):
   ```
   VITE_API_URL=https://codeclash.up.railway.app/api
   VITE_WS_URL=wss://codeclash.up.railway.app/ws/websocket
   ```
4. Trigger a redeploy

### Step 4: Get Your URL

1. Deployment completes in 1-2 minutes
2. Railway assigns a URL like: `codeduel-frontend-production.up.railway.app`
3. Click the URL to open your frontend
4. Share this URL with users!

---

## ✅ Verify Deployment

1. **Test Login Page:**
   - Should load and allow registration/login

2. **Test Backend Connection:**
   - Try registering a new account
   - Should connect to backend at codeclash.up.railway.app

3. **Test WebSocket:**
   - Create a room and check browser console
   - Should see WebSocket connection to wss://codeclash.up.railway.app/ws/websocket

4. **Test Full Flow:**
   - Register, create room, join with another account, play match

---

## 🔄 Auto-Deploy Updates

Railway automatically redeploys when you push to GitHub:

```bash
# Make changes
git add .
git commit -m "Update frontend"
git push origin main

# Railway will automatically rebuild and deploy!
```

---

## 📊 Monitor Deployment

1. Go to Railway dashboard
2. Select your frontend service
3. View **Deployments** to see history
4. View **Logs** to debug issues
5. View **Metrics** for performance

---

## 🆘 Troubleshooting

### Build Fails
- Check `npm run build` works locally
- Verify `package.json` has all dependencies

### WebSocket Connection Fails
- Ensure `.env.local` has correct URLs
- Check backend is running at codeclash.up.railway.app
- Try clearing browser cache

### Slow Performance
- Check Railway metrics
- Consider upgrading Railway plan
- Enable gzip compression (already done in railway.json)

---

## 💡 Tips

1. **Custom Domain:** Railway allows custom domains
   - Go to Settings → Domain
   - Add your domain (e.g., codeduel.com)

2. **Logs:** View deployment logs to debug
   - Railway Dashboard → Logs tab

3. **Rollback:** Railway keeps deployment history
   - Easy rollback if something breaks

4. **Environment:** Railway auto-detects Node.js environment
   - No additional setup needed

---

## 🎯 You're Done!

Your CodeDuel frontend is now live on Railway! 🎉

**Share your URL:**
- Frontend: `https://your-service.up.railway.app`
- Backend: `https://codeclash.up.railway.app`

---

## 📚 More Resources

- [Railway Documentation](https://docs.railway.app/)
- [Static Site Deployment on Railway](https://docs.railway.app/guides/nodejs)
- [Railway CLI Guide](https://docs.railway.app/develop/cli)

---

*Questions? Check PRODUCTION_DEPLOYMENT.md for more options*
