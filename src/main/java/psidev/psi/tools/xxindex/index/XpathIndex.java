package psidev.psi.tools.xxindex.index;

import java.util.Set;
import java.util.List;

/**
 * Author: florian
 * Date: 03-Aug-2007
 * Time: 13:55:42
 */
public interface XpathIndex {
    
    Set<String> getKeys();

    List<IndexElement> getElements(String xpath);

    void put(String path, long start, long stop, long lineNumber);

    int getElementCount(String xpath);

    boolean containsXpath(String xpath);

    boolean isRecordLineNumber();

    String print();

    String getChecksum();
}
