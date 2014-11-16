import javax.sound.sampled.AudioFormat;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by OTER on 11/7/2014.
 */
public class WavLoader implements Loadable {

    private RandomAccessFile wavFile; //audio file pointer

    private boolean valid; //if file is valid

    /*
     * constants
     */
    private final static String RIFF = new String( "RIFF" ); //constant RIFF, check if WAVE format
    private final static String WAVE = new String( "WAVE" ); //constant WAVE, check if WAVE format
    private final static String FMT  = new String( "fmt " ); //constant fmt , check if WAVE format
    private final static String DATA = new String( "data" ); //constant data
    private final static String FACT = new String( "fact" ); //constant fact, Non-PCM

    /*
     * common variables
     */
    private char [] ckIDFile = new char [ 4 ]; // Chunk ID: "RIFF" , 4 bytes
    private int cksizeFile; // Chunk size = size of file in bytes - sizeOf(RIFF and cksizeFile), 4 bytes
    private char [] WAVEID = new char [ 4 ]; // WAVE ID: "WAVE", 4 bytes

    /*
     * fmt variables
     */
    private char []  ckIDfmt = new char [ 4 ]; // Chunk ID: "fmt ", 4 bytes
    private int cksizefmt; //Chunk size: 16 or 18 or 40, 4 bytes
    private int wFormatTag; //Format code, need in enum 2 bytes
    private int nChannels; // Number of interleaved channels, 2 bytes
    private int nSamplePerSec; //Sampling rate (blocks per second), 4 bytes
    private int nAvjBytesPerSec; // Data rate, 4 bytes
    private int nBlockAlign; // Data block size (bytes), 2 bytes
    private int wBitsPerSample; // Bits per sample, 2 bytes

    /*
     * common data variables for PCM, Non-PCM, Extensible
     */
    private char [] ckIDData = new char [ 4 ]; //stores the word "data"
    private int cksizeData; // chunk size M*Nc*Ns

    /*
     * common data variables for Non-PCM, Extensible
     */
    private int cbSizeData; // size of the extension Non-PCM :0, Extensible : 22
    private char [] ckIDNonPCM = new char [ 4 ]; //Chunk ID: "fact"
    private int cksizeNonPCM; // Chunk size: 4
    private int dwSampleLength; // Nc*Ns

    /*
     * Extensible variables
     */
    private int wValidBitsPerSample; // Number of valid bits, at most 8 * M
    private int dwChannelMask; // speaker position mask : 0
    private char [] SubFormat = new char [ 16 ]; //GUID(first two bytes are the data format code)

    /*
     * data offset for all formats
     */
    private static final int PCM_Offset = 44;
    private static final int Non_PCM_Offset = 0; // To-Do
    private static final int EXTENSIBLE_Offset = 0; // To-Do

    private AudioFormat.Encoding encoding;


    /*
     * the standard format codes for waveform data
     * depends on wFormatTag
     */
    private enum Format_Code {
        WAVE_FORMAT_PCM( 0x0001 ), //PCM
        WAVE_FORMAT_IEEE_FLOAT( 0x0003 ), //IEEE float
        WAVE_FORMAT_ALAW( 0x0006 ), // 8-bit ITU-T G.711 A-law
        WAVE_FORMAT_MULAW( 0x0007 ), // 8-bit ITU-T G.711 micro-law
        WAVE_FORMAT_EXTENSIBLE( 0xFFFE ); // Determined by SubFormat

        private int value;

        /*
         * returns actual format from int value
         */
        public static Format_Code fromInt( int val ){
            switch ( val ) {
                case 0x0001 : return WAVE_FORMAT_PCM;
                case 0x0003 : return WAVE_FORMAT_IEEE_FLOAT;
                case 0x0006 : return WAVE_FORMAT_ALAW;
                case 0x0007 : return WAVE_FORMAT_MULAW;
                case 0xFFFE : return WAVE_FORMAT_EXTENSIBLE;
            }
            return null;
        }

        /*
         * constructor
         */
        Format_Code( int value ){
            this.value = value;
        }
    }

    private Format_Code wFormatTag_enum;
    private double wavDurationInSeconds; // the duration of current wav file


    /*
     * methods
     */

    /**
     * constructor for wave loader
     * @param filePath
     */
    public WavLoader( String filePath ) {
        valid = true;

        try{
            wavFile = new RandomAccessFile( filePath, "r");
        } catch( FileNotFoundException e ) {
            //TO-DO
        }

        if ( isValid() ) {
            readFormatChunk();
        }
    }

