package psidev.psi.tools.xxindex;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import psidev.psi.tools.xxindex.index.IndexElement;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: Florian Reisinger
 * Date: 24-Jul-2007
 */
@Deprecated
public class StandardXmlElementExtractor implements XmlElementExtractor {

    private static final Log log = LogFactory.getLog( StandardXmlElementExtractor.class );

    private boolean compareWithDetect;
    private boolean preferDetect;
    private Charset encoding;

    ////////////////////
    // Constructor

    public StandardXmlElementExtractor() {
        this.compareWithDetect = false;
        this.preferDetect = false;
        this.encoding = null;
    }

    public StandardXmlElementExtractor(Charset encoding) {
        this.compareWithDetect = false;
        this.preferDetect = false;
        this.encoding = encoding;
    }

    ////////////////////
    // Getter & Setter

    /**
     * @return true if comparing of the specified encoding with the detected encoding is enabled, false otherwise.
     */
    public boolean isCompareWithDetect() {
        return compareWithDetect;
    }

    /**
     * If set to true, the specified encoding will be checked and compared to the detected encoding on every
     * conversion from a byte array to a String.
     * A warning will be prouced if the encodings do not match.
     * @param compareWithDetect flag whether to check the endocing or not.
     */
    public void setCompareWithDetect(boolean compareWithDetect) {
        this.compareWithDetect = compareWithDetect;
    }

    /**
     *
     * @return true if the preference of the detected encoding over the given encoding is enabled, false otherwise
     */
    public boolean isPreferDetect() {
        return preferDetect;
    }

    /**
     * If set to true, the detected encoding will be used, irrespective of the specified encoding.
     * The encoding will be detected on the byte array to convert only, not the whole file. Therefore
     * it might not be correct!
     * @param preferDetect flag whether to prefer the detected encoding over the given encoding.
     */
    public void setPreferDetect(boolean preferDetect) {
        this.preferDetect = preferDetect;
    }

    /**
     *
     * @return the currently set encoding of null if non is set..
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     *
     * @param encoding the encoding to use when converting the read byte array into a String.
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * This method will try to find a Charset for the given String.
     * @param encoding the encoding to use when converting the read byte array into a String.
     * @return 0 is returned on success, -1 if the specified encoding is not valid and -2 if
     * the specified encoding is not supported by this virtual machine.
     */
    public int setEncoding(String encoding) {
        int result;
        try {
            this.encoding = Charset.forName(encoding);
            result = 0;
        } catch (IllegalCharsetNameException icne) {
            log.error("Illegal encoding: " + encoding, icne);
            result = -1;
        } catch (UnsupportedCharsetException ucne) {
            log.error("Unsupported encoding: " + encoding, ucne);
            result = -2;
        }
        return result;
    }

    ////////////////////
    // Methods


    /**
     * Same as readString(long from, long to, File file), but start and stop wrapped in a ByteRange object.
     * @param br the ByteRange to read.
     * @param file the file to read from.
     * @return the XML element including start and stop tag in a String.
     * @throws IOException if an I/O error occurs while reading.
     */
    public String readString(IndexElement br, File file) throws IOException {
        return readString(br.getStart(), br.getStop(), file);
    }

    /**
     * Convenience method that combines the methods: readBytes(), removeZeroBytes() and bytes2String().
     *
     * Read a String representing a XML element from the specified file (which will be opened read-only).
     * Will read from position 'from' for length 'to - from'.
     * @param from byte position of the start (incl. beginning of start tag) of the XML element.
     * @param to byte position of the end (incl. end of closing tag) of the XML element.
     * @param file the file containing the XML element.
     * @return the XML element including start and stop tag in a String.
     */
    public String readString(long from, long to, File file) throws IOException {
        // retrieve the bytes from the given range in the file
        byte[] bytes = readBytes(from, to, file);

        // remove all zero bytes (Mac filling bytes)
        byte[] newBytes = removeZeroBytes(bytes);

        // create a String from the rest using the given encoding if specified
        return bytes2String(newBytes);
    }

    /**
     * Retrieves bytes from the specified file from position 'from' for a length of 'to - from' bytes.
     * @param from where to start reading.
     * @param to how long to read.
     * @param file in which file to read.
     * @return what was read.
     * @throws IOException if a I/O Exception during the reading process occurs.
     */
    public byte[] readBytes(long from, long to, File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        // go to specified start position
        raf.seek(from);
        Long length = to - from;
        byte[] bytes = new byte[length.intValue()];

        // read into buffer
        raf.read(bytes, 0, length.intValue());
        raf.close();
        return bytes;
    }

