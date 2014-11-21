import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * Created by OTER on 11/21/2014.
 */


public class Mp3Loader implements Loadable {

    private int frameCount;

    public Mp3Loader( String filePath ){

        InputStream in = null;
        try {
            in = new FileInputStream( filePath );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int frameCount = Integer.MAX_VALUE;

        // int testing;
        // frameCount = 100;

        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(in);
        SourceDataLine line = null;
        int error = 0;
        for (int frame = 0; frame < frameCount; frame++) {
//            if (pause) {
//                line.stop();
//                while (pause && !stop) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        // ignore
//                    }
//                }
//                line.flush();
//                line.start();
//            }
            try {
                Header header = stream.readFrame();
                if (header == null) {
                    break;
                }
//                if (decoder.channels == 0) {
//                    int channels = (header.mode() == Header.MODE_SINGLE_CHANNEL) ? 1 : 2;
//                    float sampleRate = header.frequency();
//                    int sampleSize = 16;
//                    AudioFormat format = new AudioFormat(
//                            AudioFormat.Encoding.PCM_SIGNED, sampleRate,
//                            sampleSize, channels, channels * (sampleSize / 8),
//                            sampleRate, true);
//                    // big endian
//                    SourceDataLine.Info info = new DataLine.Info(
//                            SourceDataLine.class, format);
//                    line = (SourceDataLine) AudioSystem.getLine(info);
//                    if (BENCHMARK) {
//                        decoder.initOutputBuffer(null, channels);
//                    } else {
//                        decoder.initOutputBuffer(line, channels);
//                    }
//                    // TODO sometimes the line can not be opened (maybe not enough system resources?): display error message
//                    // System.out.println(line.getFormat().toString());
//                    line.open(format);
//                    line.start();
//                }
//                while (line.available() < 100) {
//                    Thread.yield();
//                    Thread.sleep(200);
//                }
//                decoder.decodeFrame(header, stream);
            } catch (Exception e) {
                if (error++ > 1000) {
                    break;
                }
                // TODO should not write directly
                System.out.println("Error at: " + " Frame: " + frame + " Error: " + e.toString());
                // e.printStackTrace();
            } finally {
                stream.closeFrame();
            }
        }
        if (error > 0) {
            System.out.println("errors: " + error);
        }
        //in.close();
        if (line != null) {
            line.stop();
            line.close();
            line = null;
        }

    }
    /**
     * sets current offset in file relative to the zero
     * byte in data section
     *
     * @param offset
     */
    @Override
    public void setCurrentPosition(int offset) {

    }

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     *
     * @return current offset in samples in file
     */
    @Override
    public int getCurrentPosition() {
        return 0;
    }

    /**
     * @return number of bytes in data chunk
     */
    @Override
    public int getDataLength() {
        return 0;
    }

    /**
     * getter for valid
     *
     * @return true, if file is valid
     */
    @Override
    public boolean isValid() {
        return false;
    }

    /**
     * reads certain number of bytes in wavFile
     *
     * @param nBytes      to read
     * @param samplesBuff
     * @return array of bytes read from the file
     */
    @Override
    public int readSampledBytes(int nBytes, byte[] samplesBuff) {
        return 0;
    }

    /**
     * @return format encoding in byte array data of audio file
     */
    @Override
    public AudioFormat.Encoding getEncoding() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public float getSampleRate() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getSampleSizeInBits() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int getChannels() {
        return 0;
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
}
