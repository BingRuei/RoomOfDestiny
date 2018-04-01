package com.app.ray.roomofdestiny;

import android.bluetooth.BluetoothSocket;

import java.io.Serializable;

/**
 * Created by ray on 2017/9/12.
 */

public class ClientData implements Serializable {

    private String ClinetAddress;
    private String ClientName;
    private BluetoothSocket Socket;

    public void setClientName(String clientName) {
        ClientName = clientName;
    }

    public void setClinetAddress(String clinetAddress) {
        ClinetAddress = clinetAddress;
    }

    public void setSocket(BluetoothSocket socket) {
        Socket = socket;
    }

    public String getClinetAddress() {
        return ClinetAddress;
    }

    public String getClientName() {
        return ClientName;
    }

    public BluetoothSocket getSocket() {
        return Socket;
    }
}
