/**
 * Created by Oleh on 08.11.2014.
 */
public class GlobalLoader {
    private final static int DEFAULT_BUFFER_SIZE = 4096;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * sets buffer size for the file. If not setted,
     * buffer size = DEFAULT_BUFFER_SIZE
     * @param bufferSize
     */
    public void setBufferSize( int bufferSize ){
        this.bufferSize = bufferSize;
    }
}
