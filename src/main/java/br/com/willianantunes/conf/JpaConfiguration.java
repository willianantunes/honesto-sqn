package br.com.willianantunes.conf;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class JpaConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JpaConfiguration.class);
    
    @Value("${dataSource.url}")
    private String url;
    
    @Bean
    public DataSource dataSource() {
        
        logger.info("DataSource URL: {}", url);
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }
}