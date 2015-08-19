package psidev.psi.tools.xxindex;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Florian Reisinger
 *         Date: 21-Oct-2010
 * @since 0.10
 */
public class SimpleXmlElementExtractorTest {


    @Test
    public void testXmlHeaderRegex() {


        // test cases where encoding is present (various format alternatives, but same encoding string)
        List<String> positiveCases = new ArrayList<String>();
        positiveCases.add("<?xml version='1.1' encoding='ASCII' ?>");
        positiveCases.add("<?xml version=\"1.1\" encoding=\"ASCII\" ?>");
        positiveCases.add("  <?xml version='1.1' encoding='ASCII' ?>  ");
        positiveCases.add("<?xml  version = '1.1'   encoding  =  'ASCII'   ?>");
        positiveCases.add("<?xml version= '1.1' encoding='ASCII' ?>  \t \n ");
        positiveCases.add("<?xml  version = '1.1'   encoding = 'ASCII'   ?>");
        positiveCases.add("  <?xml encoding='ASCII' ?>  ");

        for (String test : positiveCases) {
            Matcher mHead = SimpleXmlElementExtractor.xmlHeader.matcher(test);
            Assert.assertTrue("There has to be a xml header!", mHead.matches());
            Matcher mEnc = SimpleXmlElementExtractor.xmlEnc.matcher(test);
            Assert.assertTrue("The encoding should be ASCII!", mEnc.matches());
            Assert.assertTrue(mEnc.groupCount() >= 1);
            Assert.assertNotNull(mEnc.group(1));
            Assert.assertEquals("ASCII", mEnc.group(1));
        }

        // test cases where encoding is present and the encoding string is different
        List<String> positiveCases2 = new ArrayList<String>();
        positiveCases2.add("<?xml version='1.1' encoding='UTF-8' ?>");
        positiveCases2.add("<?xml version=\"1.1\" encoding=\"ASCII\" ?>");
        positiveCases2.add("  <?xml version='1.1' encoding='ISO-8859-1' ?>  ");
        positiveCases2.add("<?xml  version = '1.1'   encoding  =  'ASCII'   ?>");
        positiveCases2.add("<?xml version= '1.1' encoding=\"UTF-16BE\" ?>  \t \n ");
        positiveCases2.add("<?xml  version = '1.1'   encoding = 'US-ASCII'   ?>");
        positiveCases2.add("  <?xml encoding='ASCII' ?>  ");

        for (String test : positiveCases2) {
            Matcher mHead = SimpleXmlElementExtractor.xmlHeader.matcher(test);
            Assert.assertTrue("There has to be a xml header!", mHead.matches());
            Matcher mEnc = SimpleXmlElementExtractor.xmlEnc.matcher(test);
            Assert.assertTrue("The encoding should be given! " + test, mEnc.matches());
            Assert.assertTrue(mEnc.groupCount() >= 1);
            Assert.assertNotNull(mEnc.group(1));
            Assert.assertTrue(mEnc.group(1).contains("ASCII") || mEnc.group(1).contains("UTF") || mEnc.group(1).contains("ISO"));
        }


        // test cases where the encoding is not present
        List<String> negativeCases = new ArrayList<String>();
        negativeCases.add("  <?xml version='1.1'?>  ");
        negativeCases.add("  <?xml ?>  ");

        for (String test : negativeCases) {
            Matcher mHead = SimpleXmlElementExtractor.xmlHeader.matcher(test);
            Assert.assertTrue("There has to be a xml header!", mHead.matches());
            Matcher mEnc = SimpleXmlElementExtractor.xmlEnc.matcher(test);
            Assert.assertFalse("There is no encoding!", mEnc.matches());
        }

        // test case where the whole XML header is missing
        Matcher mHead = SimpleXmlElementExtractor.xmlHeader.matcher("  <xml foo='bla' />");
        Assert.assertFalse("There is no xml header!", mHead.matches());



    }





}
