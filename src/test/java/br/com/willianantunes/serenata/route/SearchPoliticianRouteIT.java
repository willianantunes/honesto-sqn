package br.com.willianantunes.serenata.route;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.model.Politician;
import br.com.willianantunes.model.Room;
import br.com.willianantunes.repository.ChatTransactionRepository;
import br.com.willianantunes.route.SetupCitizenDesireRoute;
import br.com.willianantunes.support.ScenarioBuilder;
import br.com.willianantunes.support.TelegramTestUtil;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CamelSpringBootRunner.class)
@UseAdviceWith
@SpringBootTest
public class SearchPoliticianRouteIT {

    @Autowired
    private ModelCamelContext camelContext;
    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private ScenarioBuilder scenarioBuilder;
    @Autowired
    private Messages messages;

    @Autowired
    private ChatTransactionRepository chatTransactionRepository;

    @EndpointInject(uri = "mock:SearchPoliticianRouteTest-telegram-bot-exit")
    private MockEndpoint mockedResultTelegramBotExit;

    @Before
    public void setUp() throws Exception {

        scenarioBuilder.prepareCamelEnvironment(Optional.of(c -> {
            c.getRouteDefinition(SearchPoliticianRoute.ROUTE_ID_FIRST_CONTACT).adviceWith(c,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {

                        weaveByToUri("telegram:bots").replace().to("mock:SearchPoliticianRouteTest-telegram-bot-exit");
                    }
                });
        }), camelContext);
    }

    @Test
    public void shouldSearchAndSplitEachMessageFound() {

        scenarioBuilder
            .createChatTransaction(ChatTransaction.builder().firstName("Willian").lastName("Antunes")
                .sentAt(LocalDateTime.now()).finished(false).chatId(417067134).build())
            .build();

        IncomingMessage message = TelegramTestUtil.createSampleIncommingMessageWithTextAndChatId("calheiros", "417067134");

        producerTemplate.sendBodyAndProperty("direct:" + SearchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION, message, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, message);

        Optional<ChatTransaction> chat = chatTransactionRepository.findByChatId(417067134);
        assertThat(chat.isPresent()).isTrue();
        assertThat(chat.get().getFinished()).isTrue();

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .hasSize(8).allSatisfy(e -> {

                assertThat(e.getIn().getBody()).isInstanceOf(OutgoingTextMessage.class);
                assertThat(e.getIn().getBody(OutgoingTextMessage.class).getChatId()).isNotBlank();
            }).filteredOn(e -> {

                String value = messages.get(Messages.COMMAND_RESEARCH_OUTPUT_START, message.getText());
                return e.getIn().getBody(OutgoingTextMessage.class).getText().equals(value);
            }).hasSize(1);
    }

    @Test
    public void shoudlSearchAndOutputsThereIsNothing() {

        scenarioBuilder
            .createChatTransaction(ChatTransaction.builder().firstName("Willian").lastName("Antunes")
                .sentAt(LocalDateTime.now()).finished(false).chatId(417067134).build())
            .build();

        IncomingMessage message = TelegramTestUtil.createSampleIncommingMessageWithTextAndChatId("mazer rackham", "417067134");

        producerTemplate.sendBodyAndProperty("direct:" + SearchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION, message, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, message);

        Optional<ChatTransaction> chat = chatTransactionRepository.findByChatId(417067134);
        assertThat(chat.isPresent()).isTrue();
        assertThat(chat.get().getFinished()).isTrue();

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .hasSize(1).allSatisfy(e -> {

                String value = messages.get(Messages.COMMAND_RESEARCH_OUTPUT_NOTHING, message.getText());
                assertThat(e.getIn().getBody(OutgoingTextMessage.class).getText()).isEqualTo(value);
            });
    }
}