package br.com.willianantunes.route;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.springframework.stereotype.Component;

import br.com.willianantunes.model.ChatTransaction;

import static br.com.willianantunes.route.RouteHelper.prepareChatTransactionToBeUpdatedUsingBodyMessage;

@Component
public class RedirectToResponsibleOptionRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = RedirectToResponsibleOptionRoute.class.getSimpleName();
    
    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-RECEPTION");

    public static final String PROPERTY_REDIRECT_POINTER = "PROPERTY_REDIRECT_POINTER";

    @Override
    public void configure() {

        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .process(prepareChatTransactionToBeUpdatedUsingBodyMessage(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .toF("jpa:%s&useExecuteUpdate=%s", ChatTransaction.class.getName(), true)
            .log("Redirecting user ${body.firstName} ${body.lastName} to endpoint ${body.chatEndpoint}")
            .setProperty(PROPERTY_REDIRECT_POINTER, simple("${body.chatEndpoint}"))
            .setBody(exchangeProperty(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .toD(String.format("direct:${exchangeProperty[%s]}", PROPERTY_REDIRECT_POINTER));
    }
}