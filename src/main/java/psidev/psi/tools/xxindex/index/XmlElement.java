package psidev.psi.tools.xxindex.index;

/**
 * Author: Florian Reisinger
 * Date: 23-Jul-2007
 */
public class XmlElement {

    private String xmlSnippet;
    private long startPos;

    public XmlElement(String xmlString, long startPosition) {
        this.xmlSnippet = xmlString;
        this.startPos = startPosition;
    }


    public String getXmlSnippet() {
        return xmlSnippet;
    }

    public void setXmlSnippet(String xmlSnippet) {
        this.xmlSnippet = xmlSnippet;
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }
}
