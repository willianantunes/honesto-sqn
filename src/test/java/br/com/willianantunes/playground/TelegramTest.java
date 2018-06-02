package br.com.willianantunes.playground;

import br.com.willianantunes.component.Messages;
import org.apache.camel.PropertyInject;
import org.apache.camel.component.telegram.TelegramParseMode;
import org.apache.camel.component.telegram.TelegramService;
import org.apache.camel.component.telegram.TelegramServiceProvider;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@RunWith(CamelSpringBootRunner.class)
@UseAdviceWith
@SpringBootTest
public class TelegramTest {

    @PropertyInject("camel.component.telegram.authorization-token")
    private String authorizationToken;

    @Autowired
    private Messages messages;

    private String chatId = "417067134";

    @Test
    public void testMarkdownMessage() {

        TelegramService service = TelegramServiceProvider.get().getService();

        // https://core.telegram.org/bots/api#markdown-style
        OutgoingTextMessage message = new OutgoingTextMessage();
        message.setChatId(chatId);

        Object[] parameters = Arrays.asList("2017", "Flight tickets", "Não disponível",
            "GOL LINHAS AEREAS INTELIGENTES S.A", "06164253000187", "2017-12-18",
            "6470354", "1529.8", "Preço de refeição muito incomum",
            "http://www.camara.gov.br/cota-parlamentar/documentos/publ/2907/2017/teste.pdf")
            .toArray();

        message.setText(messages.get(Messages.COMMAND_RESEARCH_OUTPUT_ENTRY, parameters)
            .concat("\r\n\r\n")
            .concat(messages.get(Messages.COMMAND_RESEARCH_OUTPUT_ENTRY, parameters))
            .concat("\r\n\r\n")
            .concat(messages.get(Messages.COMMAND_RESEARCH_OUTPUT_ENTRY, parameters)));
        message.setParseMode(TelegramParseMode.MARKDOWN.getCode());
        message.setDisableWebPagePreview(true);

        service.sendMessage(authorizationToken, message);
    }
}