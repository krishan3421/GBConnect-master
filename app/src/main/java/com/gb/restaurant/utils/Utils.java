package com.gb.restaurant.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.gb.restaurant.CATPrintSDK.Canvas;
import com.gb.restaurant.R;
import com.gb.restaurant.model.PrinterModel;
import com.gb.restaurant.model.order.Data;
import com.gb.restaurant.munbyn.MunbynPrinter;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.service.PosprinterService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.gb.restaurant.CATPrintSDK.Canvas.FONTSTYLE_BOLD;

public class Utils {

    static Boolean isBluetooth = false;
    static Boolean bPrintResult = false;
    public static String CURRENT_FONT = "Equip_Regular.ttf";

    public static IMyBinder myBinder;


    private android.graphics.Canvas canvas;

    public static Bitmap createOrderReceipt(Context ctx, Canvas mCanvas, int nPrintWidth, Data
            receiptData) {

        Bitmap bitmap = null;
        int lineHeight = 0;
        Canvas canvas = mCanvas;

        Typeface defaultFont;
        defaultFont = Typeface.createFromAsset(ctx.getAssets(), "fonts" + File.separator + CURRENT_FONT);


        int nPrintHeight = calculateHeight(receiptData);

        canvas.CanvasBegin(nPrintWidth, nPrintHeight);
        canvas.SetPrintDirection(0);

        Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.receipt_logo);

        if (icon != null) {
            canvas.DrawBitmap(icon, -2, lineHeight, 0);
            lineHeight += icon.getHeight();
        }

        lineHeight += 10;

        canvas.DrawText(receiptData.getDate2(), -2, lineHeight, 0, defaultFont, 30, 0);

        lineHeight += 40;

        canvas.DrawText("Order # " + receiptData.getId(), -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);

        lineHeight += 40;

        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);


        //canvas.DrawText(receiptData.getName(), 0, lineHeight, 0, defaultFont, 38, FONTSTYLE_BOLD);

