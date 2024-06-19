# 도커파일 jdk17 가져오기
FROM gradle:7.2.0-jdk17 as build

# 프로젝트 디렉토리 설정
WORKDIR /home/gradle/project

# 필요한 파일들 복사
COPY ./src src

# Gradle 빌드 실행
RUN gradle bootJar

# 새로운 단계로 JDK 17 가져오기
FROM openjdk:17

# 이미지 내에서 애플리케이션 파일을 보관할 디렉토리 생성 및 설정
WORKDIR /app

# 빌드 결과물인 JAR 파일을 Docker 이미지 안으로 복사
COPY --from=build /home/gradle/project/build/libs/Balgoorm-BackEnd-0.0.1-SNAPSHOT.jar /app/

# 컨테이너가 시작될 때 실행할 명령 설정
CMD ["java", "-jar", "/app/Balgoorm-BackEnd-0.0.1-SNAPSHOT.jar"]
