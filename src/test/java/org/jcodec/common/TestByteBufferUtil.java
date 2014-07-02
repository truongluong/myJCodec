package org.jcodec.common;

import static org.junit.Assert.assertArrayEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author The JCodec project
 * 
 */
public class TestByteBufferUtil {

    @Test
    public void testSearch() {

        byte[] marker = new byte[] { 0, 0, 1 };

        ByteBuffer buf = ByteBuffer.wrap(new byte[] { 10, 11, 12, 0, 0, 0, 0, 0, 1, 53, 23, 13, 0, 0, 12, 0, 0, 1, 13,
                0, 23, 0, 0, 23, 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4 });

        assertArrayEquals(new byte[] { 10, 11, 12, 0, 0, 0 },
                NIOUtils.toArray(NIOUtils.search(buf, 0, marker)));
        assertArrayEquals(new byte[] { 0, 0, 1, 53, 23, 13, 0, 0, 12 },
                NIOUtils.toArray(NIOUtils.search(buf, 1, marker)));
        assertArrayEquals(new byte[] { 0, 0, 1, 13, 0, 23, 0, 0, 23 },
                NIOUtils.toArray(NIOUtils.search(buf, 1, marker)));
        assertArrayEquals(new byte[] { 0, 0, 1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4 },
                NIOUtils.toArray(NIOUtils.search(buf, 1, marker)));
    }
}
