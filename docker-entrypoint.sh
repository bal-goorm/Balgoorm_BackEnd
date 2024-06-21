#!/bin/sh
# containerd를 백그라운드에서 실행
containerd &

# Docker 데몬을 백그라운드에서 실행
dockerd --host=unix:///var/run/docker.sock &

# Docker 데몬이 시작될 때까지 10초 대기
for i in {1..10}; do
    if docker info > /dev/null 2>&1; then
        break
    fi
    sleep 1
done

# Docker 데몬이 여전히 시작되지 않은 경우, 오류를 출력하고 종료
if ! docker info > /dev/null 2>&1; then
    echo "Docker 데몬이 시작되지 않았습니다."
    exit 1
fi

# 애플리케이션 실행
java -jar /app/build/libs/Balgoorm_BackEnd-0.0.1-SNAPSHOT.jar
