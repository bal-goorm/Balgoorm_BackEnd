#!/bin/sh
# Docker 데몬을 백그라운드에서 실행
dockerd &

# Docker 데몬이 시작될 때까지 잠시 대기
while(! docker info > /dev/null 2>&1); do
    sleep 1
done

# 애플리케이션 실행
java -jar /app/build/libs/Balgoorm_BackEnd-0.0.1-SNAPSHOT.jar
