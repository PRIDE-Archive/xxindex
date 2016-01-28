package psidev.psi.tools.xxindex;

import junit.framework.Assert;
import org.junit.Test;
import psidev.psi.tools.xxindex.index.IndexElement;
import psidev.psi.tools.xxindex.index.XmlXpathIndexer;
import psidev.psi.tools.xxindex.index.XpathIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * @author Florian Reisinger
 *         Date: 28/03/12
 * @since 0.12
 */
public class GzXmlElementExtractorTest {



    @Test
    public void testGzFileRead() throws URISyntaxException, IOException {
        URL url = GzXmlElementExtractorTest.class.getClassLoader().getResource("test-mzIdentML-CDATA.mzid.gz");
        Assert.assertNotNull("Test resource (test-mzIdentML-CDATA.mzid.gz) not found.", url);

        File xmlFile = new File(url.toURI());
        Assert.assertTrue("Test file does not exist!", xmlFile.exists());

        GZIPInputStream gzin = new GZIPInputStream(new FileInputStream(xmlFile), 1048576);
        XpathIndex index = XmlXpathIndexer.buildIndex(gzin);
        Assert.assertNotNull(index);

        List<IndexElement> indexElements = index.getElements("/mzIdentML/Provider/ContactRole");
        Assert.assertTrue(indexElements.size() == 1);

        IndexElement element = indexElements.get(0);
        Assert.assertNotNull(element);

        GzXmlElementExtractor extractor = new GzXmlElementExtractor(xmlFile);

        String readString = extractor.readString(element.getStart(), element.getStop());
        // check that we are in the right area
        Assert.assertTrue(readString.contains("researcher"));
        // now check that we have everything from the start tag
        Assert.assertTrue(readString.startsWith("<ContactRole"));
        // to the end tag
        Assert.assertTrue(readString.endsWith("ContactRole>"));


        // test some thing from the end of the file
        indexElements = index.getElements("/mzIdentML/BibliographicReference");
        Assert.assertTrue(indexElements.size() == 1);

        element = indexElements.get(0);
        Assert.assertNotNull(element);

        readString = extractor.readString(element.getStart(), element.getStop());
        // check that we are in the right area
        Assert.assertTrue(readString.contains("David M. Creasy"));
        // now check that we have everything from the start tag
        Assert.assertTrue(readString.startsWith("<BibliographicReference"));
        // to the end tag
        Assert.assertTrue(readString.endsWith("/>"));

    }

    @Test
    public void testNonGzFileRead() throws URISyntaxException, IOException {
        URL url = GzXmlElementExtractorTest.class.getClassLoader().getResource("test-mzIdentML-CDATA.mzid");
        Assert.assertNotNull("Test resource (test-mzIdentML-CDATA.mzid) not found.", url);

        File xmlFile = new File(url.toURI());
        Assert.assertTrue("Test file does not exist!", xmlFile.exists());

        boolean caughtException;
        try {
            new GZIPInputStream(new FileInputStream(xmlFile), 1048576);
            caughtException = false;
        } catch (IOException e) {
            caughtException = true;
        }
        // we should have caught an IOException
        Assert.assertTrue(caughtException);

        // now build the index
        FileInputStream in = new FileInputStream(xmlFile);
        XpathIndex index = XmlXpathIndexer.buildIndex(in);
        Assert.assertNotNull(index);

        List<IndexElement> indexElements = index.getElements("/mzIdentML/Provider/ContactRole");
        Assert.assertTrue(indexElements.size() == 1);

        IndexElement element = indexElements.get(0);
        Assert.assertNotNull(element);


        // this should throw an exception
        String readString = null;
        caughtException = false;
        try {
            GzXmlElementExtractor extractor = new GzXmlElementExtractor(xmlFile);
            readString = extractor.readString(element.getStart(), element.getStop());
            Assert.fail("We should have never got to this point! (using a GzExtractor on a non-gz file)");
        } catch (IllegalArgumentException iae) {
            caughtException = true;
        }
        Assert.assertTrue(caughtException);
        Assert.assertNull(readString);

    }

}
