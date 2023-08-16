package com.opper.demo;

import static com.opper.demo.misc.Constants.BLE_DISCONNECT_AFTER_FIRMWARE_UPDATE;
import static com.opper.demo.misc.Constants.BLE_DISCONNECT_ERR;

import android.content.Intent;
import android.os.Bundle;

import com.omofresh.oppersdk.acty.BleScanHandyActivity;
import com.omofresh.oppersdk.dto.AdvertiseDevice;
import com.omofresh.oppersdk.dto.FilterBle;
import com.omofresh.oppersdk.dto.OpperAdvertise;
import com.omofresh.oppersdk.dto.OpperDevice;
import com.omofresh.oppersdk.helper.OpperHelper;
import com.opper.demo.misc.Constants;
import com.opper.demo.misc.OpCodes;

public class BleScanActivity extends BleScanHandyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("搜索OPPER设备");
    }

    @Override
    public void onDeviceClick(AdvertiseDevice device) {
        connectOpper(device.getDevice().getAddress());
    }

    @Override
    public FilterBle filterBle() {
        return FilterBle.OPPER;
    }

    private String reconnectMac = null;

    /**
     * 连接 opper
     */
    void connectOpper(String mac) {
        reconnectMac = null;
        stopScan();
        Intent intent = new Intent(this, OpperActivity.class);
        intent.putExtra(Constants.ADDRESS, mac);
        startActivityForResult(intent, OpCodes.REQ_OPPER_CONNECT, (requestCode, resultCode, data) -> {
            if (resultCode == BLE_DISCONNECT_ERR) {
                reconnectMac = null;
                startScan();
            } else if (resultCode == BLE_DISCONNECT_AFTER_FIRMWARE_UPDATE) {
                reconnectMac = mac;
                startScan();
            }
        });
    }

    @Override
    public void onScan(AdvertiseDevice device, int rssi) {
        super.onScan(device, rssi);
        OpperHelper.addDevice(new OpperDevice(device.getDevice(), (OpperAdvertise) device.getAdvertise()));
        if (reconnectMac != null && reconnectMac.equalsIgnoreCase(device.getDevice().getAddress())) {
            handler.postDelayed(() -> connectOpper(device.getDevice().getAddress()), 300);
        }
    }

}
