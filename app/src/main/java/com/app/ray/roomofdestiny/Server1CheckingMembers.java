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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Server1CheckingMembers extends Activity implements View.OnClickListener {

    /**
     * Collecting the sockets of members,
     * and conveying the sockets  to next page
     **/

    public static final int MESSAGE_READ = 1;

    private static final String TAG = "BluetoothServer";
    private static final boolean DEBUG = true;

    private static final UUID MY_UUID = UUID.fromString("000011008-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBTAdapter;
    private Server1CheckingMembers.AcceptThread mAcceptThread;
    private Button btnNext;
    private ListView lstPlayers;
//    private Boolean booUpdateList = false;
    private String deviceName;
    private ArrayList arrMembers = new ArrayList();
    private ArrayAdapter<String> listAdapter;

    private ArrayList<ClientData> arrayClientDataList = new ArrayList<ClientData>();
    private ArrayList arrayClientSocketList = new ArrayList();
//    private ClientData clientData = new ClientData();

    /**
     * 1. 取得藍芽服務 - onCreate
     * 2. 開啟藍芽 - onStart
     * 3. 設定為可被搜尋2分鐘 - setDiscoveralbe
     * 4. 啟動AcceptThread - onResume
     * 5. 關閉AcceptThread - onPause
     * <p>
     * 備註：由於每個AcceptThread僅能接受一個請求
     * 所以假如要多個
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server1_checking_members);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        lstPlayers = (ListView) findViewById(R.id.lst_members);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrMembers);
        lstPlayers.setAdapter(listAdapter);

        // For checking device support Bluetooth or not
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            Log.i(TAG, "device does not support Bluetooth");
        }
    }

    private void updateMemberList(String newMember) {
//        ArrayList arrlstMembers = new ArrayList();
//        for (int i = 0; i < arrMembers.size(); i++) {
//            arrlstMembers.add(arrMembers.get(i));
//        }
//        arrlstMembers.add(newMember);
//        arrMembers.removeAll(arrMembers);
//        for (int i = 0; i < arrlstMembers.size(); i++) {
//            arrMembers.add(arrlstMembers.get(i).toString());
//            Log.i("1111", arrMembers.get(i).toString());
//        }
        arrMembers.add(newMember);
        listAdapter.notifyDataSetChanged();
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
        mAcceptThread = new Server1CheckingMembers.AcceptThread();
        mAcceptThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
//                view.append("Enabled BT\n");

                setDiscoveralbe();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                mAcceptThread.cancel();
                Intent intent = new Intent(Server1CheckingMembers.this, Server2ChoosingModel.class);
                Log.i("1111", arrayClientDataList.size()+"");
//                intent.putExtra("CLIENT_DATA", clientData);
//                intent.putExtra("CLIENT_DATA", arrayClientDataList);
//                intent.putParcelableArrayListExtra("CLIENT_DATA", arrayClientDataList);
//                intent.putExtra("CLIENT_SOCKET", arrayClientSocketList);
                startActivity(intent);
                break;
//            case R.id.btn:
//                Log.i("1111", "onClick()");
//                ConnectedThread server;
//                String name, address, s;
//                for (int i = 0; i < arrayClientDataList.size(); i++) {
//                    server = (ConnectedThread) (((HashMap) arrayClientDataList.get(i)).get("SOCKET"));
//                    name = (((HashMap) arrayClientDataList.get(i)).get("NAME")).toString();
//                    address = (((HashMap) arrayClientDataList.get(i)).get("ADDRESS")).toString();
//                    s = i + " | " + name + " | " + address;
//                    server.write(s.getBytes());
//                }
//                break;
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
            Log.i("1111", "AcceptThread run");
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
                        mAcceptThread = new Server1CheckingMembers.AcceptThread();
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
        server.start();
        server.write("Waiting for being assigned...".getBytes());

        ClientData clientData = new ClientData();
        clientData.setClientName(socket.getRemoteDevice().getName());
        clientData.setClinetAddress(socket.getRemoteDevice().getAddress());
//        clientData.setSocket(server);
        clientData.setSocket(socket);
//        HashMap map = new HashMap();
////        map.put("SOCKET", server);
//        map.put("ADDRESS", socket.getRemoteDevice().getAddress());
//        map.put("NAME", socket.getRemoteDevice().getName());
//        arrayClientDataList.add(map);
//        arrayClientSocketList.add(server);
        arrayClientDataList.add(clientData);
//        booUpdateList = true;
        deviceName = socket.getRemoteDevice().getName();
    }

    int x = 0;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("1111", "Handler");
            x++;
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    view.append(readMessage);
//                    Log.i("1111", deviceName);
                    updateMemberList(deviceName);
//                    booUpdateList = false;
            }
        }
    };

    private void setDiscoveralbe() {
//        view.append("set BT as being discoverable during 2 minutes\n");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120); // 2 minutes
        startActivity(discoverableIntent);
    }
}