package psidev.psi.tools.xxindex.index;

import org.apache.commons.io.input.CountingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

/**
 * Indexes XML data so we know the begin and end position of specific elements. 
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Florian Reisinger
 *
 * @since 0.3
 * Date: 11-Jan-2008
 */
public class XmlXpathIndexer {

    private static Logger log = LoggerFactory.getLogger(XmlXpathIndexer.class);

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    ////////////////////
    // Index methods

    /**
     * This method indexes the XML file accessible via the specified inputstream.
     * All xpaths encountered will be included in the index!
     *
     * @param is    inputstream to the XML file to index.
     * @return the LineXpathIndex for the XML file.
     * @throws IOException when a IOException occurs during XML file access.
     */
    public static StandardXpathIndex buildIndex(InputStream is) throws IOException {
        return XmlXpathIndexer.buildIndex(is, null);
    }

    /**
     * This method indexes the XML file accessible via the specified inputstream.
     * All xpaths that do not correspond to one of the xpaths included
     * in the xpath exclusion set will be ignored and therefore omitted from the index!
     * Note: a value of 'null' is allowed for the aXpathInclusionSet parameter and will
     * produce the same result as buildIndex(InputStream is).  
     *
     * @param is    inputstream to the XML file to index.
     * @param aXpathInclusionSet    Set with the String representation
     *                              of the xpaths to include in the index.
     *                              <b>Note</b> that these xpaths should have
     *                              their trailing '/' removed!
     *                              <b>Also note</b> that any xpath not included
     *                              in this list will <b>not</b> be added
     *                              to the index! Can be 'null' to ensure
     *                              inclusion of all xpaths.
     * @return the LineXpathIndex for the XML file.
     * @throws IOException when a IOException occurs during XML file access.
     * @see this#buildIndex(java.io.InputStream) 
     */
    public static StandardXpathIndex buildIndex(InputStream is, Set<String> aXpathInclusionSet) throws IOException {
        return buildIndex(is, aXpathInclusionSet, true);
    }

    /**
     * This method indexes the XML file accessible via the specified inputstream.
     * All xpaths that do not correspond to one of the xpaths included
     * in the xpath exclusion set will be ignored and therefore omitted from the index!
     * Note: a value of 'null' is allowed for the aXpathInclusionSet parameter and will
     * produce the same result as buildIndex(InputStream is).
     *
     * @param is    inputstream to the XML file to index.
     * @param aXpathInclusionSet    Set with the String representation
     *                              of the xpaths to include in the index.
     *                              <b>Note</b> that these xpaths should have
     *                              their trailing '/' removed!
     *                              <b>Also note</b> that any xpath not included
     *                              in this list will <b>not</b> be added
     *                              to the index! Can be 'null' to ensure
     *                              inclusion of all xpaths.
     * @param recordLineNumber boolean flag to swith line number recording on or off.
     *                         If switched off, the created index will need less memory.
     * @return the LineXpathIndex for the XML file.
     * @throws IOException when a IOException occurs during XML file access.
     * @see this#buildIndex(java.io.InputStream)
     */
    public static StandardXpathIndex buildIndex(InputStream is, Set<String> aXpathInclusionSet, boolean recordLineNumber) throws IOException {
        return buildIndex(is, aXpathInclusionSet, recordLineNumber, true);
    }

