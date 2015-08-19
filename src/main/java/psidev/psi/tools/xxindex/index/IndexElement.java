package psidev.psi.tools.xxindex.index;

/**
 * Author: florian
 * Date: 18-Jan-2008
 * Time: 12:19:35
 */
public interface IndexElement {

    public static final long NO_LINE_NUMBER = -1;

    public void setValues(long start, long stop, long lineNumber);

    public long getStart();
    public void setStart(long start);

    public long getStop();
    public void setStop(long stop);

    public long getLineNumber();
    public void setLineNumber( long lineNumber);

    public boolean hasLineNumber();

}
