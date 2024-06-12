# 基础镜像
FROM openjdk:8u302-jdk
WORKDIR /app
# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
# 拷贝jar 把可执行jar包复制到基础镜像的根目录下
ADD financial/target/financial-1.0.jar app.jar
# 设置暴露的端口号
EXPOSE 8080
# 在镜像运行为容器后执行的命令
ENTRYPOINT ["java","-jar","app.jar","-XX:+UnlockExperimentalVMOptions","XX:+UseCGroupMemoryLimitForHeap"]