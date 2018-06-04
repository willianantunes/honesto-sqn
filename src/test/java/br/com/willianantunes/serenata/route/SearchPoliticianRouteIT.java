package br.com.willianantunes.serenata.route;

import br.com.willianantunes.component.Messages;
import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.model.Politician;
import br.com.willianantunes.model.Room;
import br.com.willianantunes.repository.ChatTransactionRepository;
import br.com.willianantunes.route.SetupCitizenDesireRoute;
import br.com.willianantunes.serenata.JarbasAPI;
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

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
    public void shouldSearchByTelegramQueryAndSplitEachMessageFound() {

        scenarioBuilder
            .createChatTransaction(ChatTransaction.builder().firstName("Willian").lastName("Antunes")
                .sentAt(LocalDateTime.now()).finished(false).chatId(417067134).build())
            .build();

        IncomingMessage message = TelegramTestUtil.createSampleIncommingMessageWithTextAndChatId("calheiros", "417067134");

        producerTemplate.sendBodyAndProperty("direct:" + SearchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION, message, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, message);

        Optional<ChatTransaction> chat = chatTransactionRepository.findByChatId(417067134);
        assertThat(chat.isPresent()).isTrue();
        ChatTransaction chatTransaction = chat.get();
        assertThat(chatTransaction.getFinished()).isFalse();
        assertThat(chatTransaction.getChatProperties()).containsKey(SearchPoliticianRoute.PROPERTY_NEXT_PAGE);
        assertThat(chatTransaction.getChatProperties().get(SearchPoliticianRoute.PROPERTY_NEXT_PAGE))
            .isNotBlank().contains(JarbasAPI.API_DEFAULT_URL);

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .hasSize(9).allSatisfy(e -> {

                assertThat(e.getIn().getBody()).isInstanceOf(OutgoingTextMessage.class);
                assertThat(e.getIn().getBody(OutgoingTextMessage.class).getChatId()).isNotBlank();
            }).filteredOn(e -> {

                String value = messages.get(Messages.COMMAND_RESEARCH_OUTPUT_START, message.getText());
                return e.getIn().getBody(OutgoingTextMessage.class).getText().equals(value);
            }).hasSize(1);

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .filteredOn(e -> {

                String value = messages.get(Messages.COMMAND_RESEARCH_OUTPUT_MORE_ENTRIES);
                return e.getIn().getBody(OutgoingTextMessage.class).getText().equals(value);
            }).hasSize(1);
    }

    @Test
    public void shoudlSearchByPropertyAddressAndSplitEachMessageFound() {

        Map<String, String> properties = new HashMap<>();
        properties.put(SearchPoliticianRoute.PROPERTY_NEXT_PAGE, "https://jarbas.serenata.ai/api/chamber_of_deputies/reimbursement/?limit=7&offset=7&search=gregorio");

        scenarioBuilder
            .createChatTransaction(ChatTransaction.builder().firstName("Willian").lastName("Antunes")
                .sentAt(LocalDateTime.now()).finished(false).chatProperties(properties).chatId(417067134).build())
            .build();

        IncomingMessage message = TelegramTestUtil.createSampleIncommingMessageWithTextAndChatId(messages.get(Messages.COMMAND_RESEARCH_BUTTON_MORE), "417067134");

        producerTemplate.sendBodyAndProperty("direct:" + SearchPoliticianRoute.DIRECT_ENDPOINT_RECEPTION, message, SetupCitizenDesireRoute.PROPERTY_TELEGRAM_MESSAGE, message);

        Optional<ChatTransaction> chat = chatTransactionRepository.findByChatId(417067134);
        assertThat(chat.isPresent()).isTrue();
        ChatTransaction chatTransaction = chat.get();
        assertThat(chatTransaction.getFinished()).isFalse();
        assertThat(chatTransaction.getChatProperties()).containsKey(SearchPoliticianRoute.PROPERTY_NEXT_PAGE);
        assertThat(chatTransaction.getChatProperties().get(SearchPoliticianRoute.PROPERTY_NEXT_PAGE))
            .isNotBlank().contains(JarbasAPI.API_DEFAULT_URL);

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .hasSize(9).allSatisfy(e -> {

            assertThat(e.getIn().getBody()).isInstanceOf(OutgoingTextMessage.class);
            assertThat(e.getIn().getBody(OutgoingTextMessage.class).getChatId()).isNotBlank();
        }).filteredOn(e -> {

            String value = messages.get(Messages.COMMAND_RESEARCH_OUTPUT_START, message.getText());
            return e.getIn().getBody(OutgoingTextMessage.class).getText().equals(value);
        }).hasSize(1);

        assertThat(mockedResultTelegramBotExit.getReceivedExchanges())
            .filteredOn(e -> {

                String value = messages.get(Messages.COMMAND_RESEARCH_OUTPUT_MORE_ENTRIES);
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