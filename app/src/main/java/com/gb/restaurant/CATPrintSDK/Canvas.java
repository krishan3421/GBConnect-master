package com.gb.restaurant.CATPrintSDK;

/**
 * Created by apple on 22/11/17.
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;


public class Canvas extends IO {
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_BOTTOM_TO_TOP = 1;
    public static final int DIRECTION_RIGHT_TO_LEFT = 2;
    public static final int DIRECTION_TOP_TO_BOTTOM = 3;
    public static final int HORIZONTALALIGNMENT_LEFT = -1;
    public static final int HORIZONTALALIGNMENT_CENTER = -2;
    public static final int HORIZONTALALIGNMENT_RIGHT = -3;
    public static final int VERTICALALIGNMENT_TOP = -1;
    public static final int VERTICALALIGNMENT_CENTER = -2;
    public static final int VERTICALALIGNMENT_BOTTOM = -3;
    public static final int FONTSTYLE_BOLD = 8;
    public static final int FONTSTYLE_BOLD_MORE = 10;
    public static final int FONTSTYLE_UNDERLINE = 128;
    public static final int BARCODE_TYPE_CODE128 = 73;
    private static final String TAG = "Canvas";
    private IO IO = new IO();
    private Bitmap bitmap;
    private android.graphics.Canvas canvas;
    private Paint paint;
    private int dir;

    public Canvas(Bitmap bmp) {
    }

    public void Set(IO io) {
        if (io != null) {
            this.IO = io;
        }
    }

    public android.graphics.Canvas getCanvas() {
        return this.canvas;
    }

    public IO GetIO() {
        return this.IO;
    }

    public void CanvasBegin(int width, int height) {
        this.bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        this.canvas = new android.graphics.Canvas(this.bitmap);
        this.paint = new Paint();
        this.dir = 0;
        this.paint.setColor(-1);
        this.canvas.drawRect(0.0F, 0.0F, (float) width, (float) height, this.paint);
        this.paint.setColor(-16777216);

    }


    public void logDimenssions() {
        if (canvas != null) {
            Log.e(TAG, "CANVAS : " + canvas.getWidth() + " x " + canvas.getHeight());
        }
        if (bitmap != null) {
            Log.e(TAG, "BITMAP : " + bitmap.getWidth() + " x " + bitmap.getHeight());
        }
    }

    public void CanvasEnd() {
        this.paint = null;
        this.canvas = null;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public float getStringWidth(String s) {
        return paint.measureText(s);
    }

    public void CanvasPrint(int nBinaryAlgorithm, int nCompressMethod) {
        if (this.IO.IsOpened()) {
            this.IO.Lock();

            try {
                Bitmap mBitmap = this.bitmap;
                int nWidth = this.bitmap.getWidth();
                int dstw = (nWidth + 7) / 8 * 8;
                int dsth = mBitmap.getHeight() * dstw / mBitmap.getWidth();
                int[] dst = new int[dstw * dsth];
                mBitmap = ImageProcessing.resizeImage(mBitmap, dstw, dsth);
                mBitmap.getPixels(dst, 0, dstw, 0, 0, dstw, dsth);
                byte[] gray = ImageProcessing.GrayImage(dst);
                boolean[] dithered = new boolean[dstw * dsth];
                if (nBinaryAlgorithm == 0) {
                    ImageProcessing.format_K_dither16x16(dstw, dsth, gray, dithered);
                } else {
                    ImageProcessing.format_K_threshold(dstw, dsth, gray, dithered);
                }

                byte[] data = null;
                if (nCompressMethod == 0) {
                    data = ImageProcessing.eachLinePixToCmd(dithered, dstw, 0);
                } else {
                    data = ImageProcessing.eachLinePixToCompressCmd(dithered, dstw);
                }

                this.IO.Write(data, 0, data.length);
            } catch (Exception var14) {
                Log.i("Canvas", var14.toString());
            } finally {
                this.IO.Unlock();
            }

        }
    }

    public void SetPrintDirection(int direction) {
        this.dir = direction;
    }

    private float degreeTo360(float degree) {
        if ((double) degree < 0.0D) {
            do {
                degree = (float) ((double) degree + 360.0D);
            } while ((double) degree < 0.0D);
        } else if ((double) degree >= 360.0D) {
            do {
                degree = (float) ((double) degree - 360.0D);
            } while ((double) degree >= 360.0D);
        }

        return degree;
    }

    private void measureTranslate(float w, float h, float x, float y, float tw, float th, DXDY dxdy, float rotation) {
        float dx = x;
        float dy = y;
        float abssinth = (float) Math.abs((double) th * Math.sin(0.017453292519943295D * (double) rotation));
        float abscosth = (float) Math.abs((double) th * Math.cos(0.017453292519943295D * (double) rotation));
        float abssintw = (float) Math.abs((double) tw * Math.sin(0.017453292519943295D * (double) rotation));
        float abscostw = (float) Math.abs((double) tw * Math.cos(0.017453292519943295D * (double) rotation));
        float dw = abssinth + abscostw;
        float dh = abscosth + abssintw;
        rotation = this.degreeTo360(rotation);
        if (rotation == 0.0F) {
            if (x == -1.0F) {
                dx = 0.0F;
            } else if (x == -2.0F) {
                dx = w / 2.0F - tw / 2.0F;
            } else if (x == -3.0F) {
                dx = w - tw;
            }

            if (y == -1.0F) {
                dy = 0.0F;
            } else if (y == -2.0F) {
                dy = h / 2.0F - th / 2.0F;
            } else if (y == -3.0F) {
                dy = h - th;
            }
        } else if (rotation > 0.0F && rotation < 90.0F) {
            if (x == -1.0F) {
                dx = abssinth;
            } else if (x == -2.0F) {
                dx = w / 2.0F - dw / 2.0F + abssinth;
            } else if (x == -3.0F) {
                dx = w - dw + abssinth;
            }

            if (y == -1.0F) {
                dy = 0.0F;
            } else if (y == -2.0F) {
                dy = h / 2.0F - dh / 2.0F;
            } else if (y == -3.0F) {
                dy = h - dh;
            }
        } else if (rotation == 90.0F) {
            if (x == -1.0F) {
                dx = th;
            } else if (x == -2.0F) {
                dx = w / 2.0F + th / 2.0F;
            } else if (x == -3.0F) {
                dx = w - y;
            }

            if (y == -1.0F) {
                dy = 0.0F;
            } else if (y == -2.0F) {
                dy = h / 2.0F - tw / 2.0F;
            } else if (y == -3.0F) {
                dy = h - tw;
            }
        } else if (rotation > 90.0F && rotation < 180.0F) {
            if (x == -1.0F) {
                dx = dw;
            } else if (x == -2.0F) {
                dx = w / 2.0F + dw / 2.0F;
            } else if (x == -3.0F) {
                dx = w - y;
            }

            if (y == -1.0F) {
                dy = abscosth;
            } else if (y == -2.0F) {
                dy = h / 2.0F - dh / 2.0F + abscosth;
            } else if (y == -3.0F) {
                dy = h - dh + abscosth;
            }
        } else if (rotation == 180.0F) {
            if (x == -1.0F) {
                dx = tw;
            } else if (x == -2.0F) {
                dx = w / 2.0F + tw / 2.0F;
            } else if (x == -3.0F) {
                dx = w;
            }

            if (y == -1.0F) {
                dy = th;
            } else if (y == -2.0F) {
                dy = h / 2.0F + th / 2.0F;
            } else if (y == -3.0F) {
                dy = h;
            }
        } else if (rotation > 180.0F && rotation < 270.0F) {
            if (x == -1.0F) {
                dx = dw - abscosth;
            } else if (x == -2.0F) {
                dx = w / 2.0F + dw / 2.0F - abscosth;
            } else if (x == -3.0F) {
                dx = w - abscosth;
            }

            if (y == -1.0F) {
                dy = dh;
            } else if (y == -2.0F) {
                dy = h / 2.0F + dh / 2.0F;
            } else if (y == -3.0F) {
                dy = h;
            }
        } else if (rotation == 270.0F) {
            if (x == -1.0F) {
                dx = 0.0F;
            } else if (x == -2.0F) {
                dx = (w - th) / 2.0F;
            } else if (x == -3.0F) {
                dx = w - th;
            }

            if (y == -1.0F) {
                dy = tw;
            } else if (y == -2.0F) {
                dy = (h + tw) / 2.0F;
            } else if (y == -3.0F) {
                dy = h;
            }
        } else if (rotation > 270.0F && rotation < 360.0F) {
            if (x == -1.0F) {
                dx = 0.0F;
            } else if (x == -2.0F) {
                dx = w / 2.0F - dw / 2.0F;
            } else if (x == -3.0F) {
                dx = w - dw;
            }

            if (y == -1.0F) {
                dy = dh - abscosth;
            } else if (y == -2.0F) {
                dy = h / 2.0F + dh / 2.0F - abscosth;
            } else if (y == -3.0F) {
                dy = h - abscosth;
            }
        }

        dxdy.dx = dx;
        dxdy.dy = dy;
    }

    public void DrawText(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle) {
        this.paint.setTypeface(typeface);
        this.paint.setTextSize(textSize);
        this.paint.setFakeBoldText((nFontStyle & 8) != 0);
        this.paint.setFakeBoldText((nFontStyle & 8) != 0);
        this.paint.setUnderlineText((nFontStyle & 128) != 0);
        float w = (float) this.canvas.getWidth();
        float h = (float) this.canvas.getHeight();
        FontMetricsInt fm = this.paint.getFontMetricsInt();
        float tw = this.paint.measureText(text);
        float th = (float) (fm.descent - fm.ascent);
        DXDY dxdy = new DXDY(null);
        this.canvas.save();
        if (this.dir == 0) {
            this.canvas.translate(0.0F, 0.0F);
        } else if (this.dir == 1) {
            this.canvas.translate(0.0F, h);
        } else if (this.dir == 2) {
            this.canvas.translate(w, h);
        } else if (this.dir == 3) {
            this.canvas.translate(w, 0.0F);
        }

        this.canvas.rotate((float) (this.dir * -90));
        if (this.dir != 0 && this.dir != 2) {
            this.measureTranslate(h, w, x, y, tw, th, dxdy, rotation);
        } else {
            this.measureTranslate(w, h, x, y, tw, th, dxdy, rotation);
        }

        this.canvas.translate(dxdy.dx, dxdy.dy);
        this.canvas.rotate(rotation);
        this.canvas.drawText(text, 0.0F, (float) (-fm.ascent), this.paint);
        this.canvas.restore();
    }


    public void drawFixedWidthText(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle) {
        TextRect textRect = new TextRect(this.paint);
        textRect.prepare(text,300,1000);
        textRect.draw(this.canvas,(int)x,(int)y);
    }

    private float getTextHeight(String text, Paint paint) {

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    public void DrawWhiteText(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle) {
        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(252, 252, 252));
        myPaint.setTypeface(typeface);
        myPaint.setTextSize(textSize);
        myPaint.setFakeBoldText((nFontStyle & 8) != 0);
        myPaint.setUnderlineText((nFontStyle & 128) != 0);

        float w = (float) this.canvas.getWidth();
        float h = (float) this.canvas.getHeight();
        FontMetricsInt fm = myPaint.getFontMetricsInt();
        float tw = myPaint.measureText(text);
        float th = (float) (fm.descent - fm.ascent);
        DXDY dxdy = new DXDY(null);
        this.canvas.save();
        if (this.dir == 0) {
            this.canvas.translate(0.0F, 0.0F);
        } else if (this.dir == 1) {
            this.canvas.translate(0.0F, h);
        } else if (this.dir == 2) {
            this.canvas.translate(w, h);
        } else if (this.dir == 3) {
            this.canvas.translate(w, 0.0F);
        }

        this.canvas.rotate((float) (this.dir * -90));
        if (this.dir != 0 && this.dir != 2) {
            this.measureTranslate(h, w, x, y, tw, th, dxdy, rotation);
        } else {
            this.measureTranslate(w, h, x, y, tw, th, dxdy, rotation);
        }

        this.canvas.translate(dxdy.dx, dxdy.dy);
        this.canvas.rotate(rotation);
        this.canvas.drawText(text, 0.0F, (float) (-fm.ascent), myPaint);
        this.canvas.restore();
    }


    public void DrawLine(float startX, float startY, float stopX, float stopY) {
        this.canvas.drawLine(startX, startY, stopX, stopY, this.paint);
    }

    public void DrawBox(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(5);

        //For Filled box (New code)
        DrawBoxPaint.setStyle(Paint.Style.FILL);
        DrawBoxPaint.setColor(Color.rgb(0, 0, 0));
        //DrawBoxPaint.setAlpha(0); // optional
        canvas.drawRect(left, top, right, bottom, DrawBoxPaint); // that's painting the whole canvas in the chosen color.

        //For border only (Old code)
//        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
//        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
//        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
//        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
    }


    public void DrawBoxOld(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(5);

        //For Filled box (New code)
        //DrawBoxPaint.setAlpha(0); // optional
        //canvas.drawRect(left, top, right, bottom, DrawBoxPaint); // that's painting the whole canvas in the chosen color.

        //For border only (Old code)
        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
    }


    public void DrawBoxLight(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(3);
        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
    }
    public void DrawBoxLightWhite(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(3);
        DrawBoxPaint.setColor(Color.rgb(252, 252, 252));
        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
    }
    public void DrawBoxLightWithCrossedLine(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(3);
        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, right, top, DrawBoxPaint);
    }
    public void DrawBoxLightWithCrossedLineWhite(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(3);
        DrawBoxPaint.setColor(Color.rgb(252, 252, 252));
        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, right, top, DrawBoxPaint);
    }

    public void DrawBoxWithText(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle) {
        this.paint.setTypeface(typeface);
        this.paint.setTextSize(textSize);
        this.paint.setFakeBoldText((nFontStyle & 8) != 0);
        this.paint.setUnderlineText((nFontStyle & 128) != 0);
        float textHeight = this.paint.descent() - this.paint.ascent();
        float textOffset = (textHeight / 2) - this.paint.descent();

        RectF bounds = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.drawText(text, bounds.centerX(), bounds.centerY() + textOffset, this.paint);

        canvas.drawRect(x, y - textSize, 300, 300, this.paint);
        canvas.drawText(text, x, y, this.paint); //x=300,y=300
    }

    public void DrawRect(float left, float top, float right, float bottom) {
        this.canvas.drawRect(left, top, right, bottom, this.paint);
    }

    public void DrawBlackBox(float left, float top, float right, float bottom) {
        Paint myPaint = new Paint();
        myPaint.setColor(Color.BLACK);
        this.canvas.drawRect(left, top, right, bottom, myPaint);
    }

    public void DrawBitmap(Bitmap bitmap, float x, float y, float rotation) {
        float w = (float) this.canvas.getWidth();
        float h = (float) this.canvas.getHeight();
        float tw = (float) bitmap.getWidth();
        float th = (float) bitmap.getHeight();
        DXDY dxdy = new DXDY((DXDY) null);
        this.canvas.save();
        if (this.dir == 0) {
            this.canvas.translate(0.0F, 0.0F);
        } else if (this.dir == 1) {
            this.canvas.translate(0.0F, h);
        } else if (this.dir == 2) {
            this.canvas.translate(w, h);
        } else if (this.dir == 3) {
            this.canvas.translate(w, 0.0F);
        }

        this.canvas.rotate((float) (this.dir * -90));
        if (this.dir != 0 && this.dir != 2) {
            this.measureTranslate(h, w, x, y, tw, th, dxdy, rotation);
        } else {
            this.measureTranslate(w, h, x, y, tw, th, dxdy, rotation);
        }

        this.canvas.translate(dxdy.dx, dxdy.dy);
        this.canvas.rotate(rotation);
        this.canvas.drawBitmap(bitmap, 0.0F, 0.0F, this.paint);
        this.canvas.restore();
    }


    private Bitmap QRModulesToBitmap(Boolean[][] modules, int unitWidth) {
        int height = modules.length * unitWidth;
        int width = height;
        int[] pixels = new int[height * height];
        Bitmap bitmap = Bitmap.createBitmap(height, height, Config.ARGB_8888);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                pixels[width * y + x] = modules[y / unitWidth][x / unitWidth].booleanValue() ? -16777216 : -1;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    private Bitmap BPatternToBitmap(boolean[] bPattern, int unitWidth, int height) {
        int width = unitWidth * bPattern.length;
        int[] pixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                pixels[width * y + x] = bPattern[x / unitWidth] ? -16777216 : -1;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private class DXDY {
        float dx;
        float dy;

        private DXDY(DXDY dxdy) {
        }
    }
}

