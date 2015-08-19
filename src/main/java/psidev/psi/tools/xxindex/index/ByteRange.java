package psidev.psi.tools.xxindex.index;

/**
 * Holds the information about a potision in a sequence of bytes.
 *
 * @author florian
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * Date: 18-Jan-2008
 * @since 0.3
 */
public class ByteRange implements IndexElement {

    private long start = -1;
    private long stop = -1;

    public ByteRange() {
    }

    public ByteRange(long start, long stop, long lineNumber) {
        setStart( start );
        setStop( stop );
        // this ByteRange does not record the line number
    }

    public void setValues(long start, long stop, long lineNumber) {
        setStart( start );
        setStop( stop );
        // this ByteRange does not record the line number
    }

    public long getStart() {
        return start;
    }
    public void setStart(long start) {
//        if( start < 0 ) {
//            throw new IllegalArgumentException( "You must give a positive start: " + start );
//        }
        this.start = start;
    }

    public long getStop() {
        return stop;
    }
    public void setStop(long stop) {
//        if( stop < 0 ) {
//            throw new IllegalArgumentException( "You must give a positive stop: " + stop );
//        }
        this.stop = stop;
    }

    public long getLineNumber() {
        // return default value -1, since we don't record the line number
        return NO_LINE_NUMBER;
    }
    public void setLineNumber(long lineNumber) {
        // this ByteRange does not record the line number
    }

    public boolean hasLineNumber() {
        return false;
    }

    public String toString() {
        return "ByteRange{" +
                "start=" + start +
                ", stop=" + stop +
                '}';
    }
}