    /**
     * This method indexes the XML file accessible via the specified inputstream.
     * All xpaths that do not correspond to one of the xpaths included
     * in the xpath exclusion set will be ignored and therefore omitted from the index!
     * Note: a value of 'null' is allowed for the aXpathInclusionSet parameter and will
     * produce the same result as buildIndex(InputStream is).
     *
     * @param is    inputstream to the XML file to index.
     * @param aXpathInclusionSet    Set with the String representation
     *                              of the xpaths to include in the index.
     *                              <b>Note</b> that these xpaths should have
     *                              their trailing '/' removed!
     *                              <b>Also note</b> that any xpath not included
     *                              in this list will <b>not</b> be added
     *                              to the index! Can be 'null' to ensure
     *                              inclusion of all xpaths.
     * @param recordLineNumber boolean flag to switch line number recording on or off.
     *                         If switched off, the created index will need less memory.
     * @param ignoreNSPrefix   boolean flag, if set to true (default) namespace prefixes (ending in ':')
     *                         will be ignored when reading tag names for the XML elements.
     * @return the LineXpathIndex for the XML file.
     * @throws IOException when a IOException occurs during XML file access.
     * @see this#buildIndex(java.io.InputStream)
     */
    public static StandardXpathIndex buildIndex(InputStream is, Set<String> aXpathInclusionSet, boolean recordLineNumber, boolean ignoreNSPrefix) throws IOException {
        BufferedInputStream bufStream = new BufferedInputStream(is);

        InputStream tmpStream;
        DigestInputStream digestStream;
        try {
            digestStream = new DigestInputStream(bufStream, MessageDigest.getInstance("MD5"));
            tmpStream = digestStream;
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate checksum!", e);
            digestStream = null;
            tmpStream = bufStream;
        }

        CountingInputStream countStream = new CountingInputStream( tmpStream );

        StandardXpathIndex index = new StandardXpathIndex(aXpathInclusionSet);

        // create a index that will or will not record the line number according to the specification
        if ( log.isDebugEnabled()) {
            log.debug( "Indexing " + (recordLineNumber ? "and" : "without") + " keeping track of line numbers." );
        }

        index.setRecordLineNumber(recordLineNumber);

        Stack<TmpIndexElement> stack = new Stack<TmpIndexElement>();
        byte[] buf = new byte[1];
        byte read = ' ';
        byte oldRead;
        long startPos = 0;
        long stopPos;
        boolean recording = false;
        boolean closingTag = false;
        boolean startTag = false;
        boolean inQuote = false;
        ByteBuffer bb = new ByteBuffer();

        long lineNum = 1; // initial line number (we start in the first line)

        while ( (nextByte(countStream, buf)) != -1 ) {
            oldRead = read; // save previous byte
            read = buf[0];
            // first keep track of all the line breaks, so we can count the line numbers
            if (read == '\n') { // normal 'new line'
                lineNum++;
            }
            if (oldRead == '\r' && read != '\n') { // carriage return that is not covered by the previous 'new line'
                lineNum++;
            }
            // now check for XML tags
            if ( read == '<' && !inQuote ) { // possible start tag
                startPos = countStream.getByteCount() -1; // we want the '<' included
                oldRead = read; // save previous byte
                nextByte(countStream, buf);
                read = buf[0];
                if ( read == '!' || read == '?' ) {
                    // we don't bother with header and comments
                    // read util the next '<' WITHOUT recording
                    int skippedLines = skipSpecialSection(countStream, buf);
                    lineNum += skippedLines;
                    startPos = -1; // reset position
                } else if ( read == '/' ) { // we have the start of a closing tag -> begin recording
                    closingTag = true;
                    recording = true;
                } else { // we have the start of a start tag -> begin recording
                    startTag = true;
                    recording = true;
                }
            }
            if (read == '"' && recording) {
                inQuote = !inQuote;
            }
            if ( read == '>' && !inQuote ) {
                stopPos = countStream.getByteCount();
                if ( startTag ) { // end of start tag
                    if ( oldRead == '/' ) { // self closing start tag
                        String tagName = getTagName(bb, ignoreNSPrefix);
                        bb.clear();
                        // since it is a self closing start tag, we can set the stop position already
                        TmpIndexElement element = new TmpIndexElement(tagName, startPos, stopPos, lineNum);
                        stack.push(element);
                        String xpath = createPathFromStack(stack);
                        stack.pop();
                        index.put(xpath, element.getStart(), element.getStop(), element.getLineNumber());
                    } else { // end of regular start tag
                        String tagName = getTagName(bb, ignoreNSPrefix);
                        bb.clear();
                        // only set start, since we don't know yet where this element ends
                        TmpIndexElement element = new TmpIndexElement(tagName, startPos, -1L, lineNum);
                        stack.push(element);
                    }
                    recording = false;
                    startTag = false;
                    // reset startPos ?
                    bb.clear();
                } else if ( closingTag ) { // end of regular closing tag
                    String tagName = getTagName(bb, ignoreNSPrefix);
                    bb.clear();
                    recording = false;
                    closingTag = false;
                    String xpath = createPathFromStack(stack);
                    TmpIndexElement element = stack.pop();
                    // check if found name is the last on stack
                    if ( !element.getName().equalsIgnoreCase(tagName) ) {
                        //ToDo: change to throw Exception, if this goes wrong, the index will be incorrect !!
                        StringBuilder sb = new StringBuilder( 256 );
                        sb.append("Tag name mismatch! Found '").append(tagName);
                        sb.append("' but '").append(element.getName()).append("' on stack.");
                        sb.append( "\n State of the Stack:\n" );
                        ListIterator<TmpIndexElement> iter = stack.listIterator();
                        while (iter.hasNext()) {
                            TmpIndexElement tmpIndexElement = iter.next();
                            sb.append("[");
                            sb.append(tmpIndexElement.getName());
                            sb.append(" at line ");
                            sb.append(tmpIndexElement.getLineNumber());
                            sb.append("]\n");
                        }
                        log.error( sb.toString() );
                        System.out.println(sb.toString());
                        System.out.println("line number" + lineNum);
                        throw new IllegalStateException("Internal stack of XML tags was corrupted!");
                    }
                    element.setStop(stopPos);
                    index.put(xpath, element.getStart(), element.getStop(), element.getLineNumber());
                    // reset stopPos ?
                }
            }
            if ( recording ) {
                bb.append(read);
            }
        } // end of reading

        if (digestStream != null) {
            byte[] checksum = digestStream.getMessageDigest().digest();
            String checksumHexString = asHex(checksum);
            index.setChecksum(checksumHexString);
        }

        countStream.close();
        is.close();
        return index;
    }

