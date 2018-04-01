package com.app.ray.roomofdestiny;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class Server4Assigning extends Activity implements View.OnClickListener {

    /**
     * Loading data from model that you selected it,
     * and you can filled some data out that you need,
     * the end of the day, clicking the button 'Assigning',
     * and then the client will get your item by random.
     **/
    public static final int MESSAGE_READ = 1;

    private Bundle bundle;
    private ArrayList arrayClientDataList, arrayClientSocketList, arrayServer2Client;
    private EditText edtName;
    private Button btnSave, btnAddItem, btnAssign;
    private LinearLayout layItem;

    private int index = 0;
    private ArrayList arrayItems, arrayExist;
//    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server4_assigning);

        bundle = getIntent().getExtras();
        arrayClientDataList = (ArrayList) bundle.get("CLIENT_DATA");
//        arrayClientSocketList = (ArrayList) bundle.get("CLIENT_SOCKET");
        arrayServer2Client = new ArrayList();

        for(int i=0;i<arrayClientDataList.size();i++){
            manageConnectedSocket(((ClientData)arrayClientSocketList.get(i)).getSocket());
        }

        arrayItems = new ArrayList();
        arrayExist = new ArrayList();
//        settings = getSharedPreferences("Preference", 0);

        edtName = (EditText) findViewById(R.id.edt_name);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnAddItem = (Button) findViewById(R.id.btn_add_item);
        btnAssign = (Button) findViewById(R.id.btn_assign);
        layItem = (LinearLayout) findViewById(R.id.layout_add_item);

        btnSave.setOnClickListener(this);
        btnAddItem.setOnClickListener(this);
        btnAssign.setOnClickListener(this);

    }

    public void manageConnectedSocket(BluetoothSocket socket) {
        ConnectedThread server = new ConnectedThread(mHandler, socket);
        server.start();
        arrayClientDataList.add(server);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                break;
            case R.id.btn_add_item:
                addItem(0, false);
                break;
            case R.id.btn_assign:
                ArrayList arrayRandomItem = randomItem();
                assignItem(arrayRandomItem);
                break;
        }
    }

    private void assignItem(ArrayList arrayRandomItem) {
        if (arrayRandomItem.size() == arrayClientDataList.size()) {
            for (int i = 0; i < arrayRandomItem.size(); i++) {
                String item = arrayRandomItem.get(i).toString();
//                ((ConnectedThread)arrayClientSocketList.get(i)).write(item.getBytes());
                ((ConnectedThread)arrayServer2Client.get(i)).write(item.getBytes());
//                ((ConnectedThread) ((HashMap) arrayClientDataList.get(i)).get("SOCKET")).write(item.getBytes());
                Log.i("1111", item);
            }
            Log.i("1111", "-");
        } else {
            Log.i("1111", "項目數量與參與玩家人數不相等");
        }
    }

    private ArrayList randomItem() {
        ArrayList arrayAllItem = new ArrayList();
        EditText edtTempItem, edtTempAmount;
        for (int i = 0; i < arrayItems.size(); i++) {
            if ((boolean) arrayExist.get(i)) {
                edtTempItem = (EditText) (((HashMap) arrayItems.get(i)).get("ITEM"));
                edtTempAmount = (EditText) (((HashMap) arrayItems.get(i)).get("AMOUNT"));
//                Log.i("1111", edtTempItem.getText().toString() + ":"+ edtTempAmount.getText().toString());
                String item = edtTempItem.getText().toString();
                int amount = Integer.parseInt(edtTempAmount.getText().toString());
                for (int j = 0; j < amount; j++) {
                    HashMap map = new HashMap();
                    map.put("ITEM", item);
                    map.put("UNSELECTED", true);
                    arrayAllItem.add(map);
                }
            }
        }

        ArrayList arrayRandomItem = new ArrayList();
        for (int i = 0; i < arrayAllItem.size(); i++) {
            int randomNum = (int) (Math.random() * arrayAllItem.size());
            if ((boolean) ((HashMap) arrayAllItem.get(randomNum)).get("UNSELECTED")) {
                arrayRandomItem.add(((HashMap) arrayAllItem.get(randomNum)).get("ITEM").toString());
                ((HashMap) arrayAllItem.get(randomNum)).put("UNSELECTED", false);
            } else {
                i--;
            }
        }

        return arrayRandomItem;
    }

    private void addItem(int initPos, boolean initStatus) {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_server4_assigning_item, null);
        EditText edtItem = (EditText) view.findViewById(R.id.edt_item_name);
        EditText edtAmount = (EditText) view.findViewById(R.id.edt_item_amount);

        /**************** init ****************/
//        if (initStatus) {
//            edtItem.setText(settings.getString("ITEM_NAME" + initPos, ""));
//            edtAmount.setText(settings.getString("ITEM_AMOUNT" + initPos, ""));
//        }
        /**************** init ****************/

//        RelativeLayout btnDelete = (RelativeLayout) view.findViewById(R.id.btn_delete);
        Button btnDelete = (Button) view.findViewById(R.id.btn_delete);
        btnDelete.setTag(R.id.btn, view);
        btnDelete.setTag(R.id.btn_add_item, index);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = Integer.parseInt(v.getTag(R.id.btn_add_item) + "");
                layItem.removeView((View) v.getTag(R.id.btn));
                ((EditText) ((HashMap) arrayItems.get(index)).get("ITEM")).setText("");
                ((EditText) ((HashMap) arrayItems.get(index)).get("AMOUNT")).setText("");
                arrayExist.set(index, false);
            }
        });
        layItem.addView(view);
        HashMap map = new HashMap();
        map.put("ITEM", edtItem);
        map.put("AMOUNT", edtAmount);
        arrayItems.add(map);
        arrayExist.add(true);
        index++;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
            }
        }
    };
}
