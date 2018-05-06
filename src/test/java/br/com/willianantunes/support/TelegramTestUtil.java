package br.com.willianantunes.support;

import java.io.IOException;

import org.apache.camel.component.telegram.model.Chat;
import org.apache.camel.component.telegram.model.IncomingMessage;
import org.apache.cxf.helpers.IOUtils;

public final class TelegramTestUtil {

    private TelegramTestUtil() {
        
    }
    
    public static IncomingMessage createSampleIncommingMessageWithTextAndChatId(String text, String chatId) {
        
        IncomingMessage message = new IncomingMessage();
        message.setText(text);
        
        Chat chat = new Chat();
        chat.setId(chatId);
        
        message.setChat(chat);
        
        return message;
    }

    public static byte[] createSampleImage(String imageIOType) throws IOException {
        
        byte[] img = null;
        
        if (imageIOType.equalsIgnoreCase("png")) {
            img = IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.png"));
        } else if (imageIOType.equalsIgnoreCase("jpg")) {
            img = IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.jpg"));
        } else {
            throw new IllegalArgumentException("Unknown format " + imageIOType);
        }
        
        return img;
    }

    public static byte[] createSampleAudio() throws IOException {
        
        return IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.mp3"));
    }

    public static byte[] createSampleVideo() throws IOException {
        
        return IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.mp4"));
    }

    public static byte[] createSampleDocument() throws IOException {
        
        return IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.txt"));
    }
}