    /**
     * This method will skip a special XML section. A section is regarded 'special' if
     * the start tag starts with '<!' or '<![CD' (the start of a CDATA section). The end
     * of the section is expected to be a simple '>' or in case of a CDATA section a ']]>'.
     * NOTE: this method assumes that the two previous characters read are '<' and '!'.
     *
     * @param cis the counting input stream we are operating on.
     * @param buf the buffer to read (one byte at a time).
     * @return the number of new lines we have skipped.
     * @throws IOException in case of reading errors.
     */
    private static int skipSpecialSection(CountingInputStream cis, byte[] buf) throws IOException {
        // we know we are in a special section (starting with '<!'), now we have to find its end
        // this special section could be a xml header or even a CDATA section
        int skippedLines = 0;

        boolean possibleCDATA = false;
        boolean cDATA = false;
        boolean possibleComment = false;
        boolean comment = false;
        // we know we have read first '<' and then '!', otherwise we would not have entered this method.
        byte read = '!';
        byte oldRead = '<';
        byte veryOldRead;

        while ( (nextByte(cis, buf)) != -1 ) {
            veryOldRead = oldRead;
            oldRead = read;
            read = buf[0];
            // check for line breaks that we pass
            if (read == '\n') { // normal 'new line'
                skippedLines++;
            }
            if (oldRead == '\r' && read != '\n') { // carriage return that is not covered by the previous 'new line'
                skippedLines++;
            }

            // check if we have a CDATA section
            if (read == '[' && oldRead == '!' && veryOldRead == '<') {
                possibleCDATA = true;
            }
            if (read == 'D' && oldRead == 'C' && veryOldRead == '[' && possibleCDATA) {
                cDATA = true;
            }
            if (read == '-' && oldRead == '!' && veryOldRead == '<') {
                possibleComment = true;
            }
            if (read == '-' && oldRead == '-' && possibleComment) {
                comment = true;
            }
            // find the appropriate end of tag signal ('normal' = '>'; CDATA = ']]>'
            // so, if we are in a CDATA section we can not stop at at single '>', but
            // have to continue until we find ']]>'
            if (cDATA) {
                if (read == '>' && oldRead == ']' && veryOldRead == ']') {
                    break;
                } // else it is not a proper CDATA end.
            } else if (comment) {
                if (read == '>' && oldRead == '-' && veryOldRead == '-') {
                    break;
                }
            } else if (read == '>') {
                break;
            }
            // if we have not found a end signal, we continue skipping
        }
        // finally, when we are at the end of the skipped section, we return the number of lines we have skipped
        return skippedLines;
    }

