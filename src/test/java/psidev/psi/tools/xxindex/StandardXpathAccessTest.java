package psidev.psi.tools.xxindex;

import org.junit.Test;
import psidev.psi.tools.xxindex.index.IndexElement;
import psidev.psi.tools.xxindex.index.XmlElement;
import psidev.psi.tools.xxindex.index.XpathIndex;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Florian Reisinger
 * Date: 28-Jan-2008
 */
public class StandardXpathAccessTest {


    @Test
    public void testConstrainedXmlAccession() throws IOException, URISyntaxException {

        URL url = this.getClass().getResource( "/test-win1252-wo-header.xml" );
        File file = new File(url.toURI());
        StandardXpathAccess access = new StandardXpathAccess(file);
        XpathIndex index = access.getIndex();
        String xpath = "/first/second";
        if (index.containsXpath(xpath)) {
            List<IndexElement> elements = index.getElements(xpath);

            // -------------------------------------------------------------------------------------------
            // check that XML snippet Iterator and List of XML snippets contain the same XML
            for (IndexElement element : elements) {
                Iterator iter = access.getXmlSnippetIterator(xpath + "/third/fourth", element.getStart(), element.getStop());
                for (String snippet : access.getXmlSnippets(xpath + "/third/fourth", element.getStart(), element.getStop())) {
                    assertEquals( iter.next(), snippet);
                }
            }

            // -------------------------------------------------------------------------------------------
            // check that XML element Iterator and List of XML elements contain the same elements
            for (IndexElement element : elements) {
                Iterator<XmlElement> iter = access.getXmlElementIterator(xpath + "/third/fourth", element.getStart(), element.getStop());
                for (XmlElement xmlElement : access.getXmlElements(xpath + "/third/fourth", element.getStart(), element.getStop())) {
                    XmlElement currentElement = iter.next();
                    assertEquals( currentElement.getXmlSnippet(), xmlElement.getXmlSnippet());
                    assertEquals( currentElement.getStartPos(), xmlElement.getStartPos());
                }
            }

            // -------------------------------------------------------------------------------------------
            // check the number of <third> and <fourth> elements within the 1. <second> element
            IndexElement element = elements.get(0);
            long start = element.getStart();
            long stop = element.getStop();
            // the first 'second' element has 3 'third' elements
            assertEquals( 3, access.getXmlElements(xpath + "/third", start, stop).size());
            // and 4 'fourth' elements
            assertEquals( 4, access.getXmlElements(xpath + "/third/fourth", start, stop).size());

            // now test the same thing using the XML snippets instead of the XML elements
            // the first 'second' element has 3 'third' snippets
            assertEquals( 3, access.getXmlSnippets(xpath + "/third", start, stop).size());
            // and 4 'fourth' snippets
            assertEquals( 4, access.getXmlSnippets(xpath + "/third/fourth", start, stop).size());

            // -------------------------------------------------------------------------------------------
            // check the number of <third> and <fourth> elements within the 2. <second> element
            element = elements.get(1);
            start = element.getStart();
            stop = element.getStop();
            // the second 'second' element has 1 'third' element
            assertEquals( 1, access.getXmlElements(xpath + "/third", start, stop).size());
            // and 1 'fourth' element
            assertEquals( 1, access.getXmlElements(xpath + "/third/fourth", start, stop).size());

            // now test the same thing using the XML snippets instead of the XML elements
            // the second 'second' element has 1 'third' snippet
            assertEquals( 1, access.getXmlSnippets(xpath + "/third", start, stop).size());
            // and 1 'fourth' snippet
            assertEquals( 1, access.getXmlSnippets(xpath + "/third/fourth", start, stop).size());
        }
    }

    @Test
    public void testGetStartTag() throws IOException, URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource( "test-mzIdentML-CDATA.mzid" );
        assertNotNull(url);
        File file = new File(url.toURI());
        StandardXpathAccess access = new StandardXpathAccess(file);
        assertNotNull(access.getIndex());

