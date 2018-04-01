package com.app.ray.roomofdestiny;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class Server2ChoosingModel extends Activity implements View.OnClickListener {

    /**
     * Choosing which of models,
     * and conveying the sockets from last page to next page
     **/
    private Bundle bundle;
    private ArrayList<ClientData> arrayClientDataList;//, arrayClientSocketList;
    private Button btnNewModel, btnModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server2_choosing_model);

//        bundle = getIntent().getExtras();
//        arrayClientDataList = (ArrayList) bundle.getParcelableArrayList("CLIENT_DATA");
//        arrayClientDataList = (ArrayList) getIntent().getSerializableExtra("CLIENT_DATA");
//        arrayClientDataList = (ArrayList) bundle.get("CLIENT_DATA");
//        arrayClientSocketList = (ArrayList) bundle.get("CLIENT_SOCKET");

        btnNewModel = (Button) findViewById(R.id.btn_new_model);
        btnModelList = (Button) findViewById(R.id.btn_model_list);
        btnNewModel.setOnClickListener(this);
        btnModelList.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_new_model:
                intent = new Intent(Server2ChoosingModel.this, Server4Assigning.class);
                break;
            case R.id.btn_model_list:
                intent = new Intent(Server2ChoosingModel.this, Server3ModelList.class);
                break;
        }
        intent.putExtra("CLIENT_DATA", arrayClientDataList);
//        intent.putExtra("CLIENT_SOCKET", arrayClientSocketList);
        startActivity(intent);
    }
}