    ////////////////////
    // Utilities

    /**
     * Convenients method to skip filling bytes. Returns the next useful byte.
     * @param cis the CountingInputStream of the file to index.
     * @param buf byte array to use as read buffer.
     * @return the total number of bytes read into the buffer, -1 if end of stream.
     * @throws IOException if an I/O error occurs.
     */
    private static int nextByte(CountingInputStream cis, byte[] buf) throws IOException {
        int result = cis.read(buf);
        while (result != -1 && buf[0] == 0 ){
            result = cis.read(buf);
        }
        return result;
    }

    /**
     * Method to extract the tag name out of the ByteBuffer containing all the bytes of the tag
     * (including attributes). The tag name is considered to start after '<' and end at any of the
     * characters ' ', '\t', '\n' or '\r'.
     *
     * @param bb ByteBuffer of all the bytes between '<' and '>'
     * @param ignoreNSPrefix  if set to true and the buffer contains a ':' before the name end is detected,
     *                        then all characters upto this position are regarded as prefix and ignored.        
     * @return String of the tag name.
     */
    protected static String getTagName(ByteBuffer bb, boolean ignoreNSPrefix) {
        ByteBuffer bbTmp = new ByteBuffer();
        // get name (every byte till first blank)
        for (Byte aByte : bb) {
            // append till name-attribute separating character (#x20 | #x9 | #xD | #xA) found
            if ( aByte == ' ' || aByte == '\t' || aByte == '\n' || aByte == '\r' ) {
                break;
            }
            bbTmp.append(aByte);

            if (ignoreNSPrefix && aByte == ':') {
                // if we encouter a ':' we have a namespace prefix
                // since we do not handle this, we just get rid of it: all the characters
                // in the byte buffer (including the current ':') are prefix and have to be removed
                bbTmp.clear();
            }
        }
        // might still contain a leading or trailing '/' from self closing start tags or regular closing tags
        // get rid of leading '/' if closing tag
        if ( bbTmp.get(0) == '/' ) {
            bbTmp.remove(0);
        }
        // get rid of trailing '/' if slef closing start tag
        if ( bbTmp.get(bbTmp.size()-1) == '/' ) {
            bbTmp.remove( bbTmp.size()-1 );
        }
        // forget about the rest in the buffer (e.g. attributes)
        return bbTmp.toString();
    }

    /**
     * Creates a xpath of the element names on the stack.
     * @param stack the stack of XmlElements to create the xpath from.
     * @return a xpath expression representing the elements in the stack.
     */
    private static String createPathFromStack(Stack<TmpIndexElement> stack) {
        StringBuilder path = new StringBuilder(100);
        path.append("/");
        for (TmpIndexElement element : stack) {
            path.append(element.getName()).append("/");
        }
        // Get rid of the trailing '/'.
        return path.substring(0, path.length()-1);
    }

    /**
     * Specialised convenience class only used within this indexer.
     * Extends the IndexElement class with a String containing the
     * name of the XML element (used to generate the xpath for the index)).
     */
    private static class TmpIndexElement extends LineNumberedByteRange {

        private String name;

        public TmpIndexElement(String name, long start, long stop, long lineNumber) {
            this.setValues(start, stop, lineNumber);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


    /**
     * Creates a hexadecimal String from a byte array of a File hash.
     *
     * @param buf the byte[] to turn into a hex string.
     * @return the hex encoded String representation of the byte array.
     * @see java.security.MessageDigest#digest()
     */
    private static String asHex(byte[] buf) {
        // from: http://forums.xkcd.com/viewtopic.php?f=11&t=16666&p=553936
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

}
