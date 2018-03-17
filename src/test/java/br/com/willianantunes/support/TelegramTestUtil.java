package br.com.willianantunes.support;

import java.io.IOException;

import org.apache.cxf.helpers.IOUtils;

public final class TelegramTestUtil {

    private TelegramTestUtil() {
        
    }

    public static byte[] createSampleImage(String imageIOType) throws IOException {
        
        byte[] img;
        
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
        
        byte[] audio = IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.mp3"));
        return audio;
    }

    public static byte[] createSampleVideo() throws IOException {
        
        byte[] video = IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.mp4"));
        return video;
    }

    public static byte[] createSampleDocument() throws IOException {
        
        byte[] document = IOUtils.readBytesFromStream(TelegramTestUtil.class.getResourceAsStream("/attachments/sample.txt"));
        return document;
    }
}