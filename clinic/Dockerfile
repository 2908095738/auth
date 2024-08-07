# 基础镜像
FROM openjdk:8-jdk-alpine
WORKDIR /app

RUN apk update
RUN apk add --no-cache vim curl wget bash

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories && apk update && apk add busybox-extras

RUN echo "a deb http://mirrors.ustc.edu.cn/debian buster main contrib non-free" >> /etc/apt/sources.list \
    && echo "a deb http://mirrors.ustc.edu.cn/debian buster-backports main contrib non-free" >> /etc/apt/sources.list \
    && echo "a deb http://mirrors.ustc.edu.cn/debian buster-proposed-updates main contrib non-free" >> /etc/apt/sources.list \
    && echo "a deb http://mirrors.ustc.edu.cn/debian-security buster/updates main contrib non-free" >> /etc/apt/sources.list \
    && apt-get clean \
    && apt-get update

RUN apt-get install yum

RUN yum install -y fontconfig mkfontscale

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
ENV LANG C.UTF-8
# 拷贝jar 把可执行jar包复制到基础镜像的根目录下
ADD clinic/target/clinic-1.0.jar app.jar
ADD clinic/target/classes/templates /app/templates
ADD clinic/target/classes/diagnosis /app/diagnosis
ADD clinic/target/classes/preview /app/preview


#&& apk --no-cache add ttf-dejavu fontconfig
COPY clinic/fonts /usr/share/fonts
RUN fc-cache -f -v

# 设置暴露的端口号
EXPOSE 8080
# 在镜像运行为容器后执行的命令
ENTRYPOINT ["java","-jar","app.jar","-XX:+UnlockExperimentalVMOptions","XX:+UseCGroupMemoryLimitForHeap", " --spring.config.location=/app/bootstrap.properties"]