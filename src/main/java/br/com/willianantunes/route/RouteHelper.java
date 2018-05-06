package br.com.willianantunes.route;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.camel.Processor;
import org.apache.camel.component.telegram.model.IncomingMessage;

import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.model.Room;

public class RouteHelper {
    
    public static String verifyUserConversation() {
        
        return String.format("jpa:%s?namedQuery=%s&consumeDelete=%s&parameters={\"chatId\":${body.chat.id}}",
                ChatTransaction.class.getName(), ChatTransaction.NAMED_QUERY_SELECT_NOT_FINISHED_BY_CHAT_ID, false);
    }
    
    public static String verifyWhoTheUserIsWatching() {
        
        return String.format("jpa:%s?namedQuery=%s&consumeDelete=%s&parameters={\"chatId\":${body.chat.id}}",
                Room.class.getName(), Room.NAMED_QUERY_SELECT_BY_CHAT_ID, false);
    }
    
    public static Processor prepareMessageToBePersisted(String chatEndpoint) {
        
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
                .chatEndpoint(chatEndpoint).build();
            
            exchange.getIn().setBody(chatTransaction);
        };
    }

    public static Processor prepareMessageToBePersistedByProperty(String chatEndpoint, String propertyTelegramMessage) {

        return exchange -> {
            
            IncomingMessage message = (IncomingMessage) exchange.getProperty(propertyTelegramMessage);
            
            ChatTransaction chatTransaction = ChatTransaction.builder()
                .chatId(Integer.parseInt(message.getChat().getId()))
                .messageId(message.getMessageId().intValue())
                .message(message.getText())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .sentAt(LocalDateTime.ofInstant(message.getDate(), ZoneId.systemDefault()))
                .finished(false)
                .chatEndpoint(chatEndpoint).build();
            
            exchange.getIn().setBody(chatTransaction);
        };
    }
}