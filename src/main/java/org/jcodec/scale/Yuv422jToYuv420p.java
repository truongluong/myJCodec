package org.jcodec.scale;

import org.jcodec.common.model.Picture;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author The JCodec project
 * 
 */
public class Yuv422jToYuv420p implements Transform {
    public static int Y_COEFF = 7168;

    public Yuv422jToYuv420p() {
    }

    public void transform(Picture src, Picture dst) {
        int[] sy = src.getPlaneData(0);
        int[] dy = dst.getPlaneData(0);
        for (int i = 0; i < src.getPlaneWidth(0) * src.getPlaneHeight(0); i++)
            dy[i] = (sy[i] * Y_COEFF >> 13) + 16;

        copyAvg(src.getPlaneData(1), dst.getPlaneData(1), src.getPlaneWidth(1), src.getPlaneHeight(1));
        copyAvg(src.getPlaneData(2), dst.getPlaneData(2), src.getPlaneWidth(2), src.getPlaneHeight(2));
    }

    private void copyAvg(int[] src, int[] dst, int width, int height) {
        int offSrc = 0, offDst = 0;
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++, offDst++, offSrc++) {
                int a = ((src[offSrc] - 128) * Y_COEFF >> 13) + 128;
                int b = ((src[offSrc + width] - 128) * Y_COEFF >> 13) + 128;

                dst[offDst] = (a + b + 1) >> 1;
            }
            offSrc += width;
        }
    }
}
