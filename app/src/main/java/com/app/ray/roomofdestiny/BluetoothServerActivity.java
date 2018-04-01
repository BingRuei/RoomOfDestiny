package com.app.ray.roomofdestiny;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BluetoothServerActivity extends Activity implements View.OnClickListener {
    public static final int MESSAGE_READ = 1;

    private static final String TAG = "BluetoothServer";
    private static final boolean DEBUG = true;

    private static final UUID MY_UUID = UUID.fromString("000011008-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBTAdapter;
    private AcceptThread mAcceptThread;
    private EditText view;
    private Button btn;

    private ArrayList arrayList = new ArrayList();
//    private ConnectedThread server;

    /**
     * 1. 取得藍芽服務 - onCreate
     * 2. 開啟藍芽 - onStart
     * 3. 設定為可被搜尋2分鐘 - setDiscoveralbe
     * 4. 啟動AcceptThread - onResume
     * 5. 關閉AcceptThread - onPause
     *
     * 備註：由於每個AcceptThread僅能接受一個請求
     * 所以假如要多個
     **/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_server);

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
        view = (EditText) findViewById(R.id.edt1);
//        view.setGravity(Gravity.TOP | Gravity.LEFT);

        // For checking device support Bluetooth or not
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            Log.i(TAG, "device does not support Bluetooth");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBTAdapter.isEnabled()) {
            // Turn on the bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Setting the device others device can find the device
            setDiscoveralbe();
        }
    }

    @Override
    protected void onPause() {
        mAcceptThread.cancel();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setting the status of the device being the server
        // others device can connect it
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                view.append("Enabled BT\n");

                setDiscoveralbe();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn:
                Log.i("1111", "onClick()");
                ConnectedThread server;
                String name, address, s;
                for (int i = 0; i < arrayList.size(); i++) {
                    server = (ConnectedThread) (((HashMap) arrayList.get(i)).get("SOCKET"));
                    name = (((HashMap) arrayList.get(i)).get("NAME")).toString();
                    address = (((HashMap) arrayList.get(i)).get("ADDRESS")).toString();
                    s = i + " | " + name + " | " + address;
                    server.write(s.getBytes());
                }
                break;
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBTAdapter.listenUsingRfcommWithServiceRecord("BluetoothServer", MY_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                        mAcceptThread = new AcceptThread();
                        mAcceptThread.start();
                    } catch (IOException e) {
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void manageConnectedSocket(BluetoothSocket socket) {
        if (DEBUG) Log.d(TAG, "Connected");
        ConnectedThread server = new ConnectedThread(mHandler, socket);
//        server = new ConnectedThread(mHandler, socket);
        server.start();
        server.write("Hello".getBytes());
        server.write(" Client".getBytes());

        HashMap map = new HashMap();
        map.put("SOCKET", server);
        map.put("ADDRESS", socket.getRemoteDevice().getAddress());
        map.put("NAME", socket.getRemoteDevice().getName());
        arrayList.add(map);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    view.append(readMessage);
            }
        }
    };

    private void setDiscoveralbe() {
        view.append("set BT as being discoverable during 2 minutes\n");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120); // 2 minutes
        startActivity(discoverableIntent);
    }
}
