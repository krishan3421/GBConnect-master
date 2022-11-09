package com.gb.restaurant.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothDiscovery {

    private String TAG = BluetoothDiscovery.class.getSimpleName();

    Context context;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //private ArrayList<BluetoothDevice> mDeviceList;
    BluetoothDiscoveryListener bluetoothDiscoveryListener;
    AtomicBoolean retry = new AtomicBoolean(false);

    public BluetoothDiscovery(Context context, BluetoothDiscoveryListener bluetoothDiscoveryListener) {
        this.context = context;
        this.bluetoothDiscoveryListener = bluetoothDiscoveryListener;
        //mDeviceList = new ArrayList<BluetoothDevice>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);
        Log.i(TAG, "registerReceiver");
    }

    public void unregisterReceiver(){
        try {
            context.unregisterReceiver(mReceiver);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void startDiscovering() {
        if(!mBluetoothAdapter.isDiscovering())
            Log.i(TAG, "startDiscovering");
            mBluetoothAdapter.startDiscovery();
    }

    public void stopDiscovering() {
        if(mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action !=null){
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:

                        Log.i(TAG, "ACTION_STATE_CHANGED");
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_ON) {
                            Log.i(TAG, "ACTION_STATE_CHANGED: STATE_ON");
                        }
                        break;

                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        bluetoothDiscoveryListener.discoveryStarted();
                        //mProgressDlg.show();
                        Log.i(TAG, "ACTION_DISCOVERY_STARTED");
                        break;

                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        bluetoothDiscoveryListener.discoveryFinished();
                        Log.i(TAG, "ACTION_DISCOVERY_FINISHED");

                        break;

                    case BluetoothDevice.ACTION_FOUND:

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                       // if (isAPrinter(device)) {
                            //mDeviceList.add(device);
                            ArrayList<BluetoothDevice> oneItemList = new ArrayList<>();
                            oneItemList.add(device);
                            bluetoothDiscoveryListener.onBluetoothDeviceFound(oneItemList);

                       // }

                        Log.i(TAG, "Device found = " + device.getName());
                        break;

                    default:
                        Log.i(TAG, "Unknown Action");
                        break;
                }
            }
        }
    };

    private static boolean isAPrinter(BluetoothDevice device) {
        int printerMask = 0b000001000000011010000000;
        int fullCod = device.getBluetoothClass().hashCode();
        return (fullCod & printerMask) == printerMask;
    }

    public interface BluetoothDiscoveryListener {
        void discoveryStarted();
        void discoveryFinished();
        void onBluetoothDeviceFound(ArrayList<BluetoothDevice> mDeviceList);
    }
}
