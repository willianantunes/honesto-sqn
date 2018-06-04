package br.com.willianantunes.route;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.component.TransactionalContext;
import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.model.Politician;
import br.com.willianantunes.model.Room;
import br.com.willianantunes.repository.RoomRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.TelegramParseMode;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.apache.camel.component.telegram.model.InlineKeyboardButton;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.component.telegram.model.ReplyKeyboardMarkup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static br.com.willianantunes.route.RouteHelper.*;

@Component
public class LetMeQuitRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = LetMeQuitRoute.class.getSimpleName();
    public static final String ROUTE_ID_AFTER_FIRST_CONTACT = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-AFTER-FIRST-CONTACT");
    
    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-RECEPTION");
    public static final String DIRECT_ENDPOINT_AFTER_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-AFTER-RECEPTION");
    
    public static final String PROPERTY_STORED_MESSAGE = "PROPERTY_STORED_MESSAGE";

    @Autowired
    private Messages messages;
    @Autowired
    private TransactionalContext transactionalContext;
    @Autowired
    private RoomRepository roomRepository;

    @Override
    public void configure() throws Exception {
        
        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .toD(verifyWhoTheUserIsWatching())
            .choice()
                .when(simple("${body.iterator.hasNext} == true && ${body.iterator.next.politicians.size} > 0"))
                    .process(prepareMessageWithCustomKeyboardAndStoreInProperty(PROPERTY_STORED_MESSAGE))
                    .process(prepareMessageToBePersistedByProperty(DIRECT_ENDPOINT_AFTER_RECEPTION, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
                    .toF("jpa:%s", ChatTransaction.class.getName())
                    .log("Inserted new ChatTransaction with ID ${body.id}")
                    .setBody(exchangeProperty(PROPERTY_STORED_MESSAGE))
                .otherwise()
                    .setBody(constant(messages.get(Messages.COMMAND_RETIRAR_NOT_CONFIGURED)))
                .end()
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");

        fromF("direct:%s", DIRECT_ENDPOINT_AFTER_RECEPTION).routeId(ROUTE_ID_AFTER_FIRST_CONTACT)
            .toD(verifyTheUserIsWatching())
            .choice()
                .when(simple("${body?.size} > 0"))
                    .process(this::removePoliticianByItsId)
                    .log(String.format("Politician ${body[0].name} was removed from chat room ${exchangeProperty[%s].chat.id}", SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
                    .setBody(constant(messages.get(Messages.COMMAND_RETIRAR_COMPLETED)))
                .otherwise()
                    .process(this::replyWarningAboutWrongOptionSelected)
                .end()
            .setProperty(PROPERTY_STORED_MESSAGE, body())
            .toD(finishChatTransaction(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
            .log("ChatTransaction with ID ${body} was configured as finished")
            .process(this::configureReplyToRemoveCustomKeyBoard)
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");
    }

    private void configureReplyToRemoveCustomKeyBoard(Exchange exchange) {

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
            .removeKeyboard(true)
            .build();

        OutgoingTextMessage message = OutgoingTextMessage.builder()
            .text(exchange.getProperty(PROPERTY_STORED_MESSAGE, String.class))
            .parseMode(TelegramParseMode.MARKDOWN.getCode())
            .replyKeyboardMarkup(replyKeyboardMarkup).build();

        exchange.getIn().setBody(message);
    }

    private void replyWarningAboutWrongOptionSelected(Exchange exchange) {

        IncomingMessage message = (IncomingMessage) exchange.getProperty(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE);
        exchange.getIn().setBody(messages.get(Messages.COMMAND_RETIRAR_WRONG_OPTION, message.getText()));
    }

    private void removePoliticianByItsId(Exchange exchange) {

        Politician politician = exchange.getIn().getBody(Politician.class);
        IncomingMessage message = (IncomingMessage) exchange.getProperty(SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE);

        transactionalContext.execute(() -> {

            Optional<Room> room = roomRepository.findByChatId(Integer.parseInt(message.getChat().getId()));
            return room.get().getPoliticians().removeIf(p -> p.getId().equals(politician.getId()));
        });
    }

    private Processor prepareMessageWithCustomKeyboardAndStoreInProperty(String property) {
        
        return exchange -> {
            
            Optional<Room> room = exchange.getIn().getBody(List.class).stream().findFirst();
            
            room.ifPresent(r -> {
                
                List<Politician> politicians = r.getPoliticians();
                
                List<InlineKeyboardButton> buttons = transformEachPoliticianToButton(politicians);
                
                ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                        .keyboard()
                            .addOneRowByEachButton(buttons)
                            .close()
                        .oneTimeKeyboard(true)
                        .build();
                
                OutgoingTextMessage message = OutgoingTextMessage.builder()
                    .text(messages.get(Messages.COMMAND_RETIRAR_CONFIGURED))
                    .replyKeyboardMarkup(replyKeyboardMarkup).build();
                
                exchange.getProperties().put(property, message);
            });
        };
    }

    private List<InlineKeyboardButton> transformEachPoliticianToButton(List<Politician> politicians) {
        
        return politicians.stream()
            .map(p -> InlineKeyboardButton.builder().text(p.getName()).build())
            .collect(Collectors.toList());
    }
}