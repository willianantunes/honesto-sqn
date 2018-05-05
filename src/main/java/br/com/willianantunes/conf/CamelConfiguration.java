package br.com.willianantunes.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.willianantunes.converter.StringToMapTypeConverter;

@Configuration
public class CamelConfiguration {

    @Bean
    public StringToMapTypeConverter stringToMapTypeConverter() {
        
        return new StringToMapTypeConverter();
    }
}