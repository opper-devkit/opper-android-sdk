package com.opper.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.omofresh.oppersdk.acty.BaseActivity;
import com.omofresh.oppersdk.misc.StoragePermissionHelper;
import com.opper.demo.misc.OpCodes;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Button btnOpper = findViewById(R.id.btnOpper);
        btnOpper.setOnClickListener(v -> {
            if (StoragePermissionHelper.verifyStoragePermissions(this)) {
                Intent i = new Intent(this, BleScanActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        if (!StoragePermissionHelper.verifyStoragePermissions(this)) {
            StoragePermissionHelper.requestStoragePermission(this, OpCodes.REQ_STORAGE);
        }
    }
}
