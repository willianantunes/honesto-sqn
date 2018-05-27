package br.com.willianantunes.serenata;

import br.com.willianantunes.serenata.model.Pagination;
import br.com.willianantunes.serenata.model.Receipt;
import br.com.willianantunes.serenata.model.Reimbursement;
import br.com.willianantunes.util.deser.LocalDateFromISODateDeserializer;
import br.com.willianantunes.util.deser.ZonedDateTimeFromISOOffsetDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JarbasAPIIT {

    private JarbasAPI api;

    private Integer documentIdEdBo = 6470354;
    private Integer documentIdJaVa = 6094149;

    @Before
    public void setUp() {

        api = JAXRSClientFactory.create(JarbasAPI.API_DEFAULT_URL, JarbasAPI.class, Collections.singletonList(providerByCustomObjectMapper()));

        ClientConfiguration clientConfiguration = WebClient.getConfig(api);
        clientConfiguration.getRequestContext().put("http.redirect.relative.uri", true);
        HTTPClientPolicy httpClientPolicy = clientConfiguration.getHttpConduit().getClient();
        httpClientPolicy.setAutoRedirect(true);
        clientConfiguration.getInInterceptors().add(new LoggingInInterceptor());
        clientConfiguration.getInFaultInterceptors().add(new LoggingInInterceptor());
        clientConfiguration.getOutInterceptors().add(new LoggingOutInterceptor());
    }

    @Test
    public void retrieveReimbursement() {

        Reimbursement reimbursement = api.reimbursementByDocumentId(documentIdEdBo);

        assertThat(reimbursement).isNotNull().satisfies(r -> {
            assertThat(r.getReceipt().getUrl()).isEqualTo("http://www.camara.gov.br/cota-parlamentar/documentos/publ/2907/2017/6470354.pdf");
            assertThat(r.congresspersonId).isEqualTo(92346);
        });
    }

    @Test
    public void retrieveReimbursementReceipt() {

        Receipt receipt = api.reimbursementReceiptByDocumentId(documentIdJaVa);

        assertThat(receipt).isNotNull().satisfies(r -> {
            assertThat(r.getUrl()).isEqualTo("http://www.camara.gov.br/cota-parlamentar/documentos/publ/2908/2016/6094149.pdf");
        });
    }

    @Test
    public void searchReimbursement() {

        Pagination pagination = api.findReimbursement("mello");

        assertThat(pagination).isNotNull().satisfies(p -> {

            assertThat(p.getNext()).isNotBlank();
            assertThat(p.results.stream().findAny()).isNotEmpty();
        });
    }

    @Test
    public void searchReimbursementWithCustomYear() {

        Pagination pagination = api.findReimbursementWithYear("romero", 2015);

        assertThat(pagination).isNotNull().satisfies(p -> {

            assertThat(p.getNext()).isNotBlank();
            assertThat(p.results.stream().filter(r -> r.getSupplier().equalsIgnoreCase("POSTO ROMERO"))
                .findAny()).isNotEmpty();
        });
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