# 베이스 이미지로 gradle 이미지 사용
FROM krmp-d2hub-idock.9rum.cc/goorm/gradle:7.3.1-jdk17

# Docker 설치를 위한 단계
FROM docker:latest as docker

# gradle 이미지에 Docker 설치
FROM krmp-d2hub-idock.9rum.cc/goorm/gradle:7.3.1-jdk17

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y \
    iptables \
    fuse-overlayfs \
    sudo

# Docker 설치 파일을 복사
COPY --from=docker /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker /usr/local/bin/dockerd /usr/local/bin/dockerd
COPY --from=docker /usr/local/bin/docker-init /usr/local/bin/docker-init
COPY --from=docker /usr/local/bin/docker-proxy /usr/local/bin/docker-proxy
COPY --from=docker /usr/local/bin/containerd /usr/local/bin/containerd
COPY --from=docker /usr/local/bin/runc /usr/local/bin/runc

# Docker 그룹 추가
RUN groupadd -g 999 docker && usermod -aG docker gradle

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . .

# gradle 빌드 시 proxy 설정을 gradle.properties에 추가
RUN echo "systemProp.http.proxyHost=krmp-proxy.9rum.cc\nsystemProp.http.proxyPort=3128\nsystemProp.https.proxyHost=krmp-proxy.9rum.cc\nsystemProp.https.proxyPort=3128" > /root/.gradle/gradle.properties

# gradlew에 실행 권한 부여
RUN chmod +x gradlew

# Gradle 캐시를 활용하여 빌드 시간 단축
RUN ./gradlew clean build -x test

# docker-entrypoint.sh 파일 복사 및 실행 권한 부여
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Docker 데몬 시작 및 애플리케이션 시작
CMD ["docker-entrypoint.sh"]
