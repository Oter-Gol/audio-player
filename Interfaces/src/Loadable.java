import javax.sound.sampled.AudioFormat;

/**
 * Created by OTER on 11/16/2014.
 */
public interface Loadable {

    /**
     * sets current offset in file relative to the zero
     * byte in data section
     */
    public void setCurrentPosition(int position);

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     * @return current offset in samples in file
     */
    public int getCurrentPosition();

    /**
     * @return number of bytes in data chunk
     */
    public int getDataLength();

    /**
     * getter for valid
     * @return true, if file is valid
     */
    public boolean isValid();

    /**
     * reads certain number of bytes in wavFile
     * @param nBytes to read
     * @param samplesBuff filled with sampled bytes
     * @return array of bytes read from the file
     */
    public int readSampledBytes(int nBytes, byte[] samplesBuff);

    /**
     *
     * @return format encoding in byte array data of audio file
     */
    public AudioFormat.Encoding getEncoding();

    /**
     *
     * @return
     */
    public float getSampleRate();

    /**
     *
     * @return
     */
    public int getSampleSizeInBits();

    /**
     *
     * @return
     */
    public int getChannels();

    /**
     *
     * @return
     */
    public int getFrameSize();

    /**
     * @return
     */
    public float getFrameRate();

}
