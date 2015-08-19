package psidev.psi.tools.xxindex.index;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

/**
 * Author: florian
 * Date: 30-Jul-2007
 * Time: 18:19:10
 */
public class ByteBuffer implements Iterable<Byte>{

    private List<Byte> buffer;

    public ByteBuffer() {
        buffer = new ArrayList<Byte>();
    }

    public void append(byte b) {
        buffer.add(b);
    }

    public Byte get(int i) {
        return buffer.get(i);
    }

    public void remove(int i) {
        buffer.remove(i);
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }

    public byte[] toArray() {
        int liSize = buffer.size();
        // Dimension the resulting array.
        byte[] result =  new byte[liSize];
        for(int i=0;i<liSize;i++) {
            result[i] = buffer.get(i).byteValue();
        }
        return result;
    }

    public String toString(String charsetName) {
        byte[] bArray = new byte[buffer.size()];
        int i = 0;
        for (Byte aByte : buffer) {
            bArray[i] = aByte;
            i++;
        }
        String result;
        if ( charsetName == null ) {
            result = new String(bArray);
        } else {
            try {
                result =  new String(bArray, charsetName);
            } catch (UnsupportedEncodingException e) {
                result = "Unsupported Charset name: " + charsetName;
            }
        }
        return result;
    }

    public String toString() {
        return this.toString(null);
    }

    // for testing only
    protected String toStringByteByByte() {
        StringBuffer sb = new StringBuffer();
        for (Byte aByte : buffer) {
            byte[] b = new byte[1];
            b[0] = aByte;
            sb.append(new String(b));
            sb.append("-");
        }
        return sb.toString();
    }

    public Iterator<Byte> iterator() {
        return new ByteBufferIterator(buffer);
    }

    private class ByteBufferIterator implements Iterator<Byte> {

        private List<Byte> bBuf;
        private Iterator<Byte> iterator;

        protected ByteBufferIterator(List<Byte> buf) {
            this.bBuf = buf;
            iterator = this.bBuf.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Byte next() {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }
    }

}