        // check a cv element, it contains an 'id', 'fullName' and 'URI' attribute
        List<IndexElement> elements = access.getIndex().getElements("/mzIdentML/cvList/cv");
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        IndexElement element = elements.get(0);
        assertNotNull(element);
        String startTag = access.getStartTag(element);
        assertNotNull(startTag);
        System.out.println("cv start tag: " + startTag);
        assertTrue(startTag.contains("id"));
        assertTrue(startTag.contains("fullName"));
        assertTrue(startTag.contains("URI"));
        assertTrue(startTag.endsWith(">"));

        // now check another element, 'cvList' which does not contain any attributes
        elements = access.getIndex().getElements("/mzIdentML/cvList");
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        element = elements.get(0);
        assertNotNull(element);
        startTag = access.getStartTag(element);
        assertNotNull(startTag);
        System.out.println("cvList start tag: " + startTag);
        assertTrue(startTag.equals("<cvList>"));
        assertTrue(startTag.endsWith(">"));

        // and another one, just to make sure
        elements = access.getIndex().getElements("/mzIdentML/SequenceCollection/DBSequence");
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        element = elements.get(0);
        assertNotNull(element);
        startTag = access.getStartTag(element);
        assertNotNull(startTag);
        System.out.println("DBSequence start tag: " + startTag);
        assertTrue(startTag.contains("<DBSequence"));
        assertTrue(startTag.contains("id"));
        assertTrue(startTag.contains("length"));
        assertTrue(startTag.contains("SearchDatabase_ref"));
        assertTrue(startTag.contains("accession"));
        assertTrue(startTag.endsWith(">"));
    }


    /**
     * This is the same test as #testGetStartTag, but on a gz file (GZip compressed).
     * @throws IOException in case the file could not be handled with the GZ input stream.
     * @throws URISyntaxException if the file location was not represented as proper URI.
     */
    @Test
    public void testGetStartTagFromGzFile() throws IOException, URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource( "test-mzIdentML-CDATA.mzid.gz" );
        assertNotNull(url);
        File file = new File(url.toURI());
        StandardXpathAccess access = new StandardXpathAccess(file);
        assertNotNull(access.getIndex());

        // check a cv element, it contains an 'id', 'fullName' and 'URI' attribute
        List<IndexElement> elements = access.getIndex().getElements("/mzIdentML/cvList/cv");
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        IndexElement element = elements.get(0);
        assertNotNull(element);
        String startTag = access.getStartTag(element);
        assertNotNull(startTag);
        System.out.println("cv start tag: " + startTag);
        assertTrue(startTag.contains("id"));
        assertTrue(startTag.contains("fullName"));
        assertTrue(startTag.contains("URI"));
        assertTrue(startTag.endsWith(">"));

        // now check another element, 'cvList' which does not contain any attributes
        elements = access.getIndex().getElements("/mzIdentML/cvList");
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        element = elements.get(0);
        assertNotNull(element);
        startTag = access.getStartTag(element);
        assertNotNull(startTag);
        System.out.println("cvList start tag: " + startTag);
        assertTrue(startTag.equals("<cvList>"));
        assertTrue(startTag.endsWith(">"));

        // and another one, just to make sure
        elements = access.getIndex().getElements("/mzIdentML/SequenceCollection/DBSequence");
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        element = elements.get(0);
        assertNotNull(element);
        startTag = access.getStartTag(element);
        assertNotNull(startTag);
        System.out.println("DBSequence start tag: " + startTag);
        assertTrue(startTag.contains("<DBSequence"));
        assertTrue(startTag.contains("id"));
        assertTrue(startTag.contains("length"));
        assertTrue(startTag.contains("SearchDatabase_ref"));
        assertTrue(startTag.contains("accession"));
        assertTrue(startTag.endsWith(">"));
    }

}
