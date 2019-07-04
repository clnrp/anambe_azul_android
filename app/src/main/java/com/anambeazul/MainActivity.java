package com.anambeazul;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    private TextView TextViewThrottle;
    private TextView TextViewInfo;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList list = new ArrayList();
    private BluetoothSocket mSocket = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private String str;
    private static Integer throttle, last_throttle;
    private Bluetooth bluetooth;
    private Boolean thBl_r,thBl_s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        throttle = 0;

        bluetooth = new Bluetooth(getApplicationContext());

        TextViewThrottle = (TextView) findViewById(R.id.textViewThrottle);
        TextViewInfo = (TextView) findViewById(R.id.textViewInfo);

        final JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickView_left);
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                int movY = 50-joystickLeft.getNormalizedY();
                int dy = Math.abs((int)(0.2 * (float)movY));
                if(movY > 0){
                    if(throttle + dy <= 500) {
                        throttle += dy;
                    }else{
                        throttle = 500;
                    }
                }else{
                    if(throttle - dy >= 0) {
                        throttle -= dy;
                    }else{
                        throttle = 0;
                    }
                }
                TextViewThrottle.setText(String.valueOf(throttle));
            }
        });

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

            }
        });

        final Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonConnect.getText().equals("CONECTAR")){
                    thBl_r = true;
                    thBl_s = true;
                    String address = bluetooth.connect(false, "VRFisioterapia");
                    if (address.length() > 0) {
                        buttonConnect.setText("DESCONECTAR");
                        Runnable runnableSend = new Runnable() {
                            public void run() {
                                while (thBl_r) {
                                    try {
                                        Thread.sleep(100);
                                        str = bluetooth.readInputStream();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TextViewInfo.setText(str);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };

                        Runnable runnableReceive = new Runnable() {
                            public void run() {
                                while (thBl_s) {
                                    try {
                                        Thread.sleep(200);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (throttle != last_throttle) {
                                                    TextViewThrottle.setText(String.valueOf(throttle));
                                                    last_throttle = throttle;
                                                    String protocol = "@02;" + String.valueOf(throttle) + "!\r\n";
                                                    bluetooth.send(protocol);
                                                }
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };

                        Thread threadSend = new Thread(runnableSend);
                        threadSend.start();

                        Thread threadReceive = new Thread(runnableReceive);
                        threadReceive.start();
                    }
                }else{
                    thBl_r = false;
                    thBl_s = false;
                    buttonConnect.setText("CONECTAR");
                    bluetooth.Desconnect();
                }
            }
        });

    }


}
