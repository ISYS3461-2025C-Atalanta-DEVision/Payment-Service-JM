package com.devision.jm.payment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        logger.info("Configuring MongoDB Transaction Manager");
        return new MongoTransactionManager(dbFactory);
    }
}
