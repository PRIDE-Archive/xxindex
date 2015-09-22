package psidev.psi.tools.xxindex;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import psidev.psi.tools.xxindex.index.IndexElement;
import psidev.psi.tools.xxindex.index.StandardXpathIndex;
import psidev.psi.tools.xxindex.index.XmlXpathIndexer;
import psidev.psi.tools.xxindex.index.XpathIndex;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Author: Florian Reisinger
 * Date: 31-Jul-2007
 */
public class XmlXpathIndexerTest {

    ////////////////////
    // Utilities

    private String readByteRange( long from, long to, String filename, String encoding ) throws Exception {
        URL url = XmlXpathIndexerTest.class.getResource( filename );
        File f = new File( url.toURI() );
        SimpleXmlElementExtractor xee = new SimpleXmlElementExtractor();
        if (encoding != null) {
            xee.setEncoding( encoding );
        }
        return xee.readString( from, to, f );
    }

    private List<String> createTestFileList() {
        List<String> fileList = new ArrayList<String>();

        fileList.add( "/test-ascii-wrong-header.xml" );
        fileList.add( "/test-ascii-wrong-header-flat.xml" );

        fileList.add( "/test-utf8-header.xml" );
        fileList.add( "/test-utf8-header2.xml" );
        fileList.add( "/test-utf8-header-flat.xml" );
        fileList.add( "/test-utf8-header-flat2.xml" );
        fileList.add( "/test-utf8-wo-header.xml" );
        fileList.add( "/test-utf8-wo-header2.xml" );
        fileList.add( "/test-utf8-wo-header-flat.xml" );
        fileList.add( "/test-utf8-wo-header-flat2.xml" );

        fileList.add( "/test-win1252-wo-header.xml" );
        fileList.add( "/test-win1252-wo-header-flat.xml" );
        fileList.add( "/test-win1252-header.xml" );
        fileList.add( "/test-win1252-header-flat.xml" );

        return fileList;
    }

    private String detectFileEncoding(String filename) throws IOException {
        String result;
        URL url = XmlXpathIndexerTest.class.getResource(filename);
        SimpleXmlElementExtractor xee = new SimpleXmlElementExtractor();
        result = xee.detectFileEncoding( url );
        return result;
    }

    private void checkUniqueElementLineNumber( XpathIndex index, String xp, int expectedLineNumber ) {
        List<IndexElement> elements = index.getElements( xp );
        Assert.assertEquals( 1, elements.size() );
        final IndexElement element = elements.iterator().next();
        Assert.assertEquals( expectedLineNumber, element.getLineNumber() );
    }

    ////////////////////
    // Tests

    @Test
    public void xmlXpathIndexerTest1() throws Exception {
        List<String> fileList = createTestFileList();
        for ( String s : fileList ) {

            InputStream is = XmlXpathIndexerTest.class.getResourceAsStream(s);
            String encoding = detectFileEncoding(s);

            // ----- index creation ------ //
            StandardXpathIndex index = XmlXpathIndexer.buildIndex( is );
            is.close();
            Assert.assertNotNull( "Index was not created!", index );

            // ----- extraction of XML snippets using index and extractor ------ //
            String xpath = "/first/second/third/fourth";
            Assert.assertTrue( "The xpath '" + xpath + "' should be in the index!", index.containsXpath( "/first/second/third/fourth" ) );
            List<IndexElement> brList = index.getElements( xpath );
            Assert.assertEquals( "Unexpected number of entries for " + xpath + "!", 5, brList.size() );
            for ( IndexElement element : brList ) {
                String test = readByteRange( element.getStart(), element.getStop(), s, encoding );
                Assert.assertTrue( test.startsWith( "<fourth>" ) );
                Assert.assertTrue( test.endsWith( "</fourth>" ) );
            }

            // ----- check if the line number calculation works correct ----- //
            List<IndexElement> xeList = index.getElements( "/first/second/third" );
            IndexElement firstElement = xeList.get( 0 );
            IndexElement fourthElement = xeList.get( 3 );
            // we have to distinguish 3 file layouts:  all in one line, with header and without header
            if ( s.contains( "header-flat" ) ) { // all in one line
                Assert.assertEquals( 1, firstElement.getLineNumber() );
                Assert.assertEquals( 1, fourthElement.getLineNumber() );
            } else if ( s.contains( "-wo-header" ) ) { // without header
                Assert.assertEquals(  3, firstElement.getLineNumber() );
                Assert.assertEquals(  15, fourthElement.getLineNumber() );
            } else if ( s.contains( "header" ) ) { // the rest should be with header
                Assert.assertEquals(  4, firstElement.getLineNumber() );
                Assert.assertEquals(  16, fourthElement.getLineNumber() );
            } else {
                Assert.fail( "WARNING: This case is not currently handled, please update your test: " + s );
            }
        }
    }

