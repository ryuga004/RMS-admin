package com.rms.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3ClientConfig {

    @Bean
    public StaticCredentialsProvider awsCredentials(StorageProperties props) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        props.getAccessKey(),
                        props.getSecretKey()
                )
        );
    }

    @Bean
    public S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    @Bean
    public S3Client s3Client(StorageProperties props, StaticCredentialsProvider credentials) {
        return S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(credentials)
                .endpointOverride(URI.create(props.getEndpoint()))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(StorageProperties props, StaticCredentialsProvider credentials) {
        return S3Presigner.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(credentials)
                .endpointOverride(URI.create(props.getEndpoint()))
                .serviceConfiguration(s3Configuration())
                .build();
    }
}