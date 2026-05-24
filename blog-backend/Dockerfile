# 使用 Eclipse Temurin 的 JDK 17 镜像作为基础镜像
FROM eclipse-temurin:17-jdk
LABEL authors="一朝风月"



# 设置工作目录（容器内）
WORKDIR /blog

# 将本地宿主机的 jar 包复制到容器中
# 请确保你的 jar 包名字是 app.jar，或者修改这里的名字
COPY  blog.jar blog.jar

# 暴露应用的端口（例如 Spring Boot 默认是 8080）
EXPOSE 8081

# 容器启动时运行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]