package psidev.psi.tools.xxindex.index;

import java.util.*;

/**
 * Author: Florian Reisinger
 * Date: 11-Jan-2008
 */
public class StandardXpathIndex implements XpathIndex {

    private Map<String, List<IndexElement>> index;

    private Set<String> xpathInclusionSet = null;

    private boolean recordLineNumber;

    private String checksum;

    ////////////////////
    // Constructors

    /**
     * Default constructor, will result in the creation of an index containing
     * all xpaths.
     */
    public StandardXpathIndex() {
        this(null);
    }

    /**
     * This constructor takes a set of xpaths to include in the index.
     * All xpaths that do not correspond to one of the xpaths included
     * in this set will be ignored and therefore omitted from the index!
     *
     * @param aXpathInclusionSet    Set with the String representation
     *                              of the xpaths to include in the index.
     *                              <b>Note</b> that these xpaths should have
     *                              their trailing '/' removed!
     *                              <b>Also note</b> that any xpath not included
     *                              in this list will <b>not</b> be added
     *                              to the index! Can be 'null' to ensure
     *                              inclusion of all xpaths.
     */
    public StandardXpathIndex(Set<String> aXpathInclusionSet) {
        index = new HashMap<>();
        // If we have a 'non-null' inclusion set, check it for trailing '/'
        // while initializing the inclusion set instance variable.
        // If the inclusion set is 'null', leave the instance variable 'null'as well -
        // it will then be ignored.
        if(aXpathInclusionSet != null) {
            xpathInclusionSet = new HashSet<>(aXpathInclusionSet.size());
            for (String s : aXpathInclusionSet) {
				if (s != null) {
					if (s.endsWith("/")) {
						s = s.substring(0, s.length() - 1);
					}
					xpathInclusionSet.add(s);
				}
            }
        }
        this.recordLineNumber = true;
    }


    ////////////////////
    // Getter + Setter

    public boolean isRecordLineNumber() {
        return recordLineNumber;
    }

    public void setRecordLineNumber(boolean recordLineNumber) {
        this.recordLineNumber = recordLineNumber;
    }

    public Set<String> getKeys() {
        return index.keySet();
    }

    public List<IndexElement> getElements(String xpath) {
        if(xpath.endsWith("/")) {
            xpath = xpath.substring(0, xpath.length()-1);
        }
        return index.computeIfAbsent(xpath, k -> new ArrayList<>());
    }

    public void put(String path, long start, long stop) {
        this.put(path, start, stop, -1);
    }

    public void put(String xpath, long start, long stop, long lineNumber) {
        // Check whether we have an inclusion list.
        if (xpathInclusionSet != null && !xpathInclusionSet.contains(xpath)) {
            return;
        }
        //ToDo: ? check wheather that range already exists (should never be the case, since we scan over the file only once)
        
        IndexElement element;
        if (recordLineNumber) {
            element = new LineNumberedByteRange(start, stop, lineNumber);
        } else {
            element = new ByteRange(start, stop, lineNumber);
        }

        this.getElements(xpath).add(element);
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    ////////////////////
    // Utilities

    /**
     * Thid method will return the number of elements (ByteRange) stored for the specified xpath expression.
     * The number of ByteRangeS represets the number of XML elements in the document the index was created on.
     * @param xpath the xpath to the element of interest (one key of the index).
     * @return the number of elements stored under this key or -1 if no entry with the specified key exists.
     */
    public int getElementCount(String xpath) {
        if(xpath.endsWith("/")) {
            xpath = xpath.substring(0, xpath.length()-1);
        }
        int cnt = -1;
        List <IndexElement> ranges = index.get(xpath);
        if ( ranges != null ) {
            cnt = ranges.size();
        }
        return cnt;
    }

    /**
     * Checks, if xpath is indexed.
     * @param xpath the XPath
     * @return true, if xpath is indexed; else false
     */
    @Override
    public boolean containsXpath(String xpath) {
        if (xpath != null && xpath.endsWith("/")) {
            xpath = xpath.substring(0, xpath.length()-1);
        }
        
        boolean result = false;
        if (this.index.containsKey(xpath)) {
            if (this.index.get(xpath).size() > 0) {
                result = true;
            }
        }
        
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for ( String key : index.keySet() ) {
            List<IndexElement> entries = getElements(key);
            sb.append("xPath: ");
            sb.append(key);
            sb.append(" entries: ").append(entries.size());
            sb.append("\n");
        }
        return sb.toString();
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        for ( String key : index.keySet() ) {
            List<IndexElement> entries = getElements(key);
            sb.append("xPath: ");
            sb.append(key);
            sb.append("\n");
            for (IndexElement element : entries) {
                sb.append("\tLocation : ");
                sb.append(element.getStart());
                sb.append("-");
                sb.append(element.getStop());
                sb.append(" in line: ");
                sb.append(element.getLineNumber());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    

}