    /**
     * reads File Format structure
     * @return 0 if everything is ok, -1 if file isn't WAVE
     */
    public void readWaveFileFormat(){
        byte [] buffForFour; // is necessary for reading files

        /*
         * set pointer to the beginning of wavFile
         */
        try {
            wavFile.seek( 0 );
        } catch (IOException e ) {
            //TO-DO
        }

        /*
         * saves first 0..3 bytes for RIFF
         */
        buffForFour = readBytes( 4 );
        /*
         * gets chars from buff for ['R', 'I', 'F', 'F']
         */
        for ( int i = 0; i < 4; i++ ){
            ckIDFile[ i ] = ( char ) buffForFour[ i ];
        }

        /*
         * reads 4..7 bytes in wavFile and gets cksizeFile
         * the number of bytes in file except RIFF and sizeOf(cksizeFile)
         */
        cksizeFile = littleEndianByteArrayToInt( readBytes( 4 ) );

        /*
         * reads 8..11 bytes for word WAVE
         */
        buffForFour = readBytes( 4 );
        for ( int i = 0; i < 4; i++ ){
            WAVEID[ i ] = ( char ) buffForFour[ i ];
        }

        /*
         * checks if wavFile has WAVE format
         */
        if ( !( ( RIFF.equals( new String( ckIDFile ) ) ) &&
            ( WAVE.equals( new String( WAVEID ) ) ) ) ) {
            valid = false;
            try {
                wavFile.close();
            } catch (IOException e) {
                //TO-DO
            }
        }

    }

    /**
     * reads format chunk from current wavFile
     * @return 0 if everything ok, else -1
     */
    public int readFormatChunk() {
        byte [] buffForFour; // array of bytes size 4

        /*
         * set pointer to the 12th position
         */
        try {
            wavFile.seek( 12 );
        } catch (IOException e ) {
            //TO-DO
        }

        /*
         * saves first 12..15 elements for fmt
         */
        buffForFour = readBytes( 4 );
        /*
         * gets chars from buff for ['f', 'm', 't', '']
         */
        for ( int i = 0; i < 4; i++ ){
            ckIDfmt[ i ] = ( char ) buffForFour[ i ];
        }

        /*
         * reads 16..19 bytes in wavFile and gets cksizefmt
         */
        cksizefmt = littleEndianByteArrayToInt( readBytes( 4 ) );

        /*
         * reads 20, 21 bytes. Format code
         */
        wFormatTag = littleEndianByteArrayToInt( readBytes( 2 ) );

        /*
         * choose Format Code according to wFormatTag
         */
        wFormatTag_enum = Format_Code.fromInt( wFormatTag );

        /*
         * reads 22,23 bytes. Number of interleaved channels
         * Nc
         */
        nChannels = littleEndianByteArrayToInt( readBytes( 2 ) );

        /*
         * reads 24..27 bytes. Sampling rate (blocks per second)
         * F
         */
        nSamplePerSec = littleEndianByteArrayToInt( readBytes( 4 ) );

        /*
         * reads 28..31 bytes. Data rate
         * F * M * Nc
         */
        nAvjBytesPerSec = littleEndianByteArrayToInt( readBytes( 4 ) );

        /*
         * reads 32,33 bytes. Data block size (bytes)
         * M * Nc
         */
        nBlockAlign = littleEndianByteArrayToInt( readBytes( 2 ) );

        /*
         * reads 34,35 bytes. Bits per sample
         * rounds up to 8 * M
         */
        wBitsPerSample = littleEndianByteArrayToInt( readBytes( 2 ) );

        switch (wFormatTag_enum) {
            case WAVE_FORMAT_PCM:
                readPCMHeader();
                break;
            case WAVE_FORMAT_EXTENSIBLE:
                readExtensibleHeader();
                break;
            default:
                readNonPCMHeader();
        }

        //TO-DO checking
        return 0;
    }

    /**
     * reads header for PCM format
     * @return 0 if everything is ok
     */
    public int readPCMHeader(){
        /*
         * Chunk ID: "data", chunk size
         */
        common_ckID_cksize();

        return 0;
    }

    /**
     * reads header for Non-PCM format
     * @return //TO-DO
     */
    public int readNonPCMHeader() {
        /*
         * for size of the extension
         * size of extension = 0
         */
        cbSizeData = littleEndianByteArrayToInt( readBytes( 2 ) );

        /*
         * "fact", chunk size: 4, Nc*Ns
         */
        common_NonPCM_Extensible();

        /*
         * "data", chunk size
         */
        common_ckID_cksize();

        return 0;
    }

    /**
     * //TO-DO
     * @return 0 if ok
     */
    public int readExtensibleHeader() {
        //TO-DO
        return 0;
    }

