package com.gb.restaurant.CATPrintSDK;

/**
 * Created by apple on 22/11/17.
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

class ImageProcessing {
    private static int[][] Floyd16x16 = new int[][]{{0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170}, {192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106}, {48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154}, {240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90}, {12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166}, {204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102}, {60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150}, {252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86}, {3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169}, {195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105}, {51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153}, {243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89}, {15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165}, {207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101}, {63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149}, {254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85}};

    ImageProcessing() {
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
        return bmpGrayscale;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scaleWidth = (float)w / (float)bitmapWidth;
        float scaleHeight = (float)h / (float)bitmapHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
    }

    public static void format_K_dither16x16(int xsize, int ysize, byte[] orgpixels, boolean[] despixels) {
        int k = 0;

        for(int y = 0; y < ysize; ++y) {
            for(int x = 0; x < xsize; ++x) {
                if((orgpixels[k] & 255) > Floyd16x16[x & 15][y & 15]) {
                    despixels[k] = false;
                } else {
                    despixels[k] = true;
                }

                ++k;
            }
        }

    }

    public static void format_K_threshold(int xsize, int ysize, byte[] orgpixels, boolean[] despixels) {
        int graytotal = 0;
        int grayave = 1;
        int k = 0;

        int i;
        int j;
        int gray;
        for(i = 0; i < ysize; ++i) {
            for(j = 0; j < xsize; ++j) {
                gray = orgpixels[k] & 255;
                graytotal += gray;
                ++k;
            }
        }

        grayave = graytotal / ysize / xsize;
        k = 0;

        for(i = 0; i < ysize; ++i) {
            for(j = 0; j < xsize; ++j) {
                gray = orgpixels[k] & 255;
                if(gray > grayave) {
                    despixels[k] = false;
                } else {
                    despixels[k] = true;
                }

                ++k;
            }
        }

    }

    public static void format_K_threshold(Bitmap mBitmap) {
        int graytotal = 0;
        int grayave = 1;
        int graycnt = 1;
        int ysize = mBitmap.getHeight();
        int xsize = mBitmap.getWidth();

        int gray;
        int i;
        int j;
        for(i = 0; i < ysize; ++i) {
            for(j = 0; j < xsize; ++j) {
                gray = mBitmap.getPixel(j, i) & 255;
                if(gray != 0 && gray != 255) {
                    graytotal += gray;
                    ++graycnt;
                }
            }
        }

        grayave = graytotal / graycnt;

        for(i = 0; i < ysize; ++i) {
            for(j = 0; j < xsize; ++j) {
                gray = mBitmap.getPixel(j, i) & 255;
                if(gray > grayave) {
                    mBitmap.setPixel(j, i, -1);
                } else {
                    mBitmap.setPixel(j, i, -16777216);
                }
            }
        }

    }

    public static Bitmap alignBitmap(Bitmap bitmap, int wbits, int hbits, int color) {
        if(bitmap.getWidth() % wbits == 0 && bitmap.getHeight() % hbits == 0) {
            return bitmap;
        } else {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            int newwidth = (width + wbits - 1) / wbits * wbits;
            int newheight = (height + hbits - 1) / hbits * hbits;
            int[] newpixels = new int[newwidth * newheight];
            Bitmap newbitmap = Bitmap.createBitmap(newwidth, newheight, Config.ARGB_8888);

            for(int i = 0; i < newheight; ++i) {
                for(int j = 0; j < newwidth; ++j) {
                    if(i < height && j < width) {
                        newpixels[i * newwidth + j] = pixels[i * width + j];
                    } else {
                        newpixels[i * newwidth + j] = color;
                    }
                }
            }

            newbitmap.setPixels(newpixels, 0, newwidth, 0, 0, newwidth, newheight);
            return newbitmap;
        }
    }

    public static Bitmap adjustPhotoRotation(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate((float)orientationDegree, (float)bm.getWidth() / 2.0F, (float)bm.getHeight() / 2.0F);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError var4) {
            return null;
        }
    }

    private static int PixOffset(int w, int x, int y) {
        return y * w + x;
    }

    public static byte[] Image1ToNVData(int width, int height, boolean[] src) {
        int x = (width + 7) / 8;
        int y = (height + 7) / 8;
        int dstlen = 4 + x * y * 8;
        byte[] dst = new byte[dstlen];
        dst[0] = (byte)x;
        dst[1] = (byte)(x >> 8);
        dst[2] = (byte)y;
        dst[3] = (byte)(y >> 8);
        int idx = 4;
        int d = 0;

        for(int i = 0; i < width; ++i) {
            for(int j = 0; j < height; ++j) {
                int offset = PixOffset(width, i, j);
                if(j % 8 == 0) {
                    d = (src[offset]?1:0) << 7 - j % 8;
                } else {
                    d |= (src[offset]?1:0) << 7 - j % 8;
                }

                if(j % 8 == 7 || j == height - 1) {
                    dst[idx++] = (byte)d;
                }
            }
        }

        return dst;
    }

    public static byte[] Image1ToGSCmd(int width, int height, boolean[] src) {
        int x = (width + 7) / 8;
        int y = (height + 7) / 8;
        int dstlen = 4 + x * y * 8;
        byte[] dst = new byte[dstlen];
        dst[0] = 29;
        dst[1] = 42;
        dst[2] = (byte)x;
        dst[3] = (byte)y;
        int idx = 4;
        int d = 0;

        for(int i = 0; i < width; ++i) {
            for(int j = 0; j < height; ++j) {
                int offset = PixOffset(width, i, j);
                if(j % 8 == 0) {
                    d = (src[offset]?1:0) << 7 - j % 8;
                } else {
                    d |= (src[offset]?1:0) << 7 - j % 8;
                }

                if(j % 8 == 7 || j == height - 1) {
                    dst[idx++] = (byte)d;
                }
            }
        }

        return dst;
    }

    public static byte[] Image1ToRasterCmd(int width, int height, boolean[] src) {
        int x = (width + 7) / 8;
        int y = (height + 7) / 8 * 8;
        int dstlen = 8 + x * y;
        byte[] dst = new byte[dstlen];
        dst[0] = 29;
        dst[1] = 118;
        dst[2] = 48;
        dst[3] = 0;
        dst[4] = (byte)(x % 256);
        dst[5] = (byte)(x / 256);
        dst[6] = (byte)(y % 256);
        dst[7] = (byte)(y / 256);
        int idx = 8;
        int d = 0;

        for(int j = 0; j < height; ++j) {
            for(int i = 0; i < width; ++i) {
                int offset = PixOffset(width, i, j);
                if(i % 8 == 0) {
                    d = (src[offset]?1:0) << 7 - i % 8;
                } else {
                    d |= (src[offset]?1:0) << 7 - i % 8;
                }

                if(i % 8 == 7 || i == width - 1) {
                    dst[idx++] = (byte)d;
                }
            }
        }

        return dst;
    }

    public static byte[] Image1ToRasterData(int width, int height, boolean[] src) {
        int x = (width + 7) / 8;
        int dstlen = x * height;
        byte[] dst = new byte[dstlen];
        int idx = 0;
        int d = 0;

        for(int j = 0; j < height; ++j) {
            for(int i = 0; i < width; ++i) {
                int offset = PixOffset(width, i, j);
                if(i % 8 == 0) {
                    d = (src[offset]?1:0) << 7 - i % 8;
                } else {
                    d |= (src[offset]?1:0) << 7 - i % 8;
                }

                if(i % 8 == 7 || i == width - 1) {
                    dst[idx++] = (byte)d;
                }
            }
        }

        return dst;
    }

    public static byte[] Image1ToTM88IVRasterCmd(int width, int height, boolean[] src) {
        int x = (width + 7) / 8 * 8;
        int y = (height + 7) / 8 * 8;
        int dstlen = 26 + x * y / 8;
        byte[] dst = new byte[dstlen];
        dst[0] = 29;
        dst[1] = 56;
        dst[2] = 76;
        dst[3] = (byte)(x * y / 8 + 10 & 255);
        dst[4] = (byte)(x * y / 8 + 10 >> 8 & 255);
        dst[5] = (byte)(x * y / 8 + 10 >> 16 & 255);
        dst[6] = (byte)(x * y / 8 + 10 >> 24 & 255);
        dst[7] = 48;
        dst[8] = 112;
        dst[9] = 48;
        dst[10] = 1;
        dst[11] = 1;
        dst[12] = 49;
        dst[13] = (byte)(x % 256);
        dst[14] = (byte)(x / 256);
        dst[15] = (byte)(y % 256);
        dst[16] = (byte)(y / 256);
        byte[] cmdPrint = new byte[]{29, 56, 76, 2, 0, 0, 0, 48, 2};
        System.arraycopy(cmdPrint, 0, dst, dstlen - cmdPrint.length, cmdPrint.length);
        int idx = 17;
        int d = 0;

        for(int j = 0; j < height; ++j) {
            for(int i = 0; i < width; ++i) {
                int offset = PixOffset(width, i, j);
                if(i % 8 == 0) {
                    d = (byte)((src[offset]?1:0) << 7 - i % 8);
                } else {
                    d |= (byte)((src[offset]?1:0) << 7 - i % 8);
                }

                if(i % 8 == 7 || i == width - 1) {
                    dst[idx++] = (byte)d;
                }
            }
        }

        return dst;
    }

    public static byte[] eachLinePixToCmd(boolean[] src, int nWidth, int nMode) {
        int nHeight = src.length / nWidth;
        int nBytesPerLine = nWidth / 8;
        byte[] data = new byte[nHeight * (8 + nBytesPerLine)];
        int offset = 0;
        int k = 0;

        for(int i = 0; i < nHeight; ++i) {
            offset = i * (8 + nBytesPerLine);
            data[offset + 0] = 29;
            data[offset + 1] = 118;
            data[offset + 2] = 48;
            data[offset + 3] = (byte)(nMode & 1);
            data[offset + 4] = (byte)(nBytesPerLine % 256);
            data[offset + 5] = (byte)(nBytesPerLine / 256);
            data[offset + 6] = 1;
            data[offset + 7] = 0;

            for(int j = 0; j < nBytesPerLine; ++j) {
                data[offset + 8 + j] = (byte)((src[k]?128:0) | (src[k + 1]?64:0) | (src[k + 2]?32:0) | (src[k + 3]?16:0) | (src[k + 4]?8:0) | (src[k + 5]?4:0) | (src[k + 6]?2:0) | (src[k + 7]?1:0));
                k += 8;
            }
        }

        return data;
    }

    public static byte[] eachLinePixToCompressCmd(boolean[] src, int nWidth) {
        int nHeight = src.length / nWidth;
        int nBytesPerLine = nWidth / 8;
        byte[] data = new byte[nHeight * nBytesPerLine];
        int k = 0;

        int compresseddatalen;
        int y;
        for(compresseddatalen = 0; compresseddatalen < nHeight; ++compresseddatalen) {
            for(y = 0; y < nBytesPerLine; ++y) {
                data[compresseddatalen * nBytesPerLine + y] = (byte)((src[k]?128:0) | (src[k + 1]?64:0) | (src[k + 2]?32:0) | (src[k + 3]?16:0) | (src[k + 4]?8:0) | (src[k + 5]?4:0) | (src[k + 6]?2:0) | (src[k + 7]?1:0));
                k += 8;
            }
        }

        compresseddatalen = 0;

        byte[] line;
        for(y = 0; y < nHeight; ++y) {
            line = new byte[nBytesPerLine];
            System.arraycopy(data, y * nBytesPerLine, line, 0, nBytesPerLine);
            byte[] buf = CompressDataBuf(line);
            line = new byte[]{31, 40, 80, (byte)((int)((long)buf.length & 255L)), (byte)((int)(((long)buf.length & 65535L) >> 8))};
            compresseddatalen += line.length;
            compresseddatalen += buf.length;
        }

        byte[] compresseddatabytes = new byte[compresseddatalen];
        int offset = 0;

        for(y = 0; y < nHeight; ++y) {
            line = new byte[nBytesPerLine];
            System.arraycopy(data, y * nBytesPerLine, line, 0, nBytesPerLine);
            byte[] buf = CompressDataBuf(line);
            byte[] cmd = new byte[]{31, 40, 80, (byte)((int)((long)buf.length & 255L)), (byte)((int)(((long)buf.length & 65535L) >> 8))};
            System.arraycopy(cmd, 0, compresseddatabytes, offset, cmd.length);
            offset += cmd.length;
            System.arraycopy(buf, 0, compresseddatabytes, offset, buf.length);
            offset += buf.length;
        }

        return compresseddatabytes;
    }

    public static byte[] CompressDataBuf(byte[] src) {
        int srclen = src.length;
        byte[] buf = new byte[srclen * 2];
        byte ch = src[0];
        buf[0] = ch;
        int cnt = 1;
        int idx = 1;

        for(int i = 1; i < srclen; ++i) {
            while(src[i] == ch) {
                ++i;
                ++cnt;
                if(i >= srclen) {
                    break;
                }
            }

            if(i >= srclen) {
                buf[idx] = (byte)cnt;
                ++idx;
                break;
            }

            buf[idx] = (byte)cnt;
            buf[idx + 1] = ch = src[i];
            cnt = 1;
            idx += 2;
        }

        if((idx & 1) != 0) {
            buf[idx] = (byte)cnt;
            ++idx;
        }

        byte[] dst;
        if(idx >= srclen) {
            dst = new byte[srclen + 1];
            dst[0] = 0;
            System.arraycopy(src, 0, dst, 1, srclen);
            return dst;
        } else {
            dst = new byte[idx + 1];
            dst[0] = (byte)idx;
            System.arraycopy(buf, 0, dst, 1, idx);
            return dst;
        }
    }

    public static void PicZoom_ThreeOrder0(int srcw, int srch, int[] src, int dstw, int dsth, int[] dst) {
        if(dstw != 0 && dsth != 0 && srcw != 0 && srch != 0) {
            if(srcw == dstw && srch == dsth) {
                System.arraycopy(src, 0, dst, 0, src.length);
            } else {
                int k = 0;

                for(int y = 0; y < dsth; ++y) {
                    double srcy = ((double)y + 0.4999999D) * (double)srch / (double)dsth - 0.5D;

                    for(int x = 0; x < dstw; ++x) {
                        double srcx = ((double)x + 0.4999999D) * (double)srcw / (double)dstw - 0.5D;
                        dst[k++] = ThreeOrder0(srcw, srch, src, srcx, srcy);
                    }
                }

            }
        }
    }

    static int ThreeOrder0(int srcw, int srch, int[] src, double fx, double fy) {
        int x0 = (int)fx;
        if((double)x0 > fx) {
            --x0;
        }

        int y0 = (int)fy;
        if((double)y0 > fy) {
            --y0;
        }

        double fu = fx - (double)x0;
        double fv = fy - (double)y0;
        TARGB32[] pixel = new TARGB32[16];

        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < 4; ++j) {
                long x = (long)(x0 - 1 + j);
                long y = (long)(y0 - 1 + i);
                pixel[i * 4 + j] = Pixels_Bound(srcw, srch, src, x, y);
            }
        }

        double[] afu = new double[4];
        double[] afv = new double[4];
        afu[0] = SinXDivX(1.0D + fu);
        afu[1] = SinXDivX(fu);
        afu[2] = SinXDivX(1.0D - fu);
        afu[3] = SinXDivX(2.0D - fu);
        afv[0] = SinXDivX(1.0D + fv);
        afv[1] = SinXDivX(fv);
        afv[2] = SinXDivX(1.0D - fv);
        afv[3] = SinXDivX(2.0D - fv);
        float sR = 0.0F;
        float sG = 0.0F;
        float sB = 0.0F;
        float sA = 0.0F;

        for(int i = 0; i < 4; ++i) {
            float aR = 0.0F;
            float aG = 0.0F;
            float aB = 0.0F;
            float aA = 0.0F;

            for(int j = 0; j < 4; ++j) {
                aA = (float)((double)aA + afu[j] * (double)pixel[i * 4 + j].a);
                aR = (float)((double)aR + afu[j] * (double)pixel[i * 4 + j].r);
                aG = (float)((double)aG + afu[j] * (double)pixel[i * 4 + j].g);
                aB = (float)((double)aB + afu[j] * (double)pixel[i * 4 + j].b);
            }

            sA = (float)((double)sA + (double)aA * afv[i]);
            sR = (float)((double)sR + (double)aR * afv[i]);
            sG = (float)((double)sG + (double)aG * afv[i]);
            sB = (float)((double)sB + (double)aB * afv[i]);
        }

        byte a = (byte)((int)border_color((long)((double)sA + 0.5D)));
        byte r = (byte)((int)border_color((long)((double)sR + 0.5D)));
        byte g = (byte)((int)border_color((long)((double)sG + 0.5D)));
        byte b = (byte)((int)border_color((long)((double)sB + 0.5D)));
        return (int)(((long)a & 255L) << 24 | ((long)r & 255L) << 16 | ((long)g & 255L) << 8 | ((long)b & 255L) << 0);
    }

    static TARGB32 Pixels_Bound(int srcw, int srch, int[] src, long x, long y) {
        boolean IsInPic = true;
        if(x < 0L) {
            x = 0L;
            IsInPic = false;
        } else if(x >= (long)srcw) {
            x = (long)(srcw - 1);
            IsInPic = false;
        }

        if(y < 0L) {
            y = 0L;
            IsInPic = false;
        } else if(y >= (long)srch) {
            y = (long)(srch - 1);
            IsInPic = false;
        }

        TARGB32 result = Pixels(srcw, srch, src, x, y);
        if(!IsInPic) {
            result.a = 0;
        }

        return result;
    }

    static TARGB32 Pixels(int srcw, int srch, int[] src, long x, long y) {
        int pixel = src[(int)(y * (long)srcw + x)];
        return new TARGB32(pixel);
    }

    static double SinXDivX(double x) {
        float a = -1.0F;
        if(x < 0.0D) {
            x = -x;
        }

        double x2 = x * x;
        double x3 = x2 * x;
        return x <= 1.0D?1.0D * x3 - 2.0D * x2 + 1.0D:(x <= 2.0D?-1.0D * x3 - -5.0D * x2 + -8.0D * x - -4.0D:0.0D);
    }

    static long border_color(long Color) {
        return Color <= 0L?0L:(Color >= 255L?255L:Color);
    }

    public static byte[] GrayImage(int[] src) {
        int srclen = src.length;
        byte[] dst = new byte[srclen];

        for(int k = 0; k < srclen; ++k) {
            dst[k] = (byte)((int)((((long)src[k] & 16711680L) >> 16) * 19595L + (((long)src[k] & 65280L) >> 8) * 38469L + (((long)src[k] & 255L) >> 0) * 7472L >> 16));
        }

        return dst;
    }

    public static void ReverseBitmap(int srcw, int srch, int[] src) {
        int srclen = src.length;

        for(int i = 0; i < srclen; ++i) {
            src[i] = (int)(4278190080L | ~((long)src[i] & 16777215L));
        }

    }

    static class TARGB32 {
        public int b;
        public int g;
        public int r;
        public int a;

        public TARGB32(int argb) {
            this.a = (int)(((long)argb & 4294967295L) >> 24);
            this.r = (int)(((long)argb & 4294967295L) >> 16);
            this.g = (int)(((long)argb & 4294967295L) >> 8);
            this.b = (int)(((long)argb & 4294967295L) >> 0);
        }

        public int IntValue() {
            return (int)(((long)this.a & 255L) << 24 | ((long)this.r & 255L) << 16 | ((long)this.g & 255L) << 8 | ((long)this.b & 255L) << 0);
        }
    }

    static class TPicRegion {
        public int[] pdata;
        public int width;
        public int height;

        TPicRegion() {
        }
    }
}
