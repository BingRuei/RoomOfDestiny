package com.app.ray.roomofdestiny;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

public class Client2Result extends Activity {

    /**
     * Waiting for getting item, it will showing on the screen.
     **/

    public static final int MESSAGE_READ = 1;

    private static final String TAG = "BluetoothClient";
    private static final boolean DEBUG = true;

    private static final UUID MY_UUID = UUID.fromString("000011008-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_DISCOVERY_BT = 1;
    private static final int REQUEST_ENABLE_BT = 3;

    private static final String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBTAdapter;
    private String mAddress;
    private Client2Result.ConnectThread mConnectThread;
    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client2_result);

        txtResult = (TextView) findViewById(R.id.txt_result);

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            Log.i(TAG, "device does not support Bluetooth");
        }

        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            doDiscovery();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mConnectThread != null)
            mConnectThread.cancel();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                txtResult.append("Enabled BT\n");
                doDiscovery();
            }
        } else if (requestCode == REQUEST_DISCOVERY_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                mAddress = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                doConnect();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            mBTAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            manageConnectedSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        if (DEBUG) Log.d(TAG, "Connected");
        ConnectedThread client = new ConnectedThread(mHandler, socket);
        client.start();
        String name = "Hi, Server";
        client.write(name.getBytes());
    }

    private void doDiscovery() {
        Intent intent = new Intent(getBaseContext(), Client1ChoosingRoom.class);
        startActivityForResult(intent, REQUEST_DISCOVERY_BT);
    }

    private void doConnect() {
        BluetoothDevice device = mBTAdapter.getRemoteDevice(mAddress);
        mConnectThread = new Client2Result.ConnectThread(device);
        mConnectThread.start();
        txtResult.append("Connecting...\n");
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    txtResult.append(readMessage);
                    txtResult.append("\n");
            }
        }
    };
}