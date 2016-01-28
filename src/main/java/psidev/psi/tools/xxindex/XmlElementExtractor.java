package psidev.psi.tools.xxindex;

import java.io.IOException;
import java.net.URL;

/**
 * Author: florian
 * Date: 03-Aug-2007
 * Time: 14:03:56
 */
public interface XmlElementExtractor {
    int setEncoding(String encoding);

    byte[] readBytes(long from, long to) throws IOException;

    String readString(long from, long to) throws IOException;

    String detectFileEncoding(URL fileLocation) throws IOException;

    String detectFileEncoding(URL fileLocation, int length) throws IOException;

}
