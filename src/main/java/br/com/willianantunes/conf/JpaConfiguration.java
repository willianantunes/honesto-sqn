package br.com.willianantunes.conf;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class JpaConfiguration {

    @Bean
    public DataSource dataSource() {
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:h2:mem:sptv;DB_CLOSE_DELAY=-1");
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }
}