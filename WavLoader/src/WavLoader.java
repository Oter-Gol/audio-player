import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by OTER on 11/7/2014.
 */
public class WavLoader {

    private RandomAccessFile wavFile;


    private final static String RIFF = new String( "RIFF" ); //constant RIFF, check for WAVE format
    private final static String WAVE = new String( " WAVE" ); //constant WAVE, check for WAVE format

    private char [] ckIDFile = new char [ 4 ]; // Chunk ID: "RIFF" , 4 bytes
    private int cksizeFile; // Chunk size = size of file in bytes - sizeOf(RIFF and cksizeFile), 4 bytes
    private char [] WAVEID = new char [ 4 ]; // WAVE ID: "WAVE", 4 bytes

    private char []  ckIDfmt = new char [ 4 ]; // Chunk ID: "fmt", 4 bytes
    private int cksizefmt; //Chunk size: 16 or 18 or 40, 4 bytes
    private char [] wFormatTag = new char [ 2 ]; //Format code, 2 bytes
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
    private enum Format_Code {
        WAVE_FORMAT_PCM( 0x0001 ), //PCM
        WAVE_FORMAT_IEEE_FLOAT( 0x0003 ), //IEEE float
        WAVE_FORMAT_ALAW( 0x0006 ), // 8-bit ITU-T G.711 A-law
        WAVE_FORMAT_MULAW( 0x0007 ), // 8-bit ITU-T G.711 micro-law
        WAVE_FORMAT_EXTENSIBLE( 0xFFFE ); // Determined by SubFormat

        private int value;

        /*constructor */
        Format_Code( int value ) {
            this.value = value;
        }

        /*getter for value */
        public int getValue(){
            return value;
        }
    }

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
        readWaveFileFormat();
    }

    /**
     * reads File Format structure
     * @return 0 if everything is ok, -1 if file isn't WAVE
     */
    public int readWaveFileFormat(){
        byte [] buff = new byte [ 4 ]; // is necessary for reading files
        /* set pointer to the beginning of wavFile */
        try {
            wavFile.seek( 0 );
        } catch (IOException e ) {
            //TO-DO
        }

        /* saves first 4 elements for RIFF */
        try {
            wavFile.readFully(buff, 0, 4);
        } catch ( IOException e ) {
            //TO-DO
        }
        /* gets chars from buff for ['R', 'I', 'F', 'F'] */
        for ( int i = 0; i < 4; i++ ){
            ckIDFile[ i ] = ( char ) buff[ i ];
        }

        /* reads 5..8 bytes in wavFile and gets cksizeFile */
        try {
            wavFile.readFully( buff, 0, 4 );
        } catch ( IOException e ){
            //TO-DO
        }
        /* the number of bytes in file except RIFF and sizeOf(cksizeFile) */
        cksizeFile = littleEndianByteArrayToInt(buff);

        /* reads 9..12 bytes for word WAVE */
        try {
            wavFile.readFully(buff, 0, 4);
        } catch ( IOException e ) {
            //TO-DO
        }
        for ( int i = 0; i < 4; i++ ){
            WAVEID[ i ] = ( char ) buff[ i ];
        }

        try {
            wavFile.close();
        } catch ( IOException e ){
            //TO-DO
        }

        /* checks if wavFile has WAVE format #1*/
        if ( ( RIFF.equals( new String( ckIDFile ) ) ) &&
            ( WAVE.equals( new String( WAVEID ) ) ) ){
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * makes int value of array of 4 bytes
     * @param b
     * @return int value
     */
    public static int littleEndianByteArrayToInt(byte[] b)
    {
        return   b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }
}

