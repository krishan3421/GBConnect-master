package com.gb.restaurant.munbyn;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.gb.restaurant.MyApp;
import com.gb.restaurant.utils.Util;
import com.gb.restaurant.utils.Utils;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;

import java.util.ArrayList;
import java.util.List;


public class MunbynPrinter {

    public static MunbynPrinter instance = null;

    public static boolean ISCONNECT = false;

    public static int CONNECT_SUCCESS = 1;
    public static int CONNECT_FAIL = 2;
    public static int DISCONNECT_SUCCESS = 3;
    public static int DISCONNECT_FAIL = 4;

    Context mContext;

    MunbynCallBack munbynCallBack;


    public static MunbynPrinter getInstance() {
        if (instance == null) {
            instance = new MunbynPrinter();
        }
        return instance;
    }

    public void init(Context mContext, String address, int printerType,
                     MunbynCallBack munbynCallBack) {
        this.mContext = mContext;
        this.munbynCallBack = munbynCallBack;

        boolean isConnected = ISCONNECT;

        if (isConnected) {
            MunbynPrinter.getInstance().disConnect();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                if (printerType == 2) {
                    connectNet(address);
                } else if (printerType == 1) {
                    connectBT(address);
                }
            }
        }, isConnected ? 500 : 0);
    }

    public void connectNet(String ip) {
        if (ip != null || ISCONNECT == false) {
            Utils.myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    ISCONNECT = true;
                    munbynCallBack.onMunbynConnect(CONNECT_SUCCESS, true);
                }

                @Override
                public void OnFailed() {
                    ISCONNECT = false;
                    munbynCallBack.onMunbynConnect(CONNECT_FAIL, false);
                    //Toast.makeText(mContext.getApplicationContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            munbynCallBack.onMunbynConnect(CONNECT_FAIL, false);
            Toast.makeText(mContext.getApplicationContext(), "Connect Fail", Toast.LENGTH_SHORT).show();
        }
    }


    public void connectBT(String BtAdress) {
        if (BtAdress.equals(null) || BtAdress.equals("")) {
            munbynCallBack.onMunbynConnect(CONNECT_FAIL, false);
            Toast.makeText(mContext, "Connect Fail", Toast.LENGTH_SHORT).show();
        } else {
            Utils.myBinder.ConnectBtPort(BtAdress, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    ISCONNECT = true;
                    munbynCallBack.onMunbynConnect(CONNECT_SUCCESS, true);
                    //Toast.makeText(mContext, "Connect Success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void OnFailed() {
                    ISCONNECT = false;
                    munbynCallBack.onMunbynConnect(CONNECT_FAIL, false);
                    //Toast.makeText(mContext, "Connect Fail", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void disConnect() {
        if (ISCONNECT) {
            Utils.myBinder.DisconnectCurrentPort(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    ISCONNECT = false;
                    munbynCallBack.onMunbynConnect(DISCONNECT_SUCCESS, false);
                }

                @Override
                public void OnFailed() {
                    ISCONNECT = true;
                    munbynCallBack.onMunbynConnect(DISCONNECT_FAIL, true);
                }
            });
        }
    }

    public void printBitmap(Bitmap bitmap1) {

        if (ISCONNECT) {
            Utils.myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    munbynCallBack.onMunbynWriteStatus(1, "Sent successfully");
                    Toast.makeText(mContext, "Sent successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void OnFailed() {
                    munbynCallBack.onPrintFail(0, "Print fail");
                    Toast.makeText(mContext, "Print Fail", Toast.LENGTH_SHORT).show();
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    List<Bitmap> blist = new ArrayList<>();
                    blist = BitmapProcess.cutBitmap(150, bitmap1);
                    for (int i = 0; i < blist.size(); i++) {
                        list.add(DataForSendToPrinterPos80.printRasterBmp(0, blist.get(i), BitmapToByteData.BmpType.Dithering, BitmapToByteData.AlignType.Center, 576));
                    }
//                    list.add(StringUtils.strTobytes("1234567890qwertyuiopakjbdscm nkjdv mcdskjb"));
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());

                    //To cut
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42, 0x66));

                    return list;
                }
            });
        } else {
            Toast.makeText(mContext, "Please connect the printer first!", Toast.LENGTH_SHORT).show();
        }
    }


    public interface MunbynCallBack {
        void onMunbynConnect(int connectionStatus, Boolean isSuccess);

        void onMunbynWriteStatus(int status, String msg);

        void onPrintFail(int status, String reason);
    }
}
