package psidev.psi.tools.xxindex.index;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * BuildTagName Tester.
 *
 * @author Florian Reisinger
 * Date: 06-Aug-2007
 */
public class BuildTagNameTest {

    String controlTagName = "test";

    @Test
    public void buildTagNameTest() throws Exception {
        List<ByteBuffer> buffers = createTestByteBuffer();
        for ( ByteBuffer buffer : buffers ) {
            String tagName = XmlXpathIndexer.getTagName( buffer, true );
            Assert.assertEquals("Extracted tag name does not match expected: ", controlTagName, tagName );
        }
    }

    private List<ByteBuffer> createTestByteBuffer() throws URISyntaxException, IOException {
        List<ByteBuffer> bBuf = new ArrayList<>();
        URL url = BuildTagNameTest.class.getResource( "/testTagName" );
        File testDir = new File( url.toURI() ); // get the directory of the test files
        File[] files = testDir.listFiles();
        byte[] b = new byte[1];

        for (File file : files) {
            ByteBuffer bb = new ByteBuffer();
            FileInputStream fis = new FileInputStream(file);
            while ( fis.read(b) > 0 ) { // read byte by byte
                bb.append( b[0] ); // append each byte to the ByteBuffer
            }
            bBuf.add( bb );
        }
        return bBuf;
    }
}