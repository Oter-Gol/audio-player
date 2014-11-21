import javax.sound.sampled.*;
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

    private AudioFormat audioFormat;

    public static enum PlayingStates { PLAY, PAUSE, STOP };

    private PlayingStates playingState = PlayingStates.STOP;

    public PlayingStates getPlayingState() {
        return playingState;
    }

    private class PlayingThread implements Runnable {

        SourceDataLine audioLine = null;
        Thread thread;
        boolean pause = false;

        public void start ()
        {
            System.out.println( "Starting thread" );
            if ( thread == null )
            {
                thread = new Thread( this );
                thread.start ();
            }
        }



        public void run() {


            try {
                DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );

                try {
                    audioLine = (SourceDataLine) AudioSystem.getLine(info);
                    audioLine.open( audioFormat );
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                audioLine.start();

                int nBytesRead;
                byte[] samplesRead = new byte[ bufferSize ];

                nBytesRead = globalLoader.readSampledBytes( bufferSize, samplesRead );

                while ( nBytesRead != 0 ) {
                    synchronized( this ) {
                        while ( pause ) {
                            wait();
                        }
                    }

                    audioLine.write(samplesRead, 0, nBytesRead );
                    nBytesRead = globalLoader.readSampledBytes( bufferSize, samplesRead );
                }
            } catch (InterruptedException e) {
                    e.printStackTrace();
            } finally {
                    audioLine.drain();
                    audioLine.close();
            }


        }

        void pause() {
            pause = true;
            audioLine.flush();
            audioLine.start();
        }
        synchronized void resume() {
            pause = false;
            notify();
        }

    }

   private PlayingThread playingThread;


    public int open( String filePath ) {

        globalLoader.load( filePath );

        if ( globalLoader.isValid() ) {
            audioFormat = new AudioFormat(globalLoader.getSampleRate(), globalLoader.getSampleSizeInBits(),
                    globalLoader.getChannels(), true, false);
        }

        globalLoader.setCurrentPosition( 0 );
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

        if ( playingState == PlayingStates.STOP ) {
            playingState = PlayingStates.PLAY;

            playingThread.start();
            //seekPlaying( 0 );


        } else {
            if ( playingState == PlayingStates.PLAY ) {
                playingState = PlayingStates.PAUSE;

                playingThread.pause();

            } else {
                playingState = PlayingStates.PLAY;
                playingThread.resume();
            }
        }


        return 0;
    }

    public int seekPlaying( int seekValue ) {
        playingThread.pause();
        globalLoader.setCurrentPosition( seekValue );

        if ( playingState == PlayingStates.PLAY ) {
            playingThread.resume();
        }

        return 0;
    }

    public int stop() {
        playingState = PlayingStates.STOP;
        seekPlaying( 0 );

        return 0;
    }

}
