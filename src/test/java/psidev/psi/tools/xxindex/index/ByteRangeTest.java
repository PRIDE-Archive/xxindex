package psidev.psi.tools.xxindex.index;

import org.junit.*;

/**
 * ByteRange Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @since 0.3
 * @version $Id$
 */
public class ByteRangeTest {

    @Test
    public void allrounder() throws Exception {
        ByteRange br = new ByteRange( 2, 5, 1 );
        Assert.assertEquals( 2, br.getStart() );
        Assert.assertEquals( 5, br.getStop() );
        Assert.assertEquals( ByteRange.NO_LINE_NUMBER, br.getLineNumber() );

        br.setValues( 3, 6, 1 );
        Assert.assertEquals( 3, br.getStart() );
        Assert.assertEquals( 6, br.getStop() );
        Assert.assertEquals( ByteRange.NO_LINE_NUMBER, br.getLineNumber() );

        br.setLineNumber( 1000 );
        Assert.assertEquals( ByteRange.NO_LINE_NUMBER, br.getLineNumber() );
        Assert.assertFalse(br.hasLineNumber());

        br.toString();

        br = new ByteRange();
        Assert.assertEquals( -1, br.getStart() );
        Assert.assertEquals( -1, br.getStop() );
        Assert.assertEquals( ByteRange.NO_LINE_NUMBER, br.getLineNumber() );

//        try {
//            br.setValues( -3, 6, 1 );
//            fail("negative start not allowed.");
//        } catch ( Exception e ) {
//            // ok
//        }
//
//        try {
//            br.setValues( 3, -6, 1 );
//            fail("negative start not allowed.");
//        } catch ( Exception e ) {
//            // ok
//        }
    }
}
