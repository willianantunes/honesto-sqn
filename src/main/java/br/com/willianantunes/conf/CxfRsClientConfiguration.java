package br.com.willianantunes.conf;

import br.com.willianantunes.serenata.JarbasAPI;
import br.com.willianantunes.util.deser.LocalDateFromISODateDeserializer;
import br.com.willianantunes.util.deser.ZonedDateTimeFromISOOffsetDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.camel.component.cxf.jaxrs.CxfRsEndpointConfigurer;
import org.apache.camel.component.cxf.spring.SpringJAXRSClientFactoryBean;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.AbstractJAXRSFactoryBean;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.springframework.util.StringUtils.isEmpty;

@Configuration
public class CxfRsClientConfiguration {

    public static final String BEAN_JARBAS_SERVICE_ENDPOINT = "BEAN_JARBAS_SERVICE_ENDPOINT";
    public static final String BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER = "BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER";

    @Value("${serenata.jarbas.endpoint}")
    private String serenataJarbasEndpoint;

    @Bean(BEAN_JARBAS_SERVICE_ENDPOINT)
    public SpringJAXRSClientFactoryBean serviceEndpoint() {

        if (isEmpty(serenataJarbasEndpoint))
            serenataJarbasEndpoint = JarbasAPI.API_DEFAULT_URL;

        SpringJAXRSClientFactoryBean clientFactoryBean = new SpringJAXRSClientFactoryBean();
        clientFactoryBean.setAddress(serenataJarbasEndpoint);
        clientFactoryBean.setServiceClass(JarbasAPI.class);
        clientFactoryBean.setProviders(Collections.singletonList(providerByCustomObjectMapper()));

        return clientFactoryBean;
    }

    @Bean(BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER)
    public CxfRsEndpointConfigurer serviceEndpointConfigurer() {

        return new CxfRsEndpointConfigurer() {

            @Override
            public void configure(AbstractJAXRSFactoryBean factoryBean) {

            }

            @Override
            public void configureClient(Client client) {

                ClientConfiguration clientConfiguration = WebClient.getConfig(client);

                clientConfiguration.getRequestContext().put("http.redirect.relative.uri", true);
                HTTPClientPolicy httpClientPolicy = clientConfiguration.getHttpConduit().getClient();
                httpClientPolicy.setAutoRedirect(true);
                httpClientPolicy.setReceiveTimeout(Duration.ofSeconds(45).toMillis());
            }

            @Override
            public void configureServer(Server server) {

            }
        };
    }

    private JacksonJsonProvider providerByCustomObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new LocalDateFromISODateDeserializer());
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeFromISOOffsetDateTimeDeserializer());
        mapper.registerModule(module);

        return new JacksonJsonProvider(mapper);
    }
}