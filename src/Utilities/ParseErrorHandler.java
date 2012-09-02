/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author harsha
 */
public class ParseErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        System.out.println("**Parsing Warning**\n"
                + " Line:" + exception.getLineNumber()
                + "\n" + " URI:" + exception.getSystemId()
                + "\n" + " Message: " + exception.getMessage());
        throw new SAXException("Warnning Encountered");
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        System.out.println("**Parsing Error**\n" + " Line:"
                + exception.getLineNumber() + "\n" + " URI:"
                + exception.getSystemId() + "\n" + " Message: "
                + exception.getMessage());
        throw new SAXException("Error encountered");
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        System.out.println("**Parsing Fatal Error**\n" + " Line:"
                + exception.getLineNumber() + "\n" + " URI:"
                + exception.getSystemId() + "\n" + " Message: "
                + exception.getMessage());
        throw new SAXException("Fatal Error encountered");
    }
}
