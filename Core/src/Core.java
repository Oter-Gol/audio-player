import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
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



    public int open( String filePath ) {

        globalLoader.load( filePath );

        if ( globalLoader.isValid() ) {
            audioFormat = new AudioFormat( globalLoader.getSampleRate(), globalLoader.getSampleSizeInBits(),
                    globalLoader.getChannels(), true, false );
        }




        byte [] samplesBateArray = new byte[ globalLoader.getDataLength() ];

        ByteArrayInputStream samplesStream = new ByteArrayInputStream( samplesBateArray );

        AudioInputStream audioInputStream = new AudioInputStream( samplesStream, audioFormat, globalLoader.getDataLength() );

        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 1);
        SourceDataLine soundLine;

        int bufferSize = 2200;

        try {
            soundLine = (SourceDataLine) AudioSystem.getLine(info);
            soundLine.open(audioFormat);

            soundLine.start();
            byte counter = 0;
            final byte[] buffer = new byte[bufferSize];


            soundLine.write( samplesBateArray, 0, globalLoader.getDataLength() );

        } catch (LineUnavailableException e) {
            e.printStackTrace();
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
