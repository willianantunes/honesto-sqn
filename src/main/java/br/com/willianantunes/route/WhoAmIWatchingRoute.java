package br.com.willianantunes.route;

import static br.com.willianantunes.route.RouteHelper.verifyWhoTheUserIsWatching;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.Room;

@Component
public class WhoAmIWatchingRoute extends RouteBuilder {
    
    public static final String ROUTE_ID_FIRST_CONTACT = WhoAmIWatchingRoute.class.getSimpleName();
    
    public static final String DIRECT_ENDPOINT_RECEPTION = ROUTE_ID_FIRST_CONTACT.concat("-DIRECT-RECEPTION");
    
    @Autowired
    private Messages messages;    

    @Override
    public void configure() throws Exception {
        
        fromF("direct:%s", DIRECT_ENDPOINT_RECEPTION).routeId(ROUTE_ID_FIRST_CONTACT)
            .toD(verifyWhoTheUserIsWatching())
            .choice()
                .when(simple("${body.iterator.next?.politicians.size} > 0"))
                    .process(preparaBodyWithPoliticiansList())
                .otherwise()
                    .setBody(constant(messages.get(Messages.COMMAND_ATUAL_NO_ONE)))
                .end()
            .to("log:INFO?showHeaders=true")        
            .to("telegram:bots");
    }

    @SuppressWarnings("unchecked")
    private Processor preparaBodyWithPoliticiansList() {
        
        return exchange -> {
            
            Optional<Room> room = exchange.getIn().getBody(List.class).stream().findFirst();
            
            room.ifPresent(r -> {
                
                String politicians = r.getPoliticians().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.joining("\r\n"));
                    
                exchange.getIn().setBody(messages.get(Messages.COMMAND_ATUAL, politicians)); 
            });
        };
    }
}