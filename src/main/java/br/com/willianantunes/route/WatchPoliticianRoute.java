package br.com.willianantunes.route;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.ChatTransaction;

@Component
public class WatchPoliticianRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = WatchPoliticianRoute.class.getSimpleName();
    public static final String ROUTE_ID_AFTER_FIRST_CONTACT = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-AFTER-FIRST-CONTACT");
    
    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-RECEPTION");
    public static final String DIRECT_ENDPOINT_AFTER_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-AFTER-RECEPTION");
    
    @Autowired
    private Messages messages;    

    @Override
    public void configure() throws Exception {
        
        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .process(prepareMessageToBePersisted())
            .toF("jpa:%s", ChatTransaction.class.getName())
            .log("Inserted new ChatTransaction with ID ${body.id}")
            .setBody(constant(messages.get(Messages.COMMAND_CONFIGURAR)))
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");
        
        fromF("direct:%s", DIRECT_ENDPOINT_AFTER_RECEPTION).routeId(ROUTE_ID_AFTER_FIRST_CONTACT)
            .setBody(constant(messages.get(Messages.COMMAND_NOT_AVAILABLE)))
            .to("log:INFO?showHeaders=true")
            .to("telegram:bots");
    }

    private Processor prepareMessageToBePersisted() {
        
        return exchange -> {
            
            IncomingMessage message = exchange.getIn().getBody(IncomingMessage.class);
            
            ChatTransaction chatTransaction = ChatTransaction.builder()
                .chatId(Integer.parseInt(message.getChat().getId()))
                .messageId(message.getMessageId().intValue())
                .message(message.getText())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .sentAt(LocalDateTime.ofInstant(message.getDate(), ZoneId.systemDefault()))
                .finished(false)
                .chatEndpoint(DIRECT_ENDPOINT_AFTER_RECEPTION).build();
            
            exchange.getIn().setBody(chatTransaction);
        };
    }
}