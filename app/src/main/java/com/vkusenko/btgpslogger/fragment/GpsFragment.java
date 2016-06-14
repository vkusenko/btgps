package com.vkusenko.btgpslogger.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vkusenko.btgpslogger.R;
import com.vkusenko.btgpslogger.events.MockProviderEvent;
import com.vkusenko.btgpslogger.service.BtGpsService;
import com.vkusenko.btgpslogger.common.CurrentLocation;
import com.vkusenko.btgpslogger.util.gps.MockLocationProvider;
import com.vkusenko.btgpslogger.events.BtSocketEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GpsFragment extends Fragment {

    private static String macAddress;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean connectionFlag = false;


    private Button btnEnableBT;
    private Button btnDeviceConnect;
    private Spinner spinnerDevices;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice selectedDevice;

    private List<String> listDevices = new ArrayList<String>();
    private  final List<String> listMacAddress = new ArrayList<String>();
    private ArrayAdapter<String> listDevicesAdapter;

    private TextView txtViewDate;
    private TextView txtViewTime;
    private TextView txtViewLat;
    private TextView txtViewLon;
    private TextView txtViewAltitude;
    private TextView txtViewSpeed;
    private TextView txtViewCourse;
    private TextView txtViewSatellites;

    private LinearLayout layoutEnableBT;
    private LinearLayout layoutDeviceConnect;

    private MockLocationProvider mockLocationProvider;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gps_fragment, container, false);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layoutEnableBT = (LinearLayout) getActivity().findViewById(R.id.layoutEnableBT);
        layoutDeviceConnect = (LinearLayout) getActivity().findViewById(R.id.layoutDeviceConnect);

        txtViewDate = (TextView) getActivity().findViewById(R.id.txtViewDate);
        txtViewTime = (TextView) getActivity().findViewById(R.id.txtViewTime);
        txtViewLat = (TextView) getActivity().findViewById(R.id.txtViewLat);
        txtViewLon = (TextView) getActivity().findViewById(R.id.txtViewLon);
        txtViewAltitude = (TextView) getActivity().findViewById(R.id.txtViewAltitude);
        txtViewSpeed = (TextView) getActivity().findViewById(R.id.txtViewSpeed);
        txtViewCourse = (TextView) getActivity().findViewById(R.id.txtViewCourse);
        txtViewSatellites = (TextView) getActivity().findViewById(R.id.txtViewSatellites);

        btnEnableBT = (Button) getActivity().findViewById(R.id.btnEnableBT);
        btnEnableBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBT();
            }
        });

        btnDeviceConnect = (Button) getActivity().findViewById(R.id.btnDeviceConnect);
        btnDeviceConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopGPS();
            }
        });

        spinnerDevices = (Spinner) getActivity().findViewById(R.id.spinnerDevices);
        listDevicesAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, listDevices);
        listDevicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(listDevicesAdapter);
        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                macAddress = listMacAddress.get(position);
                btnDeviceConnect.setEnabled(true);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void startStopGPS() {
        if (!connectionFlag) {
            if (onConnect()) {
                connectionFlag = true;
                mockLocationProvider = new MockLocationProvider(LocationManager.GPS_PROVIDER, getActivity().getApplicationContext());
                EventBus.getDefault().postSticky(new MockProviderEvent(mockLocationProvider));
                getActivity().startService(new Intent(getActivity().getApplicationContext(), BtGpsService.class));
                btnDeviceConnect.setText(getString(R.string.disconnect));
            } else {
                Toast toast =  Toast.makeText(getActivity().getApplicationContext(), getString(R.string.connection_fail), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        } else {
            connectionFlag = false;
            mockLocationProvider.shutdown();
            getActivity().stopService(new Intent(getActivity().getApplicationContext(), BtGpsService.class));
            btnDeviceConnect.setText(getString(R.string.connect));
        }
    }

    private void enableBT() {
        if (!btAdapter.isEnabled()) {
            Intent eIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(eIntent);
        }
    }

    private boolean onConnect() {
        boolean result = false;
        selectedDevice = btAdapter.getRemoteDevice(macAddress);
        try {
            btSocket =  selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            result = false;
        }
        btAdapter.cancelDiscovery();
        try {
            btSocket.connect();
            result = true;
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                result = false;
            }
        }

        EventBus.getDefault().postSticky(new BtSocketEvent(btSocket));
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (connectionFlag) {
            btnDeviceConnect.setText(getString(R.string.disconnect));
        } else {
            btnDeviceConnect.setText(getString(R.string.connect));
        }
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter.isEnabled()) {
            layoutEnableBT.setVisibility(View.GONE);
            layoutDeviceConnect.setVisibility(View.VISIBLE);
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device.getName());
                    listMacAddress.add(device.getAddress());
                }
                listDevicesAdapter.notifyDataSetChanged();
            }
        } else {
            layoutEnableBT.setVisibility(View.VISIBLE);
            layoutDeviceConnect.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCurrentLocation(CurrentLocation currentLocation) {
        txtViewDate.setText(currentLocation.getStrDate());
        txtViewTime.setText(currentLocation.getStrTime());
        txtViewLat.setText(currentLocation.getStrLat());
        txtViewLon.setText(currentLocation.getStrLon());
        txtViewAltitude.setText(currentLocation.getStrAlt());
        txtViewSpeed.setText(currentLocation.getStrSpeed());
        txtViewCourse.setText(currentLocation.getStrCourse());
        txtViewSatellites.setText(currentLocation.getStrSatellites());
    }
}
