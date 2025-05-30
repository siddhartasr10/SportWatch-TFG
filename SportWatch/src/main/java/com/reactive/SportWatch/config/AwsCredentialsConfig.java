package com.reactive.SportWatch.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

@Configuration
public class AwsCredentialsConfig {

    private static Logger log = Logger.getLogger(AwsBasicCredentials.class.getName());

    @Value("${aws.credentials.file}")
    private String awsFilePath;

    @Bean
    public AwsBasicCredentials awsCredentials() {

        try {
            File awsCredentialsFile = new ClassPathResource(awsFilePath).getFile();
            BufferedReader reader = new BufferedReader(new FileReader(awsCredentialsFile));

            String accessKey = reader.readLine();
            String secretKey = reader.readLine();
            reader.close();

            if (accessKey == null || secretKey == null) {
                throw new IllegalArgumentException("AWS credentials file must contain two lines: accessKey; and secretKey;");
            }

            reader.close();

            return AwsBasicCredentials.create(accessKey, secretKey);
        }
        catch (IOException e) {
            log.warning("Io error occurred reading credentials from file: " + e.getMessage());
            return null;
        }
    }
}
