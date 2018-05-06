package br.com.willianantunes.route;

import static br.com.willianantunes.support.TelegramTestUtil.createSampleIncommingMessageWithTextAndChatId;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.Politician;
import br.com.willianantunes.model.Room;
import br.com.willianantunes.support.ScenarioBuilder;

@RunWith(CamelSpringBootRunner.class)
@UseAdviceWith
@SpringBootTest
public class LetMeQuitRouteTest {

    @Autowired
    private ModelCamelContext camelContext;
    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private ScenarioBuilder scenarioBuilder;
    @Autowired
    private Messages messages;    
    
    @EndpointInject(uri = "mock:telegram-bot-exit")
    private MockEndpoint mockedResultTelegramBotExit;    
    
    @Before
    public void setUp() throws Exception {
        
        if (!camelContext.getStatus().isStarted()) {
            
            prepareCamelEnvironment();
        }
    }
    
    @Test
    public void shouldUnlinkPoliticianFromUser() {
        
        scenarioBuilder
            .createRoom(Room.builder().chatId(42).build())
                .withPolitician(Politician.builder().name("Sheev Palpatine").build())
                .build();
        
        IncomingMessage message = createSampleIncommingMessageWithTextAndChatId("Sheev Palpatine", "42");
        
        producerTemplate.sendBody("direct:" + LetMeQuitRoute.DIRECT_ENDPOINT_AFTER_RECEPTION, message);
        
        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .hasSize(1).allSatisfy(e -> {
                
                String text = e.getIn().getBody(String.class);

                assertThat(text).isEqualTo(messages.get(Messages.COMMAND_NOT_AVAILABLE));
            });
    }
    
    private void prepareCamelEnvironment() throws Exception {
        
        camelContext.getRouteDefinition(SetupCitizenDesireRoute.ROUTE_ID_FIRST_CONTACT).adviceWith(camelContext,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        
                        replaceFromWith("direct:telegram-entrance");                        
                    }
                });
        
        camelContext.getRouteDefinition(LetMeQuitRoute.ROUTE_ID_AFTER_FIRST_CONTACT).adviceWith(camelContext,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        
                        weaveByToUri("telegram:bots").replace().to("mock:telegram-bot-exit");                        
                    }
                });        
        
        camelContext.start();
    }    
}
