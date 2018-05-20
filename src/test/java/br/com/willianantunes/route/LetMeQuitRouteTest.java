package br.com.willianantunes.route;

import static br.com.willianantunes.support.TelegramTestUtil.createSampleIncommingMessageWithTextAndChatId;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.repository.ChatTransactionRepository;
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
import br.com.willianantunes.repository.RoomRepository;
import br.com.willianantunes.support.ScenarioBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@RunWith(CamelSpringBootRunner.class)
@UseAdviceWith
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LetMeQuitRouteTest {

    @Autowired
    private ModelCamelContext camelContext;
    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private ScenarioBuilder scenarioBuilder;
    @Autowired
    private Messages messages;    
    
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ChatTransactionRepository chatTransactionRepository;
    
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
            .createChatTransaction(ChatTransaction.builder().firstName("Willian").lastName("Antunes")
                    .sentAt(LocalDateTime.now()).finished(false).chatId(42).build())
                .build();
        
        IncomingMessage message = createSampleIncommingMessageWithTextAndChatId("Sheev Palpatine", "42");

        producerTemplate.sendBodyAndProperty("direct:" + LetMeQuitRoute.DIRECT_ENDPOINT_AFTER_RECEPTION, message, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, message);

        Optional<Room> room = roomRepository.findByChatId(42);
        Optional<ChatTransaction> chat = chatTransactionRepository.findByChatId(42);

        assertThat(room.isPresent()).isTrue();
        assertThat(room.get().getPoliticians().size()).isZero();
        assertThat(chat.isPresent()).isTrue();
        assertThat(chat.get().getFinished()).isTrue();
        
        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .hasSize(1).allSatisfy(e -> {
                
                String text = e.getIn().getBody(String.class);

                assertThat(text).isEqualTo(messages.get(Messages.COMMAND_RETIRAR_COMPLETED));
            });
    }

    @Test
    public void shouldInformWrongOption() {

        scenarioBuilder
                .createRoom(Room.builder().chatId(42).build())
                .withPolitician(Politician.builder().name("Sheev Palpatine").build())
                .createChatTransaction(ChatTransaction.builder().firstName("Willian").lastName("Antunes")
                        .sentAt(LocalDateTime.now()).finished(false).chatId(42).build())
                .build();

        IncomingMessage message = createSampleIncommingMessageWithTextAndChatId("Xeev Palpatinee", "42");

        producerTemplate.sendBodyAndProperty("direct:" + LetMeQuitRoute.DIRECT_ENDPOINT_AFTER_RECEPTION, message, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, message);

        Optional<Room> room = roomRepository.findByChatId(42);
        Optional<ChatTransaction> chat = chatTransactionRepository.findByChatId(42);

        assertThat(room.isPresent()).isTrue();
        assertThat(room.get().getPoliticians().size()).isOne();
        assertThat(chat.isPresent()).isTrue();
        assertThat(chat.get().getFinished()).isTrue();

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
                .hasSize(1).allSatisfy(e -> {

            String text = e.getIn().getBody(String.class);

            assertThat(text).isEqualTo(messages.get(Messages.COMMAND_RETIRAR_WRONG_OPTION, message.getText()));
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
