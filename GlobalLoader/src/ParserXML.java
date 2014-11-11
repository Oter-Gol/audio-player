import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;
/**
 * Created by Oleh on 11.11.2014.
 */
public class ParserXML {
    private ArrayList<ParserXML> myFormats = new ArrayList<ParserXML>();
    private String name;
    private String [] extension;

    Document dom;

    /**
     * empty constructor
     */
    public ParserXML(){}

    /**
     * constructor which set name and extensions
     * @param name
     * @param extension
     */
    public ParserXML( String name, String [] extension ){
        this.name = name;
        this.extension = extension;
    }

    /**
     * getting a document builder
     */
    public void parseXmlFile(){
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse("D:\\audio-player\\GlobalLoader\\Formats\\Formats.xml");

        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch(SAXException se) {
            se.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * get a list of format elements
     * @param str by which field to parse
     */
    public void parseDocument( String str){
        //get the root element
        Element docEle =  dom.getDocumentElement();

        //get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName(str);

        if ( nl != null && nl.getLength() > 0 ) {
            for ( int i = 0; i < nl.getLength(); i++ ){
                //get the format element
                Element el = ( Element ) nl.item( i );

                //get the Format object
                ParserXML f = getFormat( el );

                //add it to list
                myFormats.add( f );
            }
        }
    }

    /**
     *
     * @param formEl
     * @return
     */
    private ParserXML getFormat( Element formEl ){

        //for each <Format> element get text value of name and extension
        String name = getTextValue( formEl, "name")[0];
        String [] extension;
        extension = getTextValue(formEl, "extension");
        //ParserXML f = new
        ParserXML f = new ParserXML(name, extension);

        return f;
    }

    private String[] getTextValue( Element ele, String tagName ){

        NodeList nl = ele.getElementsByTagName( tagName );
        String [] textVal = new String[ nl.getLength() ];
        for ( int i = 0; i < nl.getLength(); i++){
            if ( nl != null && nl.getLength() > 0 ) {
                Element el = ( Element ) nl.item( i );
                textVal[ i ] = el.getFirstChild().getNodeValue();
            }
        }

        return textVal;
    }

    public void printData(){
        //System.out.println("No of Employees '" + myFormats.size() + "'.");

        Iterator it = myFormats.iterator();
        while(it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();

        s.append("name is " + name + " extensions: " );
        for (int i = 0; i < extension.length; i++){
            s.append( extension[i] + " " );
        }
        return s.toString();
    }
}

