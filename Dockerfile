# 多阶段构建 - 后端
FROM maven:3.8-openjdk-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 多阶段构建 - 前端
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# 最终运行镜像
FROM openjdk:17-slim
WORKDIR /app

# 安装必要工具
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制后端JAR包
COPY --from=backend-build /app/target/*.jar app.jar

# 复制前端构建产物
COPY --from=frontend-build /app/dist /app/static

# 暴露端口
EXPOSE 8081

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]
