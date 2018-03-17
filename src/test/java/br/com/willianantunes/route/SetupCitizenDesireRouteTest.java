package br.com.willianantunes.route;

import java.io.IOException;

import org.apache.camel.PropertyInject;
import org.apache.camel.component.telegram.TelegramParseMode;
import org.apache.camel.component.telegram.TelegramService;
import org.apache.camel.component.telegram.TelegramServiceProvider;
import org.apache.camel.component.telegram.model.OutgoingPhotoMessage;
import org.apache.camel.component.telegram.model.OutgoingTextMessage;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.willianantunes.support.TelegramTestUtil;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
public class SetupCitizenDesireRouteTest {
    
    @PropertyInject("camel.component.telegram.authorization-token")
    private String authorizationToken;
    private String chatId = "417067134";

    @Test
    @Ignore
    public void testSendPhotoFull() throws IOException {

        TelegramService service = TelegramServiceProvider.get().getService();
        
        byte[] image = TelegramTestUtil.createSampleImage("jpg");
        OutgoingPhotoMessage message = new OutgoingPhotoMessage();
        message.setPhoto(image);
        message.setChatId(chatId);
        message.setFilenameWithExtension("file.png");
        message.setCaption("Photo");
        message.setDisableNotification(false);
        
        service.sendMessage(authorizationToken, message);
    }
    
    @Test
    public void testSendFull() {
        TelegramService service = TelegramServiceProvider.get().getService();

        OutgoingTextMessage message = new OutgoingTextMessage();
        message.setChatId(chatId);
        message.setText("This is an *auto-generated* message from the Bot");
        message.setDisableWebPagePreview(true);
        message.setParseMode(TelegramParseMode.MARKDOWN.getCode());
        message.setDisableNotification(false);

        service.sendMessage(authorizationToken, message);
    }
}
