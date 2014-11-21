import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by oleh on 21.11.14.
 */
public class Logger {
    /**
     * constructor to create a log file
     * if it exist, rewrite it
     */
    public Logger(){
        File file =new File("Logger/logs.log");

        //if file doesnt exists, then create it
        if(!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * append the date and line into logs file
     * @param line which line to add
     */
    public static void writeInFile( String line ){
        // current date and time
        String timeStamp = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss").format(Calendar.getInstance().getTime());

        try{
            //true = append file
            FileWriter fileWritter = new FileWriter("Logger/logs.log",true);

            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("[" + timeStamp + "] " + line + "\n");

            bufferWritter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
