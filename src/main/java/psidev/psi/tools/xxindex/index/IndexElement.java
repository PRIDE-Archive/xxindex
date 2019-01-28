package psidev.psi.tools.xxindex.index;

/**
 * Author: florian
 * Date: 18-Jan-2008
 * Time: 12:19:35
 */
public interface IndexElement {

    long NO_LINE_NUMBER = -1;

    void setValues(long start, long stop, long lineNumber);

    long getStart();
    void setStart(long start);

    long getStop();
    void setStop(long stop);

    long getLineNumber();
    void setLineNumber(long lineNumber);

    boolean hasLineNumber();

}
