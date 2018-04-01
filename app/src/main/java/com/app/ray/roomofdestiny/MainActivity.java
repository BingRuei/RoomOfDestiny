package com.app.ray.roomofdestiny;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    /**
     * Deciding the device is server or client
     * **/

    private Button btnServer, btnClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnServer = (Button) findViewById(R.id.btn_server);
        btnClient = (Button) findViewById(R.id.btn_client);
        btnServer.setOnClickListener(this);
        btnClient.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.btn_server:
//                intent = new Intent(MainActivity.this, Server1CheckingMembers.class);
                intent = new Intent(MainActivity.this, Server1CheckingMembers.class);
                break;
            case R.id.btn_client:
                intent = new Intent(MainActivity.this, Client2Result.class);
                break;
        }
        startActivity(intent);
    }
}