import javax.sound.sampled.*;

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

    /*
     * new object of class GlobalLoader
     */
    private GlobalLoader globalLoader = new GlobalLoader();

    private AudioFormat audioFormat;

    public static enum PlayingStates { PLAY, PAUSE, STOP };

    private PlayingStates playingState = PlayingStates.STOP;

    public PlayingStates getPlayingState() {
        return playingState;
    }

    private SliderUpdater callbackSlider;

    /**
     * inner class for playing in thread
     * implements interface Runnable
     */
    private class PlayingThread implements Runnable {

        SourceDataLine audioLine = null;
        Thread thread;
        boolean pause = false;

        /**
         * starting thread
         * if the thread doesn't exist, make it
         */
        public void start ()
        {
            System.out.println( "Starting thread" );
            if ( thread == null )
            {
                thread = new Thread( this );
                thread.start ();
            }
        }

        /**
         * TODO comment
         */
        @Override
        public void run() {
            try {
                DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );

                /*
                 *
                 */
                try {
                    audioLine = (SourceDataLine) AudioSystem.getLine(info);
                    audioLine.open( audioFormat );
                } catch (LineUnavailableException e) {
                    Logger.writeInFile( e.toString() );
                } catch (Exception e) {
                    Logger.writeInFile(e.toString());
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

                    updateSlider();

                    audioLine.write(samplesRead, 0, nBytesRead );
                    nBytesRead = globalLoader.readSampledBytes( bufferSize, samplesRead );
                }
            } catch (InterruptedException e) {
                 Logger.writeInFile(e.toString());
            } finally {
                audioLine.drain();
                audioLine.close();
            }
        }

        /**
         * pause the playing
         */
        void pause() {
            pause = true;
            audioLine.flush();
            audioLine.start();
        }

        /**
         * resume the playing
         * synchronize threads
         */
        synchronized void resume() {
            pause = false;
            notify();
        }

    }

    public void setCallBackSlider( SliderUpdater callback ){
        callbackSlider = callback;
    }

    public void updateSlider() {
        try {
            if ( callbackSlider != null ){
                callbackSlider.updateSlider( (float)globalLoader.getCurrentPosition() / globalLoader.getDataLength() );
            }
        } catch (Exception e) {
            Logger.writeInFile(e.toString());
        }
    }

    /**
     * object of inner class PlayingThread
     */
    private PlayingThread playingThread;

    /**
     * opens the file
     * @param filePath to the certain file
     * @return 0 if file is open
     */
    public int open( String filePath ) {

        globalLoader.load( filePath );

        if ( globalLoader.isValid() ) {
            audioFormat = new AudioFormat(globalLoader.getSampleRate(), globalLoader.getSampleSizeInBits(),
                    globalLoader.getChannels(), true, false);
        }

        System.out.println( audioFormat.toString() );

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

    /**
     * @return the size of buffer
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     *
     * @return
     */
    public int playPause() {

        if ( playingState == PlayingStates.STOP ) {
            playingState = PlayingStates.PLAY;

            playingThread.start();
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

    /**
     *
     * @param seekValue
     * @return
     */
    public int seekPlaying( float seekValue ) {
        playingThread.pause();
        System.out.println(seekValue);
        int iSeek = (int)(seekValue * globalLoader.getDataLength());
        iSeek = iSeek - ( iSeek % globalLoader.getFrameSize() );
        System.out.println( globalLoader.getDataLength() );
        System.out.println( iSeek % globalLoader.getDataLength() );
        globalLoader.setCurrentPosition( iSeek );

        if ( playingState == PlayingStates.PLAY ) {
            playingThread.resume();
        }

        return 0;
    }

    /**
     *
     * @return
     */
    public int stop() {
        playingState = PlayingStates.STOP;
        seekPlaying( 0.0f );

        return 0;
    }
}
