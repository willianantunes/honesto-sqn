package br.com.willianantunes.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.springframework.stereotype.Component;

@Component
public class SetupCitizenDesireRoute extends RouteBuilder {
    
    private String authorizationToken;

    public static final String ROUTE_ID = SetupCitizenDesireRoute.class.getSimpleName();

    @Override
    public void configure() throws Exception {
        from("telegram:bots")
                .process(exchange -> {
//                    System.out.println(exchange.getIn().getHeaders());
//                    System.out.println(exchange.getIn().getBody());
                    // org.apache.camel.component.telegram.model.OutgoingTextMessage
                    
                })
                .to("log:INFO?showHeaders=true")
                .to("telegram:bots");
    }
}