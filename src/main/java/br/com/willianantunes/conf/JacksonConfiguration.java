package br.com.willianantunes.conf;

import javax.annotation.PostConstruct;

import org.apache.camel.component.jackson.springboot.JacksonDataFormatConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfiguration {

    @Autowired
    private JacksonDataFormatConfiguration configuration;

    @PostConstruct
    public void setUp() {
        
        configuration.setDisableFeatures(String.format("%s,%s", 
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.name(),
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS.name()));
        configuration.setModuleClassNames(JavaTimeModule.class.getName());
        configuration.setPrettyPrint(true);
    }
}