//        lineHeight += 5;
//
//        canvas.DrawText(receiptData.getDate2(), -3, lineHeight, 0, defaultFont, 30, 0);

        lineHeight += 20;

        canvas.DrawText(receiptData.getName(), 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);

        lineHeight += 40;

        canvas.DrawText(receiptData.getMobile(), 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);


        lineHeight -= 30;
        canvas.DrawText((receiptData.getType() + " " + receiptData.getPayment()).toUpperCase(), -3, lineHeight, 0, defaultFont, 35, FONTSTYLE_BOLD);
        lineHeight += 30;


        lineHeight += 45;

        canvas.DrawText("Total Items - " + receiptData.getItems().size(), -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);

        lineHeight += 45;

        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);

        lineHeight += 15;

        canvas.DrawText("QTY", 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
        canvas.DrawText("ITEMS", 100, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
        canvas.DrawText("PRICE", -3, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);

        lineHeight += 40;

        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);


        lineHeight += 20;

        for (int i = 0; i < receiptData.getItems().size(); i++) {


            if (receiptData.getItems().get(i).getQty() != null) {
                canvas.DrawBoxLight(5, lineHeight-7, 55, lineHeight + 30);
                if (receiptData.getItems().get(i).getQty().length() == 1) {
                    canvas.DrawText("" + receiptData.getItems().get(i).getQty(), 25, lineHeight-5, 0, defaultFont, 30, FONTSTYLE_BOLD);
                } else if (receiptData.getItems().get(i).getQty().length() == 2) {
                    canvas.DrawText("" + receiptData.getItems().get(i).getQty(), 15, lineHeight-5, 0, defaultFont, 30, FONTSTYLE_BOLD);
                } else if (receiptData.getItems().get(i).getQty().length() == 3) {
                    canvas.DrawText("" + receiptData.getItems().get(i).getQty(), 10, lineHeight-5, 0, defaultFont, 30, FONTSTYLE_BOLD);
                }
            }

            // changes by krishan
           // canvas.DrawText("" + receiptData.getItems().get(i).getQty(), 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            String headingData = receiptData.getItems().get(i).getHeading();
            String[] listString= headingData.split("\\(");
            if(listString.length==2){
                headingData = listString[0]+"\n "+"("+listString[1];
                for(String line:headingData.split("\n")){
                    canvas.DrawText("" + line, 60, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
                    lineHeight+=20;
                }
            }
            canvas.DrawText("$" + receiptData.getItems().get(i).getPrice(), -3, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);

            lineHeight += 40;

            ArrayList<String> extraList;
            if (receiptData.getItems().get(i).getExtra() != null) {
                if (!receiptData.getItems().get(i).getExtra().isEmpty()) {
                    extraList = wrapLines(receiptData.getItems().get(i).getExtra(), 33);
                    for (String li : extraList) {
                        canvas.DrawText(li, 50, lineHeight, 0, defaultFont, 23, 0);
                        lineHeight += 35;
                    }
                }
            }


            ArrayList<String> instructionList;

            if (receiptData.getItems().get(i).getInstruction() != null) {
                if (!receiptData.getItems().get(i).getInstruction().isEmpty()) {
                    instructionList = wrapLines(receiptData.getItems().get(i).getInstruction(), 23);
                    int j = 0;
                    for (String li : instructionList) {

                        if (j == 0) {
                            canvas.DrawText(" >> " + li, 50, lineHeight, 0, defaultFont, 25, 0);
                        } else {
                            canvas.DrawText(li, 50, lineHeight, 0, defaultFont, 25, 0);
                        }
                        j++;
                        lineHeight += 40;
                    }
                }
            }

        }

        lineHeight += 10;


        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);

        lineHeight += 10;

        if (receiptData.getSubtotal() != null) {
            canvas.DrawText("Sub Total", 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            canvas.DrawText("$" + receiptData.getSubtotal(), -3, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 30;
        }

        if (receiptData.getTax() != null) {
            canvas.DrawText("Tax", 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            canvas.DrawText("$" + receiptData.getTax(), -3, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 30;
        }


        if (receiptData.getTip() != null) {
            canvas.DrawText("Tip", 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            canvas.DrawText("$" + receiptData.getTip(), -3, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 30;
        }

        lineHeight += 10;

        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);

        lineHeight += 10;

        if (receiptData.getTotal() != null) {
            canvas.DrawText("TOTAL: $" + receiptData.getTotal(), -3, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 30;
        }

        lineHeight += 10;

        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);
        lineHeight++;
        canvas.DrawLine(0, lineHeight, nPrintWidth, lineHeight);

        lineHeight += 10;

        ArrayList<String> instructionList;

        if (receiptData.getDetails() != null) {
            if (!receiptData.getDetails().isEmpty()) {
                instructionList = wrapLines(receiptData.getDetails(), 35);
                int i = 0;
                for (String li : instructionList) {
                    canvas.DrawText(li, 0, lineHeight, 0, defaultFont, 30, 0);
                    i++;
                    lineHeight += 40;
                }
            }
        }

        if (!receiptData.getName().isEmpty()) {
            canvas.DrawText(receiptData.getName(), -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 40;
        }
        if (!receiptData.getMobile().isEmpty()) {
            canvas.DrawText(receiptData.getMobile(), -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 40;
        }
        if (receiptData.getType().equalsIgnoreCase("Pickup")) {
            canvas.DrawText("Order: Pickup", -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 40;
        }else{
            canvas.DrawText("Order: Delivery", -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
            lineHeight += 40;
        }
        canvas.DrawText("Delivery address", 0, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
        lineHeight += 30;
        ArrayList<String> addressList;
        if (receiptData.getDelivery() != null) {
            if (!receiptData.getDelivery().isEmpty()) {
                addressList = wrapLines(receiptData.getDelivery(), 35);
                int i = 0;
                for (String li : addressList) {
                    canvas.DrawText(li, 0, lineHeight, 0, defaultFont, 30, 0);
                    i++;
                    lineHeight += 40;
                }

            }
        }


//        if (!receiptData.getrest().isEmpty()) {
//            canvas.DrawText(receiptData.getMobile(), -2, lineHeight, 0, defaultFont, 30, FONTSTYLE_BOLD);
//            lineHeight += 40;
//        }


        lineHeight += 10;

        canvas.CanvasEnd();
        System.gc();
        Bitmap bmpp = canvas.getBitmap().copy(Bitmap.Config.RGB_565, true);

        bitmap = Bitmap.createBitmap(bmpp, 0, 0, nPrintWidth, lineHeight);

        return bitmap;

    }

    public void DrawBoxLight(float left, float top, float right, float bottom) {
        Paint DrawBoxPaint = new Paint();
        DrawBoxPaint.setStrokeWidth(3);
        this.canvas.drawLine(left, top, right, top, DrawBoxPaint);
        this.canvas.drawLine(right, top, right, bottom, DrawBoxPaint);
        this.canvas.drawLine(right, bottom, left, bottom, DrawBoxPaint);
        this.canvas.drawLine(left, bottom, left, top, DrawBoxPaint);
    }


    public static Boolean munbynPrinting(Context mContext, Bitmap bitmap, int printerType, String targetAddress) {
        if (printerType == 1) {
            isBluetooth = true;
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                MunbynPrinter.getInstance().init(mContext,
                        targetAddress.replaceAll("\\-", "\\."),
                        printerType, new MunbynPrinter.MunbynCallBack() {
                            @Override
                            public void onMunbynConnect(int connectStatus, Boolean isSuccess) {

                                if (connectStatus == MunbynPrinter.CONNECT_SUCCESS) {
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        public void run() {
                                            MunbynPrinter.getInstance().printBitmap(bitmap);
                                        }
                                    }, 500);
                                }

                                if (connectStatus == MunbynPrinter.CONNECT_FAIL) {
                                    MunbynPrinter.getInstance().disConnect();
                                }
                            }

                            @Override
                            public void onMunbynWriteStatus(int status, String msg) {
                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    public void run() {
                                        if (status == 1) {
                                            MunbynPrinter.getInstance().disConnect();
                                        }
                                        bPrintResult = true;

                                    }
                                }, 2500);
                            }

                            @Override
                            public void onPrintFail(int status, String reason) {

                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                    public void run() {
                                        MunbynPrinter.getInstance().disConnect();
                                        bPrintResult = false;
                                    }
                                }, 500);
                            }
                        });

            }
        }, 500);

        return bPrintResult;
    }


    public static int calculateHeight(Data printOrder) {
        int lineHeight = 0;
        lineHeight += 202; // Header including top line


        try {

            int items = printOrder.getItems().size();
            lineHeight += 120 * items;
        } catch (Exception e) {
            e.printStackTrace();
        }

        lineHeight += 20;


        lineHeight += 100;
        lineHeight += 300;
        lineHeight += 300;
        return lineHeight;
    }


    private static ArrayList<String> wrapLines(String str, int WRAPLIMIT) {
        String[] pieces;
        ArrayList<String> line = new ArrayList<>();
        String[] tukda = org.apache.commons.lang3.StringUtils.split(str, "，,");
        if (tukda != null) {
            for (int i = 0; i < tukda.length; i++)
                tukda[i] = tukda[i].trim();

            str = TextUtils.join(", ", tukda);
            if (str.length() > WRAPLIMIT) {
                //pieces = str.split(" ");
                pieces = org.apache.commons.lang3.StringUtils.split(str, " ");
                String generatedLine = "";

                for (int i = 0; i < pieces.length; i++) {
                    if (generatedLine.isEmpty()) {
                        generatedLine += pieces[i];
                    } else if (generatedLine.length() + pieces[i].length() < WRAPLIMIT) {
                        generatedLine += " " + pieces[i];
                    } else {
                        line.add(generatedLine);
                        generatedLine = "";
                        generatedLine += "" + pieces[i];
//                        line.add(generatedLine);
//                        generatedLine = "";
                    }
                }

                if (!generatedLine.isEmpty())
                    line.add(generatedLine);
            } else {
                line.add(str);
                //return null;
            }
        }
        return line;
    }


    public static void pairBluetoothDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean containsString(String printer_id, ArrayList<PrinterModel> configuredPrinters) {
        boolean found = false;
        for (PrinterModel printerModel : configuredPrinters) {

            if (printerModel.getPrinter_id().equals(printer_id)) {
                found = true;
            }

        }
        return found;
    }


    @SuppressWarnings("deprecation")
    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    public static void setBluetooth(boolean enable) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (enable) {
                //Enable bluetooth
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
            } else {
                //Disable bluetooth
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }
            }
        }

    }

}