    @Test
    public void xmlXpathIndexerTest2() throws Exception {
        List<String> fileList = createTestFileList();

        // ----- Create an inclusion set in this case ----- //
        HashSet<String> xpathInclusionSet = new HashSet<String>( 2 );
        xpathInclusionSet.add( "/first/second" );
        xpathInclusionSet.add( "/first/second/third/" );

        // ----- now check each file in the list ----- //
        for ( String s : fileList ) {

            InputStream is = XmlXpathIndexerTest.class.getResourceAsStream(s);
            String encoding = detectFileEncoding(s);

            if ( s.contains("wo-header") ) {
                Assert.assertNull(s, encoding);
            } else if( s.contains( "utf8" ) ) {
                Assert.assertEquals( s, "UTF-8", encoding );
            } else if( s.contains( "ascii" ) ) {
                Assert.assertEquals( s, "ASCII", encoding );
            } else if( s.contains( "win1252" ) ) {
                Assert.assertEquals( s, "windows-1252", encoding );
            } else {
                Assert.fail("Unsupported encoding type '"+ encoding +"', please update the test !!");
            }

            // ----- index creation ------ //
            XpathIndex index = XmlXpathIndexer.buildIndex( is, xpathInclusionSet );
            is.close();

            Assert.assertNotNull( index );

            // ----- extraction of XML snippets using index and extractor ------ //
            List<IndexElement> brList;
            String xpath;

            xpath = "/first/second/third/fourth";
            Assert.assertFalse( index.containsXpath( xpath ) );
            brList = index.getElements( xpath );
            Assert.assertEquals( 0, brList.size() );
            // This seemingly repetitive test checks that the retrieval of a non-existing
            // xpath does not influence the functionality of the 'contains' method.
            Assert.assertFalse( index.containsXpath( xpath ) );

            xpath = "/first";
            brList = index.getElements( xpath );
            Assert.assertEquals( 0, brList.size() );

            xpath = "/first/second";
            brList = index.getElements( xpath );
            Assert.assertEquals( 2, brList.size() );

            xpath = "/first/second/third";
            brList = index.getElements( xpath );
            Assert.assertEquals( 4, brList.size() );
            for ( IndexElement element : brList ) {
                String test = readByteRange( element.getStart(), element.getStop(), s, encoding );
                Assert.assertTrue( test.startsWith( "<third>" ) );
                Assert.assertTrue( test.endsWith( "</third>" ) );
            }
        }
    }


    // ToDo: more tests on parsing, indexing and reading bits
    // e.g. check correct building of tag names (tags with attributes, spaces, new line,...)

//    @Test
//    public void lineNumber_1() throws Exception {
//        final String fileResource = "C:\\validator-data\\test20081104_A.mif25";
//
//        InputStream is = new FileInputStream( new File( fileResource ) );
//        XpathIndex index = null;
//        index = XmlXpathIndexer.buildIndex( is, null, true );
//        is.close();
//
//        Assert.assertNotNull( index );
//
//        Assert.assertTrue( index.isRecordLineNumber() );
//
//        checkUniqueElementLineNumber( index, "/entrySet", 4 );
//    }

    @Test
    public void dipSampleFile_lineNumber() throws Exception {
        final String fileResource = "/DIP-sample.xml";

        String encoding = detectFileEncoding( fileResource );
        Assert.assertEquals( "UTF-8", encoding );

        InputStream is = XmlXpathIndexerTest.class.getResourceAsStream( fileResource );
        XpathIndex index = XmlXpathIndexer.buildIndex( is, null, true );
        is.close();

        Assert.assertNotNull( index );

        Assert.assertTrue( index.isRecordLineNumber() );

        Assert.assertFalse( index.containsXpath( "a/b/c" ) );

        checkUniqueElementLineNumber( index, "/entrySet/entry/interactionList/interaction", 30 );
        checkUniqueElementLineNumber( index, "/entrySet/entry/interactionList/interaction/experimentList/experimentDescription", 36 );
    }


