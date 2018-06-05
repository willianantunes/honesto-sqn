package br.com.willianantunes.route;

import static br.com.willianantunes.route.RouteHelper.verifyUserConversation;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.willianantunes.component.Messages;

@Component
public class SetupCitizenDesireRoute extends RouteBuilder {

    public static final String ROUTE_ID_FIRST_CONTACT = SetupCitizenDesireRoute.class.getSimpleName();
    public static final String ROUTE_ID_OPTIONS = ROUTE_ID_FIRST_CONTACT.concat("-ROUTE-ID-OPTIONS");
    
    public static final String DIRECT_ENDPOINT_OPTIONS = SetupCitizenDesireRoute.class.getSimpleName().concat("-DIRECT-OPTIONS");
    
    public static final String PROPERTY_TELEGRAM_MESSAGE = "PROPERTY_TELEGRAM_MESSAGE";
    
    @Autowired
    private Messages messages;    

    @Override
    public void configure() throws Exception {
        
        from("telegram:bots").routeId(ROUTE_ID_FIRST_CONTACT).to("log:INFO?showHeaders=true")
            .setProperty(PROPERTY_TELEGRAM_MESSAGE, body())   
            .toD(verifyUserConversation())
            .choice()
                .when(simple("${body?.size} > 0"))
                    .setBody(simple("${body[0]}"))
                    .toF("direct:%s", RedirectToResponsibleOptionRoute.DIRECT_ENDPOINT_RECEPTION)
                .otherwise()
                    .toF("direct:%s", SetupCitizenDesireRoute.DIRECT_ENDPOINT_OPTIONS)
                .end();
            
        fromF("direct:%s", DIRECT_ENDPOINT_OPTIONS).routeId(ROUTE_ID_OPTIONS)
            .setBody(exchangeProperty(PROPERTY_TELEGRAM_MESSAGE))
            .choice()
                .when(simple("${body.text} == '/atual'"))
                    .toF("direct:%s", WhoAmIWatchingRoute.DIRECT_ENDPOINT_RECEPTION)
                .when(simple("${body.text} == '/configurar'"))
                    .toF("direct:%s", WatchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION)
                .when(simple("${body.text} == '/retirar'"))
                    .toF("direct:%s", LetMeQuitRoute.DIRECT_ENDPOINT_RECEPTION)
                .when(simple("${body.text} == '/start' || ${body.text} == '/ajuda'"))
                    .toF("direct:%s", LetMeStartRoute.DIRECT_ENDPOINT_RECEPTION)
                .when(simple("${body.text} == '/pesquisar'"))
                    .toF("direct:%s", ResearchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION)
                .otherwise()
                    .setBody(constant(messages.get(Messages.COMMAND_INVALID)))
                    .to("log:INFO?showHeaders=true")
                    .to("telegram:bots")                    
                .end();
    }
}