    /*
    * current offset in file
    */
    private int currentFileOffset;

    /**
     * get common info about all 3 formats
     * the word "data" and the chunk size
     * its common for PCM, Non-PCM and Extensible
     */
    public void common_ckID_cksize(){
        byte [] buffForFour; // array of bytes size 4

        /*
         *  ckIDData must be "data"
         */
        buffForFour = readBytes( 4 );

        /*
         * gets chars from buff for ['d', 'a', 't', 'a']
         */
        for ( int i = 0; i < 4; i++ ){
            ckIDData[ i ] = ( char ) buffForFour[ i ];
        }

        /*
         * Chunk size: M * Nc * Ns
         */
        cksizeData = littleEndianByteArrayToInt( readBytes( 4 ) );
    }

    /**
     *  info for Non-PCM and Extensible formats
     *  ckID - chunk ID:"fact"
     *  ckSize chunk size :4
     */
    public void common_NonPCM_Extensible(){
        byte [] buffForFour; // array of bytes size 4

        /*
         * Chunk ID: "fact"
         */
        buffForFour = readBytes( 4 );
        for ( int i = 0; i < 4; i++ ){
            ckIDNonPCM[ i ] = ( char ) buffForFour[ i ];
        }

        /*
         * cksizeData Chunk size : 4
         */
        cksizeData = littleEndianByteArrayToInt( readBytes( 4 ) );

        /*
         * Nc * Ns dwSampleLength
         */
        dwSampleLength = littleEndianByteArrayToInt( readBytes( 4 ) );
    }

    /**
     * sets current offset in file relative to the zero
     * byte in data section
     */
    @Override
    public void setCurrentOffset( int offset) {

        switch ( wFormatTag_enum ) {
            case WAVE_FORMAT_PCM:
                currentFileOffset = PCM_Offset + offset;
                break;
            case WAVE_FORMAT_EXTENSIBLE:
                currentFileOffset = EXTENSIBLE_Offset + offset;
                break;
            default:
                currentFileOffset = Non_PCM_Offset + offset;
                break;
        }
        // To-Do!
        if ( cksizeData < currentFileOffset ) {
            currentFileOffset = 0;
        }
    }

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     * @return current offset in samples in file
     */
    @Override
    public int getCurrentOffset() {
        int retValue = 0;

        switch ( wFormatTag_enum ) {
            case WAVE_FORMAT_PCM:
                retValue = currentFileOffset - PCM_Offset;
                break;
            case WAVE_FORMAT_EXTENSIBLE:
                retValue = currentFileOffset - EXTENSIBLE_Offset;
                break;
            default:
                retValue = currentFileOffset - Non_PCM_Offset;
                break;
        }
        return retValue;
    }

    /**
     *
     * @return number of bytes in data chunk
     */
    @Override
    public int getDataLength() {
        return cksizeData;
    }

    /**
     * getter for valid
     * @return true, if file is valid
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * reads certain number of bytes in wavFile
     * @param nBytes to read
     * @return array of bytes read from the file
     */
    @Override
    public byte [] readBytes( int nBytes ){

        int bytesToRead = cksizeData - getCurrentOffset() < nBytes ? cksizeData % nBytes : nBytes;

        byte [] buff = new byte[ bytesToRead ];

        try {
            wavFile.readFully( buff, 0, bytesToRead );
        } catch ( IOException e ){
            //TO-DO
        }



        return buff;
    }

    /**
     * @return format encoding in byte array data of audio file
     */
    @Override
    public AudioFormat.Encoding getEncoding() { // To-DO
        return AudioFormat.Encoding.PCM_SIGNED;
    }

    /**
     * @return
     */
    @Override
    public float getSampleRate() {
        return (float)nSamplePerSec;
    }

    /**
     * @return
     */
    @Override
    public int getSampleSizeInBits() {
        return wBitsPerSample;
    }

    /**
     * @return
     */
    @Override
    public int getChannels() {
        return nChannels;
    }

    /**
     * @return
     */
    @Override
    public int getFrameSize() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public float getFrameRate() {
        return 0;
    }

    /**
     * @return file specification string
     */
    public static String getFileIdentifier() {
        return WAVE;
    }

    /**
     * makes int value from array of bytes
     * the value of int depends on the length of array.
     * @param b
     * @return int value
     */
    public static int littleEndianByteArrayToInt(byte[] b)
    {
        int value = 0;

        for ( int i = 0; i < b.length; i++ ){
            value |= ( b[ i ] & 0xFF ) << ( i * 8 );
        }
        return  value;
    }
}