package psidev.psi.tools.xxindex;

import psidev.psi.tools.xxindex.index.XpathIndex;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Florian Reisinger
 * Date: 03-Aug-2007
 */
public interface XpathAccess {

    public List<String> getXmlSnippets(String xpath) throws IOException;

    public Iterator<String> getXmlSnippetIterator(String xpath);

    public Iterator<String> getXmlSnippetIterator(String xpath, Long start, Long stop);

    XpathIndex getIndex();
}
