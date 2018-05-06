package br.com.willianantunes.route;

import static br.com.willianantunes.route.RouteHelper.prepareMessageToBePersistedByProperty;
import static br.com.willianantunes.route.RouteHelper.verifyWhoTheUserIsWatching;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.model.InlineKeyboardButton;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.component.telegram.model.ReplyKeyboardMarkup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.model.Politician;
import br.com.willianantunes.model.Room;

@Component
public class LetMeQuitRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = LetMeQuitRoute.class.getSimpleName();
    public static final String ROUTE_ID_AFTER_FIRST_CONTACT = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-AFTER-FIRST-CONTACT");
    
    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-RECEPTION");
    public static final String DIRECT_ENDPOINT_AFTER_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-AFTER-RECEPTION");
    
    public static final String PROPERTY_STORED_MESSAGE = "PROPERTY_STORED_MESSAGE";    
    
    @Autowired
    private Messages messages;

    @Override
    public void configure() throws Exception {
        
        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .toD(verifyWhoTheUserIsWatching())
            .choice()
                .when(simple("${body.iterator.next?.politicians.size} > 0"))
                    .process(prepareMessageWithCustomKeyboardAndStoreInProperty(PROPERTY_STORED_MESSAGE))
                    .process(prepareMessageToBePersistedByProperty(DIRECT_ENDPOINT_AFTER_RECEPTION, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE))
                    .toF("jpa:%s", ChatTransaction.class.getName())
                    .log("Inserted new ChatTransaction with ID ${body.id}")
                    .setBody(exchangeProperty(PROPERTY_STORED_MESSAGE))
                .otherwise()
                    .setBody(constant(messages.get(Messages.COMMAND_ATUAL_NO_ONE)))
                .end()
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");
        
        fromF("direct:%s", DIRECT_ENDPOINT_AFTER_RECEPTION).routeId(ROUTE_ID_AFTER_FIRST_CONTACT)
            .setBody(constant(messages.get(Messages.COMMAND_NOT_AVAILABLE)))
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");
    }

    @SuppressWarnings("unchecked")
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