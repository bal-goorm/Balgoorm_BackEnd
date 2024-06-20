#!/bin/sh
# containerd를 백그라운드에서 실행
containerd &

# Docker 데몬을 백그라운드에서 실행
dockerd --host=unix:///var/run/docker.sock &

# Docker 데몬이 시작될 때까지 10초 대기
sleep 10

# 애플리케이션 실행
java -jar /app/build/libs/Balgoorm_BackEnd-0.0.1-SNAPSHOT.jar
