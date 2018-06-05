package br.com.willianantunes.serenata.route;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.route.ResearchPoliticianRoute;
import br.com.willianantunes.route.SetupCitizenDesireRoute;
import br.com.willianantunes.serenata.model.Pagination;
import br.com.willianantunes.serenata.model.Reimbursement;
import br.com.willianantunes.util.HttpUtils;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.telegram.TelegramParseMode;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.apache.camel.component.telegram.model.InlineKeyboardButton;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.component.telegram.model.ReplyKeyboardMarkup;
import org.hibernate.collection.internal.PersistentMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

import static br.com.willianantunes.conf.CxfRsClientConfiguration.BEAN_JARBAS_SERVICE_ENDPOINT;
import static br.com.willianantunes.conf.CxfRsClientConfiguration.BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER;
import static br.com.willianantunes.route.RouteHelper.finishChatTransaction;
import static br.com.willianantunes.route.RouteHelper.prepareChatTransactionToBeUpdatedWithCustomProperties;
import static br.com.willianantunes.route.RouteHelper.verifyUserConversation;
import static br.com.willianantunes.route.SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE;

@Component
public class SearchPoliticianRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = SearchPoliticianRoute.class.getSimpleName();
    public static final String ROUTE_ID_UPDATE_TRANSACTION = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE_ID_UPDATE_TRANSACTION");
    public static final String ROUTE_ID_DEAD_LETTER = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE_ID_DEAD_LETTER");

    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT_ENDPOINT_RECEPTION");
    public static final String DIRECT_ENDPOINT_DEAD_LETTER = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT_ENDPOINT_DEAD_LETTER");
    public static final String DIRECT_ENDPOINT_UPDATE_TRANSACTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT_ENDPOINT_UPDATE_TRANSACTION");

    public static final String PROPERTY_TEXT_SEARCH = "PROPERTY_TEXT_SEARCH";
    public static final String PROPERTY_NEXT_PAGE = "PROPERTY_NEXT_PAGE";
    public static final String PROPERTY_CHAT_TRANSACTION = "PROPERTY_CHAT_TRANSACTION";

    @Autowired
    private Messages messages;

    @Override
    public void configure() throws Exception {

        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .toD(verifyUserConversation(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .setBody(simple("${body[0]}"))
            .choice()
                .when(simpleF("${body.chatProperties?.size} != 0", PROPERTY_NEXT_PAGE))
                    .setProperty(PROPERTY_CHAT_TRANSACTION, simple("${body}"))
                    .process(preparaToCallServiceByProperty())
                .otherwise()
                    .setProperty(PROPERTY_TEXT_SEARCH, simple("${body.message}"))
                    .setBody(exchangeProperty(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
                    .setBody(simple("${body.text}"))
                    .setHeader(CxfConstants.OPERATION_NAME, constant("findReimbursement"))
                    .setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, constant(false))
                .end()
            .setExchangePattern(ExchangePattern.InOut)
            .toF("cxfrs:bean:%s?cxfRsEndpointConfigurer=#%s", BEAN_JARBAS_SERVICE_ENDPOINT, BEAN_JARBAS_SERVICE_ENDPOINT_CONFIGURER)
            .setProperty(PROPERTY_NEXT_PAGE, simple("${body.next}"))
            .wireTap("direct:" + DIRECT_ENDPOINT_UPDATE_TRANSACTION)
            .setExchangePattern(ExchangePattern.InOnly)
            .process(this::configureMessageOutput)
            .split(body())
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");

        fromF("direct:%s", DIRECT_ENDPOINT_UPDATE_TRANSACTION).routeId(ROUTE_ID_UPDATE_TRANSACTION)
            .choice()
                .when(simpleF("${exchangeProperty[%s]} != null", PROPERTY_NEXT_PAGE))
                    .toD(verifyUserConversation(PROPERTY_TELEGRAM_MESSAGE))
                    .process(prepareChatTransactionToBeUpdatedWithCustomProperties(PROPERTY_TELEGRAM_MESSAGE, ResearchPoliticianRoute.DIRECT_ENDPOINT_AFTER_RECEPTION, PROPERTY_NEXT_PAGE, PROPERTY_TEXT_SEARCH))
                    .toF("jpa:%s&useExecuteUpdate=%s", ChatTransaction.class.getName(), true)
                    .log("Property created to be used by the endpoint ${body.chatEndpoint}. Content: ${body.chatProperties}")
                .otherwise()
                    .toD(finishChatTransaction(PROPERTY_TELEGRAM_MESSAGE))
                    .log("ChatTransaction with ID ${body} was configured as finished");
    }

    private Processor preparaToCallServiceByProperty() {

        return exchange -> {

            final String QUERY_STRING_LIMIT = "limit";
            final String QUERY_STRING_OFFSET = "offset";
            final String QUERY_STRING_SEARCH = "search";

            ChatTransaction chatTransaction = exchange.getIn().getBody(ChatTransaction.class);
            Object value = chatTransaction.getChatProperties().get(PROPERTY_NEXT_PAGE);
            Message message = exchange.getIn();
            URL url = new URL(value.toString());
            Map<String, List<String>> mappedQueriesString = HttpUtils.splitQuery(url);

            String path = url.getPath();
            Map<String, String> queriesString = new LinkedHashMap<String, String>();
            queriesString.put(QUERY_STRING_LIMIT, mappedQueriesString.get(QUERY_STRING_LIMIT).stream().findAny().get());
            queriesString.put(QUERY_STRING_OFFSET, mappedQueriesString.get(QUERY_STRING_OFFSET).stream().findAny().get());
            queriesString.put(QUERY_STRING_SEARCH, mappedQueriesString.get(QUERY_STRING_SEARCH).stream().findAny().get());

            message.setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API, true);
            message.setHeader(Exchange.HTTP_METHOD, "GET");
            message.setHeader(Exchange.HTTP_PATH, path);
            message.setHeader(CxfConstants.CAMEL_CXF_RS_QUERY_MAP, queriesString);
            message.setHeader(CxfConstants.CAMEL_CXF_RS_RESPONSE_CLASS, Pagination.class);
        };
    }

    private void configureMessageOutput(Exchange exchange) {

        IncomingMessage incomingMessage = exchange.getProperty(PROPERTY_TELEGRAM_MESSAGE, IncomingMessage.class);
        Optional<ChatTransaction> optionalChatTransaction = Optional.ofNullable(exchange.getProperty(PROPERTY_CHAT_TRANSACTION, ChatTransaction.class));
        Pagination pagination = exchange.getIn().getBody(Pagination.class);

        String textParameter = null;
        String keyFirstMessage = null;

        if (optionalChatTransaction.isPresent()) {

            keyFirstMessage = Messages.COMMAND_RESEARCH_OUTPUT_MORE;
            textParameter = optionalChatTransaction.get().getChatProperties().get(PROPERTY_TEXT_SEARCH);
        } else {

            textParameter = incomingMessage.getText();
            keyFirstMessage = Messages.COMMAND_RESEARCH_OUTPUT_START;
        }

        String chatId = incomingMessage.getChat().getId();
        List<Reimbursement> results = pagination.getResults();

        List<OutgoingTextMessage> outgoingTextMessages = new ArrayList<>();

        if (results.isEmpty()) {

            outgoingTextMessages.add(buildMessage(Messages.COMMAND_RESEARCH_OUTPUT_NOTHING, textParameter, chatId));
        } else {

            outgoingTextMessages.add(buildMessage(keyFirstMessage, textParameter, chatId));

            results.stream()
                .map(r -> messages.get(Messages.COMMAND_RESEARCH_OUTPUT_ENTRY, r.toDto().toParameters()))
                .map(t -> messageWithMessageAndChatId(t, chatId))
                .forEach(outgoingTextMessages::add);

            Optional.ofNullable(pagination.getNext()).ifPresent(link -> {

                List<InlineKeyboardButton> buttons = buttonsToAskTheUser();

                ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                    .keyboard()
                        .addOneRowByEachButton(buttons)
                        .close()
                    .oneTimeKeyboard(true)
                    .build();

                OutgoingTextMessage message = OutgoingTextMessage.builder()
                    .text(messages.get(Messages.COMMAND_RESEARCH_OUTPUT_MORE_ENTRIES))
                    .replyKeyboardMarkup(replyKeyboardMarkup).build();
                message.setChatId(chatId);

                outgoingTextMessages.add(message);
                exchange.setProperty(PROPERTY_NEXT_PAGE, link);
            });
        }

        exchange.getIn().setBody(outgoingTextMessages);
    }

    private List<InlineKeyboardButton> buttonsToAskTheUser() {

        InlineKeyboardButton showMore = InlineKeyboardButton.builder().text(messages.get(Messages.COMMAND_RESEARCH_BUTTON_MORE)).build();
        InlineKeyboardButton stopResearch = InlineKeyboardButton.builder().text(messages.get(Messages.COMMAND_RESEARCH_BUTTON_SATISFIED)).build();

        return Arrays.asList(showMore, stopResearch);
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