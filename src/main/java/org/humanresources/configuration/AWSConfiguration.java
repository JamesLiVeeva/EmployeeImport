package org.humanresources.configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

    private static final String AWS_ACCESS_KEY = "AWS_Access_Key";
    private static final String AWS_ACCESS_SECRET = "AWS_Access_Secret";

    @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(getAWSCredentials()))
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
    }

    private BasicAWSCredentials getAWSCredentials() {
        return new BasicAWSCredentials(System.getenv(AWS_ACCESS_KEY), System.getenv(AWS_ACCESS_SECRET));
    }
}