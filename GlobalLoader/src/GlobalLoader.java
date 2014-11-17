import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Oleh on 08.11.2014.
 */
public class GlobalLoader implements Loadable {
    private final static int DEFAULT_BUFFER_SIZE = 4096;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    Loadable loadable;

    public int load( String filePath ) {

        String file = filePath.substring(filePath.lastIndexOf("\\"));
        String extension = file.substring(file.indexOf(".") + 1);

        // To-Do
//        try {
//            extension = Files.probeContentType( Paths.get( filePath ) );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        /* Choosing file decoder */
        //if ( WavLoader.getFileIdentifier().compareToIgnoreCase( extension ) == 0 ) {
            loadable = new WavLoader( filePath );
        //}

        return 0;
    }

    /**
     * sets current offset in audio file relative to the zero
     * byte in data section
     */
    @Override
    public void setCurrentOffset( int offset) { loadable.setCurrentOffset( offset ); }

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     * @return current offset in samples in file
     */
    @Override
    public int getCurrentOffset() { return loadable.getCurrentOffset(); }

    /**
     * @return number of bytes in data chunk
     */
    @Override
    public int getDataLength() { return loadable.getDataLength(); }

    /**
     * getter for valid
     * @return true, if file is valid
     */
    public boolean isValid() { return loadable.isValid(); }

    /**
     * reads certain number of bytes in wavFile
     * @param nBytes to read
     * @return array of bytes read from the file
     */
    @Override
    public byte [] readBytes( int nBytes ) { return loadable.readBytes( nBytes ); }

    /**
     * @return format encoding in byte array data of audio file
     */
    @Override
    public AudioFormat.Encoding getEncoding() { return loadable.getEncoding(); }

    /**
     * @return
     */
    @Override
    public float getSampleRate() { return loadable.getSampleRate(); }
    /**
     * @return
     */
    @Override
    public int getSampleSizeInBits() { return loadable.getSampleSizeInBits(); }

    /**
     * @return
     */
    @Override
    public int getChannels() { return loadable.getChannels(); }

    /**
     * @return
     */
    @Override
    public int getFrameSize() { return loadable.getFrameSize(); }

    /**
     * @return
     */
    @Override
    public float getFrameRate() { return loadable.getFrameRate(); }


    /**
     * sets buffer size for the file. If not set,
     * buffer size = DEFAULT_BUFFER_SIZE
     * @param bufferSize
     */
    public void setBufferSize( int bufferSize ){
        this.bufferSize = bufferSize;
    }
}
