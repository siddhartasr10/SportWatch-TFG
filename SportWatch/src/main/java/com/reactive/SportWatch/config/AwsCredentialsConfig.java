package com.reactive.SportWatch.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
            // getFile no funciona dentro de un jar ya que no son archivos del sistema sino comprimidos.
            InputStream awsCredentialsFile = new ClassPathResource(awsFilePath).getInputStream();
            // \\R es regex para todos los tipos de newline. split usa regex por defecto en java es verda.
            String[] fileLines = new String(awsCredentialsFile.readAllBytes(), StandardCharsets.UTF_8).split("\\R");

            // BufferedReader reader = new BufferedReader((awsCredentialsFile));

            String accessKey = fileLines[0];
            String secretKey = fileLines[1];

            awsCredentialsFile.close();

            if (accessKey == null || secretKey == null) {
                throw new IllegalArgumentException("AWS credentials file must contain two lines: accessKey; and secretKey;");
            }

            return AwsBasicCredentials.create(accessKey, secretKey);
        }
        catch (IOException e) {
            log.warning("Io error occurred reading credentials from file: " + e.getMessage());
            return null;
        }
    }
}