    @Test
    public void testCDATAHandling() throws IOException, URISyntaxException {

        // A mzIdentML file usually contains regular expressions stored in XML CDATA sections
        // Here we test a mzIdentML file that contains CDATA sections.

        URL url = XmlXpathIndexerTest.class.getClassLoader().getResource("test-mzIdentML-CDATA.mzid");
        Assert.assertNotNull("Test resource (test-mzIdentML-CDATA.mzid) not found.", url);

        File xmlFile = new File(url.toURI());
        Assert.assertTrue("Test file does not exist!", xmlFile.exists());

        // check that the index creating works if there are CDATA sections
        StandardXpathAccess access = new StandardXpathAccess(xmlFile);
        Assert.assertNotNull(access);

        // check if entries for all xpath have been created
        XpathIndex index = access.getIndex();
        Assert.assertEquals("Expected xpath", 109, index.getKeys().size());
//        System.out.println("number of xpath: " + index.getKeys().size());
//        for (String xpath : index.getKeys()) {
//            System.out.println("xpath: " + xpath);
//        }

        // now check if new lines in CDATA sections are taken into account correctly
        IndexElement ele1 = index.getElements("/mzIdentML/AnalysisProtocolCollection/SpectrumIdentificationProtocol/Enzymes/Enzyme").iterator().next();
        // the first Enzyme element should start at line 748 (before any CDATA section)
        Assert.assertEquals("Element not on expected line.", 748, ele1.getLineNumber());

        IndexElement ele2 = index.getElements("/mzIdentML/AnalysisProtocolCollection/SpectrumIdentificationProtocol/MassTable").iterator().next();
        // the first MassTable element should start at line 763 (after CDATA with line breaks)
        Assert.assertEquals("Element not on expected line.", 766, ele2.getLineNumber());

    }

    @Test
    public void testCommentHandling() throws IOException, URISyntaxException {
        
        // A XML file can contain comments between <!-- and -->
        // Here we test a XML file that contains comments (one line and multi-line).

        String fileName = "test-comments.xml";
        URL url = XmlXpathIndexerTest.class.getClassLoader().getResource(fileName);
        Assert.assertNotNull("Test resource (" + fileName + ") not found.", url);

        File xmlFile = new File(url.toURI());
        Assert.assertTrue("Test file does not exist!", xmlFile.exists());

        // check that the index creating works if there are CDATA sections
        StandardXpathAccess access = new StandardXpathAccess(xmlFile);
        Assert.assertNotNull(access);

        // check if entries for all xpath have been created
        XpathIndex index = access.getIndex();
        Assert.assertNotNull(index);

        // one <third> element (including a <fourth> element) has been commented out!
        int thirdCnt = index.getElementCount("/first/second/third");
        Assert.assertEquals(4, thirdCnt);

        int fourthCnt = index.getElementCount("/first/second/third/fourth");
        Assert.assertEquals(5, fourthCnt);

    }


    @Test
    public void testCDATAandCommentHandling() throws IOException, URISyntaxException {

        // A XML file can contain comments between <!-- and -->
        // Here we test a XML file that contains comments (one line and multi-line).

        String fileName = "test-cdata.xml";
        URL url = XmlXpathIndexerTest.class.getClassLoader().getResource(fileName);
        Assert.assertNotNull("Test resource (" + fileName + ") not found.", url);

        File xmlFile = new File(url.toURI());
        Assert.assertTrue("Test file does not exist!", xmlFile.exists());

        // check that the index creating works if there are CDATA sections
        StandardXpathAccess access = new StandardXpathAccess(xmlFile);
        Assert.assertNotNull(access);

        // check if entries for all xpath have been created
        XpathIndex index = access.getIndex();
        Assert.assertNotNull(index);

        // one <third> element (including a <fourth> element) has been commented out!
        int thirdCnt = index.getElementCount("/first/second/third");
        Assert.assertEquals(4, thirdCnt);

        int fourthCnt = index.getElementCount("/first/second/third/fourth");
        Assert.assertEquals(5, fourthCnt);

        int regexCnt = index.getElementCount("/first/second/SiteRegexp");
        Assert.assertEquals(1, regexCnt);

    }

    @Test
    public void testIndexChecksum() throws Exception {

        URL fileUrl = XmlXpathIndexerTest.class.getResource( "/DIP-sample.xml" );

        File file = new File( fileUrl.toURI() );
        XpathIndex index = XmlXpathIndexer.buildIndex( new FileInputStream(file), null, true );

        String indexChecksum = index.getChecksum();
        long fileChecksum = FileUtils.checksumCRC32(file);

    }

}
