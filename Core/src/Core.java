import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Timer;

/**
 * Created by Oleh on 10.11.2014.
 */
public class Core {

    private GlobalLoader globalLoader = new GlobalLoader();

    private Timer timer;

    private AudioFormat audioFormat;


    Clip clip;

    public int open( String filePath ) {

        globalLoader.load( filePath );

        if ( globalLoader.isValid() ) {
            audioFormat = new AudioFormat( globalLoader.getSampleRate(), globalLoader.getSampleSizeInBits(),
                    globalLoader.getChannels(), true, false );
        }




        byte [] samplesBateArray = new byte[ globalLoader.getDataLength() ];

        ByteArrayInputStream samplesStream = new ByteArrayInputStream( samplesBateArray );

        AudioInputStream audioInputStream = new AudioInputStream( samplesStream, audioFormat, globalLoader.getDataLength() );

        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

//        if (auline.isControlSupported(FloatControl.Type.PAN)) {
//            FloatControl pan = (FloatControl) auline
//                    .getControl(FloatControl.Type.PAN);
//            if (curPosition == Position.RIGHT)
//                pan.setValue(1.0f);
//            else if (curPosition == Position.LEFT)
//                pan.setValue(-1.0f);
//        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[20000];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    auline.write(abData, 0, nBytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } finally {
            auline.drain();
            auline.close();
        }





//        byte sign = 1;
//        while (frame.isVisible()) {
//            int threshold = audioFormat.getFrameRate() / sliderValue;
//            for (int i = 0; i < bufferSize; i++) {
//                if (counter > threshold) {
//                    sign = (byte) -sign;
//                    counter = 0;
//                }
//                buffer[i] = (byte) (sign * 30);
//                counter++;
//            }
//            // the next call is blocking until the entire buffer is
//            // sent to the SourceDataLine
//
//        }









        return 0;
    }

    public int play() {
        return 0;
    }

    public int pause() {
        return 0;
    }

    public int seek() { return 0; }

    public int stop() {
        return 0;
    }

}
