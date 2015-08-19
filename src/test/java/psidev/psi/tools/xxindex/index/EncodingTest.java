package psidev.psi.tools.xxindex.index;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import psidev.psi.tools.xxindex.SimpleXmlElementExtractor;

/**
 * @author: florian
 * Date: 01-Aug-2007
 * Time: 16:52:13
 */
public class EncodingTest {

    @Test
    public void encodingTest() throws Exception {

        List<String> fileList = createTestFileList();
        for (String s : fileList) {
            URL url = EncodingTest.class.getResource(s);
            InputStream is = url.openStream();
            StandardXpathIndex index = XmlXpathIndexer.buildIndex(is);
            IndexElement range = index.getElements("/first/second/third/fourth").get(1);

            SimpleXmlElementExtractor xee = new SimpleXmlElementExtractor();
            String encoding = xee.detectFileEncoding(url);
            if (encoding != null) {
                xee.setEncoding(encoding);
            }
            String xmlSnippet = xee.readString(range.getStart(), range.getStop(), new File(url.toURI()));
            Assert.assertTrue( xmlSnippet.startsWith("<fourth>"));
            Assert.assertTrue( xmlSnippet.endsWith("</fourth>"));
        }
    }

    private List<String> createTestFileList() {
        List<String> fileList = new ArrayList<String>();

        fileList.add( "/test-win1252-wo-header.xml" );
        fileList.add( "/test-win1252-wo-header-flat.xml" );

        fileList.add("/test-utf8-header.xml");
        fileList.add("/test-utf8-header2.xml");
        fileList.add("/test-utf8-header-flat.xml");
        fileList.add("/test-utf8-header-flat2.xml");
        fileList.add("/test-utf8-wo-header.xml");
        fileList.add("/test-utf8-wo-header2.xml");
        fileList.add("/test-utf8-wo-header-flat.xml");
        fileList.add("/test-utf8-wo-header-flat2.xml");

        fileList.add("/test-win1252-header.xml");
        fileList.add("/test-win1252-header-flat.xml");

        return fileList;
    }
}
