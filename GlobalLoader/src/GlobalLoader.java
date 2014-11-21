import javax.sound.sampled.AudioFormat;

/**
 * Created by Oleh on 08.11.2014.
 */
public class GlobalLoader implements Loadable {

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
    public void setCurrentPosition(int position) { loadable.setCurrentPosition(position); }

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     * @return current offset in samples in file
     */
    @Override
    public int getCurrentPosition() { return loadable.getCurrentPosition(); }

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
     * @param samplesBuff
     * @return array of bytes read from the file
     */
    @Override
    public int readSampledBytes(int nBytes, byte[] samplesBuff) { return loadable.readSampledBytes(nBytes, samplesBuff); }

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

}
