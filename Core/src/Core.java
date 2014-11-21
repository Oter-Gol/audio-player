import javax.sound.sampled.*;
import javax.swing.text.PlainDocument;
import java.io.IOException;
import java.util.Timer;

/**
 * Created by Oleh on 10.11.2014.
 */
public class Core {

    /*
     * Default buffer size for play back audio
     */
    private final static int DEFAULT_BUFFER_SIZE = 4096;

    /*
     * Buffer size
     */
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private GlobalLoader globalLoader = new GlobalLoader();

    private Timer timer;

    private AudioFormat audioFormat;

    public static enum PlayingStates { PLAY, PAUSE, STOP };

    private PlayingStates playingState = PlayingStates.STOP;

    public PlayingStates getPlayingState() {
        return playingState;
    }

    private class PlayingThread implements Runnable {

        int timeSeek;

        public int playPause() {

            // To-Do perform check for valid format

            // States for state machine
            if ( playingState == PlayingStates.STOP ) {
                playingState = PlayingStates.PLAY;

                run();

            } else {
                if ( playingState == PlayingStates.PLAY ) {
                    playingState = PlayingStates.PAUSE;

                } else {
                    playingState = PlayingStates.PLAY;
                }
            }

            return 0;
        }

        public int seek( int timeSeek ) { return 0; }

        public int stop() {
            return 0;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {

            SourceDataLine auline = null;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            try {
                auline = (SourceDataLine) AudioSystem.getLine(info);
                auline.open(audioFormat);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            auline.start();

            int nBytesRead = 0;

            byte[] samplesRead = new byte[ bufferSize ];

            nBytesRead = globalLoader.readSampledBytes( bufferSize, samplesRead );

            globalLoader.setCurrentOffset( 0 );

            while (nBytesRead != 0) {
                auline.write(samplesRead, 0, nBytesRead );
                nBytesRead = globalLoader.readSampledBytes( bufferSize, samplesRead );
            }

            auline.drain();
            auline.close();

        }
    }

   public PlayingThread playingThread;


    public int open( String filePath ) {

        globalLoader.load( filePath );

        if ( globalLoader.isValid() ) {
            audioFormat = new AudioFormat(globalLoader.getSampleRate(), globalLoader.getSampleSizeInBits(),
                    globalLoader.getChannels(), true, false);
        }

        playingThread = new PlayingThread();

        return 0;
    }




    /**
     * sets buffer size for the file. If not set,
     * buffer size = DEFAULT_BUFFER_SIZE
     * @param bufferSize
     */
    public void setBufferSize( int bufferSize ){
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }


    public int playPause() {

        return playingThread.playPause();
    }

    public int seek( int timeSeek ) { return 0; }

    public int stop() {
        return 0;
    }

}
