import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by OTER on 11/21/2014.
 */



public class Mp3Loader implements Loadable {

    private int frameCount;
    private boolean valid;
    private int currentFramePosition;

    AudioFormat audioFormat;
    Decoder decoder;
    Bitstream stream;

    InputStream in = null;

    public Mp3Loader( String filePath ){


        in = null;
        valid = true;

        try {
            in = new FileInputStream( filePath );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int maxFrameCount = Integer.MAX_VALUE;

        // int testing;
        // maxFrameCount = 100;

        //
        stream = new Bitstream( in );
        int error = 0;
        int frame;
        for (frame = 1; frame < maxFrameCount; frame++) {
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

        frameCount = frame;

        decoder = new Decoder();
        stream = new Bitstream( in );

        if (error > 0) {
            System.out.println("errors: " + error);
        }
    }
    /**
     * sets current position in file relative to the zero
     * byte in data section
     *
     * @param position
     */
    @Override
    public void setCurrentPosition(int position) {
        currentFramePosition = position;

    }

    /**
     * gets current offset in file relative to the zero
     * byte in data section
     *
     * @return current offset in samples in file
     */
    @Override
    public int getCurrentPosition() {
        return currentFramePosition;
    }

    /**
     * @return number of bytes in data chunk
     */
    @Override
    public int getDataLength() {
        return frameCount;
    }

    /**
     * getter for valid
     *
     * @return true, if file is valid
     */
    @Override
    public boolean isValid() {
        return valid;
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

        Header header = null;


        try {
            header = stream.readFrame();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (header == null) {
            return 0;
        }
                if (decoder.channels == 0) {
                    int channels = (header.mode() == Header.MODE_SINGLE_CHANNEL) ? 1 : 2;
                    float sampleRate = header.frequency();
                    int sampleSize = 16;
                    audioFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED, sampleRate,
                            sampleSize, channels, channels * (sampleSize / 8),
                            sampleRate, true);
                    // big endian
                    SourceDataLine.Info info = new DataLine.Info(
                            SourceDataLine.class, audioFormat);

                        //decoder.initOutputBuffer(line, channels);
                }
//                while (line.available() < 100) {
//                    Thread.yield();
//                    Thread.sleep(200);
//                }
        try {
            decoder.decodeFrame(header, stream);
            samplesBuff = decoder.getBuffer();

            return decoder.getBufferSize();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * @return format encoding in byte array data of audio file
     */
    @Override
    public AudioFormat.Encoding getEncoding() {

        return audioFormat.getEncoding();
    }

    /**
     * @return
     */
    @Override
    public float getSampleRate() {

        return audioFormat.getSampleRate();
    }

    /**
     * @return
     */
    @Override
    public int getSampleSizeInBits() {

        return audioFormat.getSampleSizeInBits();
    }

    /**
     * @return
     */
    @Override
    public int getChannels() {

        return audioFormat.getChannels();
    }

    /**
     * @return
     */
    @Override
    public int getFrameSize() {

        return audioFormat.getFrameSize();
    }

    /**
     * @return
     */
    @Override
    public float getFrameRate() {

        return audioFormat.getFrameRate();
    }
}
