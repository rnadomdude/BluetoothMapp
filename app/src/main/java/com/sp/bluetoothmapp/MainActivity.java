package com.sp.bluetoothmapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button bluetoothBtn, refreshBtn;
    private SwitchCompat fertilizerSwitch, waterSwitch;
    private TextView detailDisplay, pairedBluetooth;
    private ProgressDialog progress;
    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private String bluetoothAddress = null;
    private String bluetoothName = "None";
    private String data = "Humidity: \n Soil moisture: ";

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detailDisplay = findViewById(R.id.sensorTv);
        bluetoothBtn = findViewById(R.id.BluetoothButton);
        fertilizerSwitch = findViewById(R.id.FertilizerButton);
        waterSwitch = findViewById(R.id.WaterButton);
        pairedBluetooth = findViewById(R.id.currentlyPaired);
        refreshBtn = findViewById(R.id.refreshButton);

        fertilizerSwitch.setChecked(false);
        waterSwitch.setChecked(false);

        new ConnectBT().execute();

        bluetoothAddress = getIntent().getStringExtra(BluetoothActivity.EXTRA_ADDRESS);
        bluetoothName = getIntent().getStringExtra(BluetoothActivity.DEVICE_NAME);

        pairedBluetooth.setText("Currently paired device: " + bluetoothName);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });

        fertilizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOnFertilizer();
                } else {
                    turnOffFertilizer();
                }
            }
        });

        waterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    turnOnWater();
                } else {
                    turnOffWater();
                }
            }
        });

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });


    }

    private void refreshData() {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        InputStream tmpIn = null;
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("a".getBytes());
                btSocket.getInputStream().read(buffer);
                final String string =new String(buffer,"UTF-8");
                detailDisplay.setText(string);
            }
            catch (IOException e) {
                Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnOnFertilizer() {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("b".getBytes());
                Toast.makeText(MainActivity.this, "Fertilizer pump turned on", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnOffFertilizer() {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("d".getBytes());
                Toast.makeText(MainActivity.this, "Fertilizer pump turned off", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnOnWater() {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("c".getBytes());
                Toast.makeText(MainActivity.this, "Water pump turned on", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnOffWater() {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("e".getBytes());
                Toast.makeText(MainActivity.this, "Water pump turned off", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean SuccessfulConnect = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting", "Please wait");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice checkDevice = btAdapter.getRemoteDevice(bluetoothAddress);
                    btSocket = checkDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e) {
                SuccessfulConnect = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!SuccessfulConnect) {
                Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                pairedBluetooth.setText("Currently paired device: " + bluetoothName + " (Please pair again)");
            }
            else {
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}