    /**
     * Convenience method to strip the byte array of zero bytes (such as filling bytes used on Mac OSX).
     * @param bytes byte array that may contain zero bytes (\u0000)
     * @return byte array free of zero bytes.
     */
    public byte[] removeZeroBytes(byte[] bytes) {
        // This code is pretty low-level and may seem peculiar.
        // The reason for coding it this way is performance. If a
        // collection is used here rather than a staging array,
        // the performance drops immensely.
        byte[] temp = new byte[bytes.length];
        int count = 0;
        for (byte aByte : bytes) {
            if (aByte != (byte)0) {
                temp[count] = aByte;
                count++;
            }
        }

        // Now we know how many bytes we retrieved,
        // so create a smaller array for the final result        // if necessary.
        byte[] result;
        if (count != bytes.length){
            result = new byte[count];
            System.arraycopy(temp, 0, result, 0, count);
        } else {
            result = temp;
        }

        return result;
    }

    /**
     * Converts the specified byte array into a String, using the encoding of this StandardXmlElementExtractor or the
     * detected encoding if 'preferDetect' is enabled. If no encoding was set, the system default is used.
     * @param bytes the byte array to convert.
     * @return the String representation of the byte array.
     */
    public String bytes2String(byte[] bytes) {
        Charset detectedEnc = null;
        if (compareWithDetect) {
            detectedEnc = detectEncoding(bytes);
            if ( detectedEnc.compareTo(encoding) != 0 ) { // not the same name
                // if not the same name, check the aliases of the Charset
                Set<String> aliases = detectedEnc.aliases();
                if ( !aliases.contains(encoding.name()) ) {
                    if ( log.isWarnEnabled() ) {
                        log.warn("WARNING: specified encoding is not the same as the detected one. " +
                                 "Specified: " + encoding.name() + " detected: " + detectedEnc.name());
                    }
                }
            }
        }

        if (preferDetect) {
            if (detectedEnc != null) {
                encoding = detectedEnc;
            } else {
                encoding = detectEncoding(bytes);
            }
            if ( log.isDebugEnabled() ) log.info( "Using detected encoding: " + encoding.name());
        }

        String result = null;
        if (encoding == null) {
            if ( log.isDebugEnabled() ) log.info("Using system default for encoding.");
            result = new String(bytes);
        } else {
            try {
                result = new String(bytes, encoding.name());
            } catch (UnsupportedEncodingException e) {
                log.error("Specified encoding '" + encoding.name() + "' is not supported", e);
                // ToDo: throw new Exception
            }
        }
        return result;
    }

    /**
     *
     * @param bytes the byte array to try to detect the encoding from.
     * @return the detected encoding or the system default if detection attempt raised a Exception.
     */
    public Charset detectEncoding(byte[] bytes) {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
        detector.add(new ParsingDetector(false)); // be not verbose about parsing.
        detector.add(JChardetFacade.getInstance()); // Another singleton.
        detector.add(ASCIIDetector.getInstance()); // Fallback, see javadoc.

        Charset charset;
        try {
            charset = detector.detectCodepage(new ByteArrayInputStream(bytes), bytes.length);
        } catch (IOException e) {
            log.error("IOException trying to detect codepage from byte array, setting charset to default", e);
            charset = Charset.defaultCharset();
        }

        return charset;
    }

    protected String detectFileEncoding(String filename) throws IOException {
        URL url = StandardXmlElementExtractor.class.getResource(filename);
        return detectFileEncoding(url);
    }

    /**
     * Tries to auto-detect the file-encoding of the file specified by the URL.
     * @param fileLocation location of the file to check.
     * @return name of the detected Charset.
     * @throws IOException if an I/O error occurs.
     */
    public String detectFileEncoding(URL fileLocation) throws IOException {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
        detector.add(new ParsingDetector(false));   // be verbose about parsing.
        detector.add(JChardetFacade.getInstance()); // Another singleton.
        detector.add(ASCIIDetector.getInstance());  // Fallback, see javadoc.

        java.nio.charset.Charset charset = detector.detectCodepage(fileLocation);

        String charsetName = null;
        if (charset != null) {
            charsetName = charset.name();
        }
        return charsetName;
    }

    /**
     * Tries to auto-detect the file-encoding of the file specified by the URL, using length number of bytes.
     * @param fileLocation location of the file to check.
     * @param length the amount of bytes to take into account.
     * @return name of the detected Charset.
     * @throws IOException if an I/O error occurs.
     */
    public String detectFileEncoding(URL fileLocation, int length) throws IOException {
        InputStream in = fileLocation.openStream();

        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
        detector.add(new ParsingDetector(false)); // be verbose about parsing.
        detector.add(JChardetFacade.getInstance()); // Another singleton.
        detector.add(ASCIIDetector.getInstance()); // Fallback, see javadoc.

        java.nio.charset.Charset charset = detector.detectCodepage(in, length);

        String charsetName = null;
        if (charset != null) {
            charsetName = charset.name();
        }
        return charsetName;
    }
}
