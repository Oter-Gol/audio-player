import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by OTER on 11/7/2014.
 */
public class WavLoader {

    private RandomAccessFile wavFile; //audio file pointer

    private final static String RIFF = new String( "RIFF" ); //constant RIFF, check if WAVE format
    private final static String WAVE = new String( "WAVE" ); //constant WAVE, check if WAVE format
    private final static String fmt  = new String( "fmt "); //constant fmt , check if WAVE format

    private char [] ckIDFile = new char [ 4 ]; // Chunk ID: "RIFF" , 4 bytes
    private int cksizeFile; // Chunk size = size of file in bytes - sizeOf(RIFF and cksizeFile), 4 bytes
    private char [] WAVEID = new char [ 4 ]; // WAVE ID: "WAVE", 4 bytes

    private char []  ckIDfmt = new char [ 4 ]; // Chunk ID: "fmt ", 4 bytes
    private int cksizefmt; //Chunk size: 16 or 18 or 40, 4 bytes
    private int wFormatTag; //Format code, need in enum 2 bytes
    private int nChannels; // Number of interleaved channels, 2 bytes
    private int nSamplePerSec; //Sampling rate (blocks per second), 4 bytes
    private int nAvjBytesPerSec; // Data rate, 4 bytes
    private int nBlockAlign; // Data block size (bytes), 2 bytes
    private int wBitsPerSample; // Bits per sample, 2 bytes
    private int cbSize; // Size of the extension (0 or 22), 2 bytes
    private int wValidBitsPerSample; // Number of valid bits, 2 bytes
    private int dwChannelMask; // speaker position mask, 4 bytes
    private char [] SubFormat = new char [ 16 ]; //GUID, including the data format code, 16 bytes

    // the standard format codes for waveform data
    //depends on wFormatTag
    private enum Format_Code {
        WAVE_FORMAT_PCM( 0x0001 ), //PCM
        WAVE_FORMAT_IEEE_FLOAT( 0x0003 ), //IEEE float
        WAVE_FORMAT_ALAW( 0x0006 ), // 8-bit ITU-T G.711 A-law
        WAVE_FORMAT_MULAW( 0x0007 ), // 8-bit ITU-T G.711 micro-law
        WAVE_FORMAT_EXTENSIBLE( 0xFFFE ); // Determined by SubFormat

        private int value;

        /* returns actual format from int value */
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

        /* constructor */
        Format_Code( int value ){
            this.value = value;
        }
    }

    private Format_Code wFormatTag_enum;
    private double wavDurationInSeconds; // the duration of current wav file

    /**
     * constructor for wave loader
     * @param filePath
     */
    public WavLoader( String filePath ) {
        try{
            wavFile = new RandomAccessFile( filePath, "r");
        } catch( FileNotFoundException e ) {
            //TO-DO
        }

        if ( readWaveFileFormat() == 0 ) {
            readFormatChunk();
        }
    }

    /**
     * reads File Format structure
     * @return 0 if everything is ok, -1 if file isn't WAVE
     */
    public int readWaveFileFormat(){
        byte [] buffForFour = new byte [ 4 ]; // is necessary for reading files

        /* set pointer to the beginning of wavFile */
        try {
            wavFile.seek( 0 );
        } catch (IOException e ) {
            //TO-DO
        }

        /* saves first 0..3 elements for RIFF */
        buffForFour = readBytes( 4 );
        /* gets chars from buff for ['R', 'I', 'F', 'F'] */
        for ( int i = 0; i < 4; i++ ){
            ckIDFile[ i ] = ( char ) buffForFour[ i ];
        }

        /* reads 4..7 bytes in wavFile and gets cksizeFile
           the number of bytes in file except RIFF and sizeOf(cksizeFile) */
        cksizeFile = littleEndianByteArrayToInt( readBytes( 4 ) );

        /* reads 8..11 bytes for word WAVE */
        buffForFour = readBytes( 4 );
        for ( int i = 0; i < 4; i++ ){
            WAVEID[ i ] = ( char ) buffForFour[ i ];
        }

        /* checks if wavFile has WAVE format */
        if ( ( RIFF.equals( new String( ckIDFile ) ) ) &&
            ( WAVE.equals( new String( WAVEID ) ) ) ){
            return 0;
        } else {
            try {
                wavFile.close();
            } catch ( IOException e ){
                //TO-DO
            }
            return -1;
        }
    }

    /**
     * reads format chunk from current wavFile
     * @return 0 if everything ok, else -1
     */
    public int readFormatChunk() {
        byte [] buffForFour;

        /* set pointer to the 12th position */
        try {
            wavFile.seek( 12 );
        } catch (IOException e ) {
            //TO-DO
        }

        /* saves first 12..15 elements for fmt */
        buffForFour = readBytes( 4 );
        /* gets chars from buff for ['f', 'm', 't', ''] */
        for ( int i = 0; i < 4; i++ ){
            ckIDfmt[ i ] = ( char ) buffForFour[ i ];
        }

        /* reads 16..19 bytes in wavFile and gets cksizefmt */
        cksizefmt = littleEndianByteArrayToInt( readBytes( 4 ) );

        /*reads 20, 21 bytes. Format code */
        wFormatTag = littleEndianByteArrayToInt( readBytes( 2 ) );

        /* reads 22,23 bytes. Number of interleaved channels */
        nChannels = littleEndianByteArrayToInt( readBytes( 2 ) );

        /* reads 24..27 bytes. Sampling rate (blocks per second) */
        nSamplePerSec = littleEndianByteArrayToInt( readBytes( 4 ) );

        /* reads 28..31 bytes. Data rate */
        nAvjBytesPerSec = littleEndianByteArrayToInt( readBytes( 4 ) );

        /* reads 32,33 bytes. Data block size (bytes) */
        nBlockAlign = littleEndianByteArrayToInt( readBytes( 2 ) );

        /* reads 34,35 bytes. Bits per sample */
        wBitsPerSample = littleEndianByteArrayToInt( readBytes( 2 ) );

        /* depends in wFormatTag choose Format Code */
        wFormatTag_enum = Format_Code.fromInt( wFormatTag );

        //TO-DO checking
        return 0;
    }

    /**
     * reads certain number of bytes in wavFile
     * @param nBytes to read
     * @return array of bytes read from the file
     */
    public byte [] readBytes( int nBytes ){
        byte [] buff = new byte[ nBytes ];
        try{
            wavFile.readFully( buff, 0, nBytes );
        } catch ( IOException e ){
            //TO-DO
        }
        return buff;
    }

    /**
     * makes int value of array of 4 bytes
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

