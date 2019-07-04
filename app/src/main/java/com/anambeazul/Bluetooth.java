package com.anambeazul;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

public class Bluetooth {
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothSocket mSocket = null;
    private String inputData="";
    private String address=null;
    private PrintWriter out;
    private BufferedReader in;
    ArrayList list = new ArrayList();
    Context context;

    public Bluetooth(Context context) {
        this.context=context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public String connect(boolean thread, String name){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluetoothAdapter.getBondedDevices();

        for(BluetoothDevice bt: pairedDevices) {
            if(bt.getName().equals(name)) {
                address = bt.getAddress();
                break;
            }
        }

        if(address != null){
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

            Method method;
            try {
                method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                mSocket = (BluetoothSocket) method.invoke(device, 1);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            bluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException e) {
                try {
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e("", "unable to close() socket during connection failure", e2);
                }
                return "";
            }

            try {
                in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
            } catch (IOException e) {
                Log.e("", "Sockets not created", e);
            }

            if(thread){
                Runnable runnable = new Runnable() {
                    int bytes;
                    public void run() {
                        while(true){
                            synchronized (this) {
                                try {
                                    Thread.sleep(100);
                                    //byte[] buffer = new byte[1024];
                                    //bytes = inStream.read(buffer);
                                    //String str = new String(buffer);
                                    //inputData+=str;
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                };
                Thread pthread = new Thread(runnable);
                pthread.start();
            }
        }
        return address;
    }

    public void send(String text){
        try {
            out.write(text);
            out.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String read(){
        String aux=inputData;
        inputData="";
        return aux;
    }

    public String readInputStream(){
        String str="";
        try {
            str=in.readLine();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return str;
    }

    public boolean isConnect(){
        boolean state=false;
        if(mSocket!=null){
            state=mSocket.isConnected();
        }
        return state;
    }

    public void Desconnect(){
        if(mSocket.isConnected()){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}