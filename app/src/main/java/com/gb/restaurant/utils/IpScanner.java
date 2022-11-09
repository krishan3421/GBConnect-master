package com.gb.restaurant.utils;

import java.util.List;
import android.util.Log;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class IpScanner extends Thread {
    private DatagramSocket a;
    private List<DeviceBean> b = new ArrayList();

    protected IpScanner() {
    }

    public void run() {
        try {
            Log.i("IpScannerRongta", "run: ");
            this.onSearchStart();
            this.a = new DatagramSocket();
            this.a.setSoTimeout(2000);
            byte[] var1 = new byte[1024];
            byte[] var2 = new byte[1024];
            InetAddress var3 = InetAddress.getByName("255.255.255.255");
            DatagramPacket var4 = new DatagramPacket(var1, var1.length, var3, 1460);

            for(int var5 = 0; var5 < 3; ++var5) {
                byte[] var6 = "MP4200FIND".getBytes();
                var4.setData(var6);
                this.a.setBroadcast(true);
                this.a.send(var4);
                DatagramPacket var7 = new DatagramPacket(var2, var2.length);

                try {
                    int var8 = 200;

                    while(var8-- > 0) {
                        var7.setData(var2);
                        this.a.receive(var7);
                        this.a(var7, var2);
                    }
                } catch (SocketTimeoutException var13) {
                }

                Log.i("IpScannerRongta", "IpScanner finished @" + var5);
            }

            this.onSearchFinish(this.b);
        } catch (IOException var14) {
            var14.printStackTrace();
            this.onSearchError(var14.getMessage());
        } finally {
            if (this.a != null) {
                this.a.close();
            }

        }

    }

    private void a(DatagramPacket var1, byte[] var2) {
        if (var1.getLength() > 0) {
            String var3 = var1.getAddress().getHostAddress();
            Iterator var4 = this.b.iterator();

            while(var4.hasNext()) {
                IpScanner.DeviceBean var5 = (IpScanner.DeviceBean)var4.next();
                if (var5.getDeviceIp().equals(var3)) {
                    Log.d("IpScannerRongta", "IP add before");
                    return;
                }
            }

            if (var2.length <= 0) {
                return;
            }

            byte[] var9 = new byte[11];
            System.arraycopy(var2, 0, var9, 0, 11);
            String var10 = new String(var9);
            if (var10 != null && var10.equals("MP4200FOUND")) {
                IpScanner.DeviceBean var6 = new IpScanner.DeviceBean();
                var6.setDeviceIp(var3);
                var6.setDevicePort(9100);
                var6.setDHCPEnable(var2[33] != 0);
                var9 = new byte[6];
                System.arraycopy(var2, 11, var9, 0, 6);
                StringBuilder var7 = new StringBuilder("");

                for(int var8 = 0; var8 < var9.length; ++var8) {
                    var7.append(String.format("%02X", var9[var8]));
                    if (var8 != var9.length - 1) {
                        var7.append("-");
                    }
                }

                var6.setMacAddress(var7.toString());
                this.b.add(var6);
            }
        }

    }

    public abstract void onSearchStart();

    public abstract void onSearchFinish(List<DeviceBean> var1);

    public abstract void onSearchError(String var1);

    public static class DeviceBean implements Serializable {
        String a;
        int b;
        String c;
        boolean d;

        public DeviceBean() {
        }

        public int hashCode() {
            return this.a.hashCode();
        }

        public boolean equals(Object var1) {
            return var1 instanceof IpScanner.DeviceBean ? this.a.equals(((IpScanner.DeviceBean)var1).getDeviceIp()) : super.equals(var1);
        }

        public String getDeviceIp() {
            return this.a;
        }

        public int getDevicePort() {
            return this.b;
        }

        public String getMacAddress() {
            return this.c;
        }

        public boolean isDHCPEnable() {
            return this.d;
        }

        public void setDeviceIp(String var1) {
            this.a = var1;
        }

        public void setDevicePort(int var1) {
            this.b = var1;
        }

        public void setMacAddress(String var1) {
            this.c = var1;
        }

        public void setDHCPEnable(boolean var1) {
            this.d = var1;
        }
    }
}
