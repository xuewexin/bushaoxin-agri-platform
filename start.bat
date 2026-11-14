@echo off
chcp 65001 >nul
echo ================================================================
echo  “不烧心”农产品直播电商产销对接平台 - 一键启动脚本
echo  品牌标语：好产品，不烧心
echo ================================================================

echo 1. 正在启动后端 Spring Boot 3 服务 (端口: 8080)...
start "AgriBackend" cmd /c "cd /d %~dp0backend && mvn spring-boot:run"

echo 2. 正在启动前端 Vue 3 + Vite 服务 (端口: 3000)...
start "AgriFrontend" cmd /c "cd /d %~dp0frontend && npm run dev"

echo ================================================================
echo  系统启动指令已发出！
echo  前端界面访问: http://localhost:3000
echo  后端接口访问: http://localhost:8080/api
echo  内置数据控制台: http://localhost:8080/h2-console
echo ================================================================
pause
