#!/bin/bash
# Quick Start Script for CodeDuel Frontend

echo "🚀 CodeDuel Frontend - Quick Start"
echo "===================================="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16+ first."
    exit 1
fi

echo "✅ Node.js version: $(node --version)"
echo "✅ npm version: $(npm --version)"
echo ""

# Navigate to frontend directory
cd frontend

# Check if dependencies are installed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo "✅ Dependencies installed!"
else
    echo "✅ Dependencies already installed"
fi

echo ""
echo "📝 Configuration:"
echo "================"

# Check if .env.local exists
if [ ! -f ".env.local" ]; then
    echo "📋 Creating .env.local from .env.example..."
    cp .env.example .env.local
    echo "✅ .env.local created!"
    echo ""
    echo "📌 Update .env.local if your backend is not on localhost:8080"
    echo "   VITE_API_URL=http://localhost:8080/api"
    echo "   VITE_WS_URL=ws://localhost:8080/ws/websocket"
else
    echo "✅ .env.local already exists"
fi

echo ""
echo "🎯 Available Commands:"
echo "====================="
echo ""
echo "Start development server:"
echo "  npm run dev"
echo ""
echo "Build for production:"
echo "  npm run build"
echo ""
echo "Preview production build:"
echo "  npm run preview"
echo ""
echo "✨ Ready to go! Run 'npm run dev' to start developing."
