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

    int bufferOffset = 0; // to read data from file. pointer from which position to start
    /*
     * constants
     */
    private final static String WAVE = "WAVE"; //constant WAVE, check if WAVE format
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
    private int dataOffset;
    private int fmtOffset;

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
            Logger.writeInFile(e.toString());
        }

        if ( readWaveFileFormat() == -1 ){
            valid = false;
        }

        if ( isValid() ) {
            if ( readFormatChunk() == -1 ){
                valid = false;
            }
        }

    }

    /**
     * method to determine position which points after chunkID data part
     * makes the process of finding junk sections easier
     * @param chunkID which string to find
     * @param startOffset from which position to start the search
     * @param endOffset on which byte end the search
     * @return position after the certain word
     */
    public int getOffsetByChunk( String chunkID, int startOffset, int endOffset ) {

        for (int i = startOffset; i < endOffset; i++) {
            try {
                wavFile.seek( i );
            } catch (IOException e) {
                Logger.writeInFile(e.toString());
            }

            char [ ] charBuff = new char [ chunkID.length() ];
            byte [] byteBuff = new byte[ chunkID.length() ];


            try {
                wavFile.readFully( byteBuff, 0, byteBuff.length );
            } catch (IOException e) {
                Logger.writeInFile(e.toString());
            }

            for ( int j = 0; j < chunkID.length(); j++ ){
                charBuff[ j ] = ( char ) byteBuff[ j ];
            }

            if ( chunkID.equals( new String( charBuff ) ) ) {
                return i + chunkID.length();
            }
        }
        return -1;
    }

    /**
     * reads File Format structure
     * valid = false if there's no word WAVE or RIFF
     */
    public int readWaveFileFormat(){
        /*
         * set pointer to the beginning of wavFile
         */
        try {
            wavFile.seek( 0 );
        } catch (IOException e ) {
            Logger.writeInFile(e.toString());
        }

        /*
         * saves first 0..3 bytes for RIFF
         * gets chars from buff for ['R', 'I', 'F', 'F']
         */
        bufferOffset = getOffsetByChunk("RIFF", bufferOffset, bufferOffset + 4);
        if ( bufferOffset == -1 ){
            Logger.writeInFile("Impossible to read the file. RIFF word not found. Is not WAVE format");
            return -1;
        }

        /*
         * reads 4..7 bytes in wavFile and gets cksizeFile
         * the number of bytes in file except RIFF and sizeOf(cksizeFile)
         */
        cksizeFile = littleEndianByteArrayToInt( readBytes(4) );
        bufferOffset += 4;

        /*
         * reads 8..11 bytes for word WAVE
         */
        bufferOffset = getOffsetByChunk("WAVE", bufferOffset, bufferOffset + 4);
        if ( bufferOffset == -1 ){
            Logger.writeInFile("impossible to read the file. WAVE word not found. Is not WAVE format");
            return -1;
        }

        /*
         * gets chars from buff for ['f', 'm', 't', ' ']
         */
        bufferOffset = getOffsetByChunk( "fmt ", bufferOffset, bufferOffset + 150 );
        if ( bufferOffset == -1 ){
            Logger.writeInFile("impossible to read the file. fmt  word not found. Is not WAVE format");
            return -1;
        }
        return 0;
    }

    /**
     * reads format chunk from current wavFile
     * @return 0 if everything ok, else -1
     */
    public int readFormatChunk() {
        /*
         * gets cksizefmt
         */
        cksizefmt = littleEndianByteArrayToInt( readBytes(4) );

        /*
         * Format code
         */
        wFormatTag = littleEndianByteArrayToInt( readBytes(2) );

        /*
         * choose Format Code according to wFormatTag
         */
        wFormatTag_enum = Format_Code.fromInt( wFormatTag );

        /*
         * Number of interleaved channels
         * Nc
         */
        nChannels = littleEndianByteArrayToInt( readBytes(2) );

        /*
         * Sampling rate (blocks per second)
         * F
         */
        nSamplePerSec = littleEndianByteArrayToInt( readBytes(4) );

        /*
         * Data rate
         * F * M * Nc
         */
        nAvjBytesPerSec = littleEndianByteArrayToInt( readBytes(4) );

        /*
         * Data block size (bytes)
         * M * Nc
         */
        nBlockAlign = littleEndianByteArrayToInt( readBytes(2) );

        /*
         * Bits per sample
         * rounds up to 8 * M
         */
        wBitsPerSample = littleEndianByteArrayToInt( readBytes(2) );

        bufferOffset += 22; // all necessary info about file contains in 22 bytes

        int checkValue;
        /*
         * read header according to the format
         */
        switch (wFormatTag_enum) {
            case WAVE_FORMAT_PCM:
                checkValue = readPCMHeader();
                break;
            case WAVE_FORMAT_EXTENSIBLE:
                checkValue = readExtensibleHeader();
                break;
            default:
                checkValue = readNonPCMHeader();
        }

        if ( checkValue == -1 ){
            Logger.writeInFile("the file is not valid. No data section");
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * reads header for PCM format
     * @return 0 if everything is ok
     */
    public int readPCMHeader(){
        /*
         * check if file is valid
         * if there is word "data"
         */
        return common_ckID_cksize() == -1 ? -1 : 0;
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
        cbSizeData = littleEndianByteArrayToInt(readBytes(2));

        /*
         * "fact", chunk size: 4, Nc*Ns
         */
        common_NonPCM_Extensible();

        /*
         * "data", chunk size
         */
        common_ckID_cksize();

        /*
         * check if file is valid
         * if there are "fact" and "data"
         */
        return common_NonPCM_Extensible() == -1 || common_ckID_cksize() == -1 ? -1 : 0;
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
    public int common_ckID_cksize(){
        /*
         * gets chars from buff for ['d', 'a', 't', 'a']
         */
        //To-Do Something wrong in with offset bufferOffset argument
        bufferOffset = getOffsetByChunk( "data", 1 , 200 );
        dataOffset = bufferOffset;

        /*
         * Chunk size: M * Nc * Ns
         */
        cksizeData = littleEndianByteArrayToInt( readBytes(4) );

        return bufferOffset;
    }

    /**
     *  info for Non-PCM and Extensible formats
     *  ckID - chunk ID:"fact"
     *  ckSize chunk size :4
     */
    public int common_NonPCM_Extensible(){
        /*
         * Chunk ID: "fact" if the word is in the file
         * gets the position after "fact"
         * otherwise gets -1
         */
        bufferOffset = getOffsetByChunk("fact", bufferOffset, 200);

        /*
         * cksizeData Chunk size : 4
         */
        cksizeData = littleEndianByteArrayToInt( readBytes(4) );

        /*
         * Nc * Ns dwSampleLength
         */
        dwSampleLength = littleEndianByteArrayToInt( readBytes(4) );

        return bufferOffset == -1 ? -1 : 0;
    }

    /**
     * sets current position of audio play back
     * byte in data section
     */
    @Override
    public void setCurrentPosition(int offset) {

        currentFileOffset = offset + dataOffset;

        try {
            wavFile.seek(currentFileOffset);
        } catch (IOException e) {
            Logger.writeInFile(e.toString());
        }
    }

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     * @return current offset in samples in file
     */
    @Override
    public int getCurrentPosition() {
        return currentFileOffset - dataOffset;
    }

    /**
     * get data length
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
     * reads certain number of sampled bytes in wavFile
     * @param nBytes to read
     * @param samplesBuff
     * @return array of bytes read from the file
     */
    @Override
    public int readSampledBytes(int nBytes, byte[] samplesBuff){

        int bytesToRead = 0;

        if ( cksizeData - getCurrentPosition() != 0 ) {
            int delta = cksizeData - getCurrentPosition();

            bytesToRead = delta - nBytes < 0 ? delta : nBytes;
            setCurrentPosition(getCurrentPosition() + bytesToRead);

            try {
                wavFile.readFully( samplesBuff, 0, bytesToRead );
            } catch ( IOException e ){
                Logger.writeInFile(e.toString());
            }
        }

        return bytesToRead;
    }

    /**
     * read certain amount of bytes
     * @param nBytes number of bytes to read from file
     * @return array of bytes
     */
    public byte [] readBytes(int nBytes){

        byte [] buff = new byte[ nBytes ];

        try {
            wavFile.readFully( buff, 0, nBytes );
        } catch ( IOException e ){
            Logger.writeInFile(e.toString());
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
     * @return sample rate
     */
    @Override
    public float getSampleRate() {
        return (float)nSamplePerSec;
    }

    /**
     * @return sample size in bits
     */
    @Override
    public int getSampleSizeInBits() {
        return wBitsPerSample;
    }

    /**
     * @return number of channels
     */
    @Override
    public int getChannels() {
        return nChannels;
    }

    /**
     * @return frame size
     */
    @Override
    public int getFrameSize() {
        return 0;
    }

    /**
     * @return frame rate
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
     * @param b which array to convert
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