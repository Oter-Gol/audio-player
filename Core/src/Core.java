import javax.sound.sampled.AudioFormat;
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
