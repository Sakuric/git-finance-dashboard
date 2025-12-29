#!/bin/bash

#############################################
# 金融投资平台 - 阿里云轻量服务器部署脚本
# 适用于: 2核2GB内存配置
#############################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  金融投资平台 - Docker部署脚本${NC}"
echo -e "${GREEN}=========================================${NC}"

# 1. 检查Docker和Docker Compose
echo -e "\n${YELLOW}[1/8] 检查Docker环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装${NC}"
    echo "请先安装Docker: https://docs.docker.com/engine/install/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}错误: Docker Compose未安装${NC}"
    echo "请先安装Docker Compose"
    exit 1
fi

echo -e "${GREEN}✓ Docker环境检查通过${NC}"

# 2. 检查必要文件
echo -e "\n${YELLOW}[2/8] 检查必要文件...${NC}"
REQUIRED_FILES=("Dockerfile" "docker-compose.yml" "finance_db.sql" "nginx.conf")
for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo -e "${RED}错误: 缺少文件 $file${NC}"
        exit 1
    fi
done
echo -e "${GREEN}✓ 必要文件检查通过${NC}"

# 3. 检查环境配置文件
echo -e "\n${YELLOW}[3/8] 检查环境配置...${NC}"
if [ ! -f ".env" ]; then
    if [ -f ".env.production" ]; then
        echo -e "${YELLOW}未找到.env文件,复制.env.production为.env${NC}"
        cp .env.production .env
        echo -e "${RED}警告: 请编辑.env文件,配置您的密钥和密码!${NC}"
        read -p "是否现在编辑.env文件? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            ${EDITOR:-vi} .env
        else
            echo -e "${RED}请在继续之前手动编辑.env文件!${NC}"
            exit 1
        fi
    else
        echo -e "${RED}错误: 缺少环境配置文件${NC}"
        echo "请创建.env文件并配置必要的环境变量"
        exit 1
    fi
fi

# 检查LLM_API_KEY是否配置
if grep -q "your_actual_llm_api_key_here" .env 2>/dev/null; then
    echo -e "${RED}警告: LLM_API_KEY未配置!${NC}"
    read -p "是否继续部署? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
echo -e "${GREEN}✓ 环境配置检查通过${NC}"

# 4. 构建前端
echo -e "\n${YELLOW}[4/8] 构建前端应用...${NC}"
if [ -d "frontend" ]; then
    cd frontend
    if [ ! -d "node_modules" ]; then
        echo "安装前端依赖..."
        npm install
    fi
    echo "构建前端..."
    npm run build
    cd ..
    echo -e "${GREEN}✓ 前端构建完成${NC}"
else
    echo -e "${RED}错误: frontend目录不存在${NC}"
    exit 1
fi

# 5. 停止旧容器
echo -e "\n${YELLOW}[5/8] 停止旧容器...${NC}"
if docker ps -a | grep -q "finance-"; then
    docker-compose down
    echo -e "${GREEN}✓ 旧容器已停止${NC}"
else
    echo "没有运行中的容器"
fi

# 6. 清理旧镜像(可选)
read -p "是否清理旧的Docker镜像? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "\n${YELLOW}[6/8] 清理旧镜像...${NC}"
    docker system prune -f
    echo -e "${GREEN}✓ 旧镜像清理完成${NC}"
else
    echo -e "\n${YELLOW}[6/8] 跳过清理旧镜像${NC}"
fi

# 7. 构建并启动容器
echo -e "\n${YELLOW}[7/8] 构建并启动容器...${NC}"
echo "这可能需要5-10分钟,请耐心等待..."

# 使用--build强制重新构建
docker-compose up -d --build

echo -e "${GREEN}✓ 容器启动成功${NC}"

# 8. 等待服务就绪
echo -e "\n${YELLOW}[8/8] 等待服务就绪...${NC}"
echo "正在等待MySQL数据库启动..."
sleep 10

echo "正在等待后端服务启动..."
MAX_WAIT=120
WAITED=0
while [ $WAITED -lt $MAX_WAIT ]; do
    if docker exec finance-backend curl -f http://localhost:8081/actuator/health >/dev/null 2>&1; then
        echo -e "${GREEN}✓ 后端服务已就绪${NC}"
        break
    fi
    echo -n "."
    sleep 5
    WAITED=$((WAITED + 5))
done

if [ $WAITED -ge $MAX_WAIT ]; then
    echo -e "\n${RED}警告: 后端服务启动超时,请检查日志${NC}"
    echo "运行 'docker logs finance-backend' 查看详细日志"
fi

# 显示容器状态
echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}  部署完成!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
docker-compose ps

# 获取HTTP端口
HTTP_PORT=$(grep "^HTTP_PORT=" .env | cut -d '=' -f2)
HTTP_PORT=${HTTP_PORT:-80}

echo ""
echo -e "${GREEN}访问地址:${NC}"
echo -e "  前端: ${GREEN}http://您的服务器IP:${HTTP_PORT}${NC}"
echo -e "  后端: ${GREEN}http://您的服务器IP:8081${NC}"
echo ""
echo -e "${YELLOW}常用命令:${NC}"
echo "  查看日志:     docker-compose logs -f"
echo "  查看后端日志: docker logs -f finance-backend"
echo "  查看MySQL日志: docker logs -f finance-mysql"
echo "  停止服务:     docker-compose down"
echo "  重启服务:     docker-compose restart"
echo "  查看状态:     docker-compose ps"
echo ""
echo -e "${YELLOW}提示:${NC}"
echo "  - 首次启动可能需要1-2分钟初始化数据库"
echo "  - 如遇问题请检查日志: docker-compose logs"
echo "  - 确保阿里云安全组开放了${HTTP_PORT}和8081端口"
echo ""
