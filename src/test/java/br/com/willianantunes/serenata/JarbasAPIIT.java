package br.com.willianantunes.serenata;

import br.com.willianantunes.serenata.model.Reimbursement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Collections;

public class JarbasAPIIT {

    private JarbasAPI api;

    @Before
    public void setUp() {

        api = JAXRSClientFactory.create(JarbasAPI.API_DEFAULT_URL, JarbasAPI.class, Collections.singletonList(providerByCustomObjectMapper()));

        ClientConfiguration clientConfiguration = WebClient.getConfig(api);
        HTTPClientPolicy httpClientPolicy = clientConfiguration.getHttpConduit().getClient();
        httpClientPolicy.setCacheControl("no-cache");
        clientConfiguration.getInInterceptors().add(new LoggingInInterceptor());
        clientConfiguration.getOutInterceptors().add(new LoggingOutInterceptor());
    }

    @Test
    public void retrieveReimbursement() {

        Integer documentId = 6470354;
        Reimbursement reimbursement = api.reimbursementByDocumentId(documentId);

        Assertions.assertThat(reimbursement).isNotNull();
    }

    private JacksonJsonProvider providerByCustomObjectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return new JacksonJsonProvider(mapper);
    }
}
