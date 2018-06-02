package br.com.willianantunes.serenata.route;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.route.SetupCitizenDesireRoute;
import br.com.willianantunes.serenata.model.Pagination;
import br.com.willianantunes.serenata.model.Reimbursement;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.telegram.TelegramParseMode;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.willianantunes.conf.CxfRsClientConfiguration.BEAN_JARBAS_SERVICE_ENDPOINT;
import static br.com.willianantunes.conf.CxfRsClientConfiguration.BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER;
import static br.com.willianantunes.route.RouteHelper.finishChatTransaction;

@Component
public class SearchPoliticianRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = SearchPoliticianRoute.class.getSimpleName();
    public static final String ROUTE_ID_UPDATE_TRANSACTION = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE_ID_UPDATE_TRANSACTION");
    public static final String ROUTE_ID_DEAD_LETTER = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE_ID_DEAD_LETTER");

    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT_ENDPOINT_RECEPTION");
    public static final String DIRECT_ENDPOINT_DEAD_LETTER = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT_ENDPOINT_DEAD_LETTER");
    public static final String DIRECT_ENDPOINT_UPDATE_TRANSACTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT_ENDPOINT_UPDATE_TRANSACTION");

    @Autowired
    private Messages messages;

    @Override
    public void configure() throws Exception {

        errorHandler(deadLetterChannel("direct:" + DIRECT_ENDPOINT_DEAD_LETTER).useOriginalMessage());

        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .setBody(simple("${body.text}"))
            .setExchangePattern(ExchangePattern.InOut)
            .setHeader(CxfConstants.OPERATION_NAME, constant("findReimbursement"))
            .setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, constant(false))
            .toF("cxfrs:bean:%s?cxfRsEndpointConfigurer=#%s", BEAN_JARBAS_SERVICE_ENDPOINT, BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER)
            .wireTap("direct:" + DIRECT_ENDPOINT_UPDATE_TRANSACTION)
            .setExchangePattern(ExchangePattern.InOnly)
            .process(this::configureMessageOutput)
            .split(body())
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");

        fromF("direct:%s", DIRECT_ENDPOINT_UPDATE_TRANSACTION).routeId(ROUTE_ID_UPDATE_TRANSACTION)
            .toD(finishChatTransaction(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .log("ChatTransaction with ID ${body} was configured as finished");

        fromF("direct:%s", DIRECT_ENDPOINT_DEAD_LETTER).routeId(ROUTE_ID_DEAD_LETTER)
            .log(LoggingLevel.ERROR, "An error was caught and the request could not be accomplished");
    }

    private void configureMessageOutput(Exchange exchange) {

        IncomingMessage incomingMessage = exchange.getProperty(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, IncomingMessage.class);
        String chatId = incomingMessage.getChat().getId();
        Pagination pagination = exchange.getIn().getBody(Pagination.class);
        List<Reimbursement> results = pagination.getResults();

        List<OutgoingTextMessage> outgoingTextMessages = new ArrayList<>();

        if (results.isEmpty()) {

            outgoingTextMessages.add(buildMessage(Messages.COMMAND_RESEARCH_OUTPUT_NOTHING, incomingMessage.getText(), chatId));
        } else {

            outgoingTextMessages.add(buildMessage(Messages.COMMAND_RESEARCH_OUTPUT_START, incomingMessage.getText(), chatId));

            results.stream()
                .map(r -> messages.get(Messages.COMMAND_RESEARCH_OUTPUT_ENTRY, r.toDto().toParameters()))
                .map(t -> messageWithMessageAndChatId(t, chatId))
                .forEach(outgoingTextMessages::add);
        }

        exchange.getIn().setBody(outgoingTextMessages);
    }

    private OutgoingTextMessage buildMessage(String messageKey, String messageParameter, String chatId) {

        String text = messages.get(messageKey, messageParameter);
        return messageWithMessageAndChatId(text, chatId);
    }

    private OutgoingTextMessage messageWithMessageAndChatId(String text, String chatId) {

        OutgoingTextMessage outgoingTextMessage = OutgoingTextMessage.builder().text(text).build();
        outgoingTextMessage.setChatId(chatId);
        outgoingTextMessage.setParseMode(TelegramParseMode.MARKDOWN.getCode());
        outgoingTextMessage.setDisableWebPagePreview(true);

        return outgoingTextMessage;
    }
}