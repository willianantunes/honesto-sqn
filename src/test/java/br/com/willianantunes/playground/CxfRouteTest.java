package br.com.willianantunes.playground;


import br.com.willianantunes.serenata.model.Pagination;
import br.com.willianantunes.serenata.model.Receipt;
import br.com.willianantunes.util.HttpUtils;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.jaxrs.CxfRsEndpoint;
import org.apache.camel.component.cxf.jaxrs.CxfRsProducer;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.apache.cxf.transport.http.HttpUrlUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static br.com.willianantunes.conf.CxfRsClientConfiguration.BEAN_JARBAS_SERVICE_ENDPOINT;
import static br.com.willianantunes.conf.CxfRsClientConfiguration.BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CamelSpringBootRunner.class)
@UseAdviceWith
@SpringBootTest
public class CxfRouteTest {

    @Autowired
    private ModelCamelContext camelContext;

    @Test
    public void cxfProducerWithJarbasProxy() throws Exception {

        CxfRsProducer producer = standardPreparation();

        Exchange exchange = new ExchangeBuilder(camelContext)
            .withBody(6470354)
            .withHeader(CxfConstants.OPERATION_NAME, "reimbursementReceiptByDocumentId")
            .withHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, false)
            .withPattern(ExchangePattern.InOut).build();

        producer.process(exchange);

        assertThat(exchange.getOut().getBody())
            .isNotNull()
            .isInstanceOf(Receipt.class)
            .satisfies(r -> {
                Receipt receipt = (Receipt) r;
                assertThat(receipt.getUrl()).isEqualTo("http://www.camara.gov.br/cota-parlamentar/documentos/publ/2907/2017/6470354.pdf");
            });
    }

    @Test
    public void cxfProducerWithJarbasWithoutProxy() throws Exception {

        CxfRsProducer producer = standardPreparation();

        URL url = new URL("https://jarbas.serenata.ai/api/chamber_of_deputies/reimbursement/?limit=7&offset=7&search=gregorio");
        Map<String, List<String>> mappedQueriesString = HttpUtils.splitQuery(url);
        String path = url.getPath();

        Map<String, String> queriesString = new LinkedHashMap<String, String>();
        queriesString.put("limit", mappedQueriesString.get("limit").stream().findAny().get());
        queriesString.put("offset", mappedQueriesString.get("offset").stream().findAny().get());
        queriesString.put("search", mappedQueriesString.get("search").stream().findAny().get());


        Exchange exchange = new ExchangeBuilder(camelContext)
            .withHeader(Exchange.HTTP_METHOD, "GET")
            .withHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, true)
            .withHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Pagination.class)
            .withHeader(Exchange.HTTP_PATH, path)
            .withHeader(CxfConstants.CAMEL_CXF_RS_QUERY_MAP, queriesString)
            .withPattern(ExchangePattern.InOut).build();

        producer.process(exchange);

        assertThat(exchange.getOut().getBody()).isNotNull().isInstanceOf(Pagination.class);
    }

    private CxfRsProducer standardPreparation() throws Exception {

        String uri = String.format("cxfrs:bean:%s?cxfRsEndpointConfigurer=#%s", BEAN_JARBAS_SERVICE_ENDPOINT, BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER);

        CxfRsEndpoint endpoint = camelContext.getEndpoint(uri, CxfRsEndpoint.class);

        // To make binding available at https://github.com/apache/camel/blob/834a59910e4b6b8d089e229b39f6c8673e7c3f9a/components/camel-cxf/src/main/java/org/apache/camel/component/cxf/jaxrs/CxfRsProducer.java#L282
        endpoint.start();

        return (CxfRsProducer)endpoint.createProducer();
    }
}