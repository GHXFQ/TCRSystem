@echo off
chcp 65001
echo 正在启动TCR系统...




:: 启动前端服务
echo 正在启动前端服务...
start cmd /k "cd frontend && npm install && npm run serve"

:: 等待5秒
timeout /t 5

:: 启动后端服务
echo 正在启动后端服务...
start cmd /k "cd backend && mvn clean install -DskipTests && mvn spring-boot:run"

echo 系统启动完成！
echo 前端地址：http://localhost:3030
echo 后端地址：http://localhost:8080

pause 