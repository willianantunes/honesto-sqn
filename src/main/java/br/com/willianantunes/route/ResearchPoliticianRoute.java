package br.com.willianantunes.route;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.serenata.route.SearchPoliticianRoute;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.TelegramConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static br.com.willianantunes.route.RouteHelper.finishChatTransaction;
import static br.com.willianantunes.route.RouteHelper.prepareMessageToBePersisted;
import static br.com.willianantunes.route.RouteHelper.verifyUserConversation;

@Component
public class ResearchPoliticianRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = ResearchPoliticianRoute.class.getSimpleName();
    public static final String ROUTE_ID_AFTER_FIRST_CONTACT = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-AFTER-FIRST-CONTACT");
    public static final String ROUTE_ID_SEND_MESSAGE = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-SEND-MESSAGE");
    public static final String ROUTE_ID_WAIT_RESPONSE = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-WAIT-RESPONSE");

    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-RECEPTION");
    public static final String DIRECT_ENDPOINT_AFTER_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-AFTER-RECEPTION");
    public static final String DIRECT_ENDPOINT_SEND_MESSAGE = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-SEND-MESSAGE");
    public static final String SEDA_ENDPOINT_WAIT_RESPONSE = ROUTE_ID_FIRST_CONTACT.concat("-SEDA-WAIT-RESPONSE");

    public static final String PROPERTY_COUNTER_PROCESSING = "PROPERTY_COUNTER_PROCESSING";
    public static final String PROPERTY_DELAY_VALUE = "PROPERTY_DELAY_VALUE";
    public static final String PROPERTY_STORED_MESSAGE = "PROPERTY_STORED_MESSAGE";

    @Autowired
    private Messages messages;

    @Override
    public void configure() {

        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT).startupOrder(1)
            .process(prepareMessageToBePersisted(DIRECT_ENDPOINT_AFTER_RECEPTION))
            .toF("jpa:%s", ChatTransaction.class.getName())
            .log("Inserted new ChatTransaction with ID ${body.id}")
            .setBody(constant(messages.get(Messages.COMMAND_RESEARCH)))
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");

        fromF("direct:%s", DIRECT_ENDPOINT_AFTER_RECEPTION).routeId(ROUTE_ID_AFTER_FIRST_CONTACT).startupOrder(4)
            .multicast().parallelProcessing()
                .to(String.format("seda:%s", SEDA_ENDPOINT_WAIT_RESPONSE), String.format("direct:%s", SearchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION));

        fromF("seda:%s?concurrentConsumers=50", SEDA_ENDPOINT_WAIT_RESPONSE).routeId(ROUTE_ID_WAIT_RESPONSE).startupOrder(3)
            .process(this::createOrIncreaseCounter)
            .choice()
                .when(simpleF("${exchangeProperty[%s]} == 1", PROPERTY_COUNTER_PROCESSING))
                    .setProperty(PROPERTY_DELAY_VALUE).constant(Duration.ofSeconds(5).toMillis())
                    .setBody(constant(messages.get(Messages.COMMAND_RESEARCH_LOADING_ONE)))
                .when(simpleF("${exchangeProperty[%s]} == 2", PROPERTY_COUNTER_PROCESSING))
                    .setProperty(PROPERTY_DELAY_VALUE).constant(Duration.ofSeconds(15).toMillis())
                    .setBody(constant(messages.get(Messages.COMMAND_RESEARCH_LOADING_TWO)))
                .when(simpleF("${exchangeProperty[%s]} == 3", PROPERTY_COUNTER_PROCESSING))
                    .setProperty(PROPERTY_DELAY_VALUE).constant(Duration.ofSeconds(15).toMillis())
                    .setBody(constant(messages.get(Messages.COMMAND_RESEARCH_LOADING_THREE)))
                .otherwise()
                    .setProperty(PROPERTY_DELAY_VALUE).constant(Duration.ofSeconds(15).toMillis())
                    .setBody(constant(messages.get(Messages.COMMAND_RESEARCH_LOADING_TIMEOUT)))
                .end()
            .setProperty(PROPERTY_STORED_MESSAGE, body())
            .setHeader(TelegramConstants.TELEGRAM_CHAT_ID, simpleF("${exchangeProperty[%s].chat.id}", SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .delay(simpleF("${exchangeProperty[%s]}", PROPERTY_DELAY_VALUE))
            .toD(verifyUserConversation(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .choice()
                .when(simpleF("${body?.size} > 0 && ${exchangeProperty[%s]} > 3", PROPERTY_COUNTER_PROCESSING))
                    .toD(finishChatTransaction(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
                    .log("ChatTransaction with ID ${body} was configured as finished")
                    .setBody(exchangeProperty(PROPERTY_STORED_MESSAGE))
                    .log(LoggingLevel.INFO, "Sadly it delayed to much. Informing the user about it")
                    .toF("direct:%s", DIRECT_ENDPOINT_SEND_MESSAGE)
                .when(simple("${body?.size} > 0"))
                    .setBody(exchangeProperty(PROPERTY_STORED_MESSAGE))
                    .log(LoggingLevel.INFO, "Sending to chat platform and SEDA component")
                    .to(String.format("direct:%s", DIRECT_ENDPOINT_SEND_MESSAGE), String.format("seda:%s", SEDA_ENDPOINT_WAIT_RESPONSE))
                .otherwise()
                    .log(LoggingLevel.INFO, "Jarbas processing has been completed successfully");

        fromF("direct:%s", DIRECT_ENDPOINT_SEND_MESSAGE).routeId(ROUTE_ID_SEND_MESSAGE).startupOrder(2)
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");
    }

    private void createOrIncreaseCounter(Exchange exchange) {

        Optional<Integer> optionalCounter = Optional.ofNullable(exchange.getProperty(PROPERTY_COUNTER_PROCESSING, Integer.class));
        Integer counter = optionalCounter.orElse(0);

        exchange.setProperty(PROPERTY_COUNTER_PROCESSING, ++counter);
    }
}