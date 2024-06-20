# 베이스 이미지로 gradle 이미지 사용
FROM krmp-d2hub-idock.9rum.cc/goorm/gradle:7.3.1-jdk17

# Docker 설치를 위한 단계
FROM docker:latest as docker

# gradle 이미지에 Docker 설치
FROM krmp-d2hub-idock.9rum.cc/goorm/gradle:7.3.1-jdk17

# Docker 설치 파일을 복사
COPY --from=docker /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker /usr/local/bin/dockerd /usr/local/bin/dockerd
COPY --from=docker /usr/local/bin/docker-init /usr/local/bin/docker-init
COPY --from/docker /usr/local/bin/docker-proxy /usr/local/bin/docker-proxy

# Docker 데몬 실행에 필요한 추가 파일 복사
COPY --from=docker /etc/docker/daemon.json /etc/docker/daemon.json

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

# Docker 데몬 실행
CMD ["dockerd"]
CMD ["java", "-jar", "/app/build/libs/Balgoorm_BackEnd-0.0.1-SNAPSHOT.jar"]
