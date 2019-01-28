package psidev.psi.tools.xxindex.index;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author florian
 * Date: 01-Aug-2007
 * Time: 10:47:41
 */
public class NumberOfElementsTest {

    @Test
    public void numberOfElementsTest() throws IOException, URISyntaxException {
        String xpath = "/first/second/third/fourth";
        URL url = NumberOfElementsTest.class.getResource(".");
        File fileDir = new File(url.toURI());
        List<File> files = getTestFileList(fileDir, "test");
        for (File file : files) {
            FileInputStream fis = new FileInputStream(file);
            StandardXpathIndex index = XmlXpathIndexer.buildIndex(fis);
            List<IndexElement> brList = index.getElements(xpath);
            int elementCount = brList.size();
            Assert.assertEquals("Xml file '" + file.getName() + "' has to have 5 elements at '" + xpath + "'!", 5, elementCount);
        }
    }

    ////////////////////
    // Utilities

    private List<File> getTestFileList(File dir, String filePrefix) {
        List<File> files = new ArrayList<>();
        File[] fileArray = new File[0]; // initialise with empty array
        if ( !dir.isDirectory() ) {
            System.out.println("Parameter has to be a valid directory name: " + dir.getName());
        } else {
            fileArray = dir.listFiles();
        }
        for (File file : fileArray) {
            if ( file.isFile() && file.getName().startsWith(filePrefix) ) {
                files.add(file);
            }
        }
        return files;
    }
}