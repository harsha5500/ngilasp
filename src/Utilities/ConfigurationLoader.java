/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author harsha
 */
public class ConfigurationLoader {

    public static void main(String[] args) {
        ContentHandler contentHandler = new ConfigParser();
        ErrorHandler errorHandler = new ParseErrorHandler();

        try {
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache."
                    + "xerces.parsers.SAXParser");

            parser.setContentHandler(contentHandler);
            parser.setErrorHandler(errorHandler);
            parser.parse("config/testConfig.xml");

        } catch (IOException ex) {
            Logger.getLogger(ConfigParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ConfigParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
