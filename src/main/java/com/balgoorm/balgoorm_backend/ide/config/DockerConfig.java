package com.balgoorm.balgoorm_backend.ide.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConfig {

    @Bean
    public DockerClient dockerClient() {
        DefaultDockerClientConfig config =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withDockerTlsVerify(false)
                        .withRegistryUrl("krmp-d2hub.9rum.cc/")
                        .withDockerHost("unix:///var/run/docker.sock")
                        .build();

        DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        return DockerClientImpl.getInstance(config, dockerHttpClient);
    }
}
