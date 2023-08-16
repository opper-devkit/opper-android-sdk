package com.opper.demo;

import static com.opper.demo.misc.Constants.BLE_DISCONNECT_AFTER_FIRMWARE_UPDATE;
import static com.opper.demo.misc.Constants.BLE_DISCONNECT_ERR;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.rutvijkumarshah.debounce.Debouncer;
import com.omofresh.oppersdk.BleOpperManager;
import com.omofresh.oppersdk.acty.ActivityLauncher;
import com.omofresh.oppersdk.acty.BaseActivity;
import com.omofresh.oppersdk.dto.Hardware;
import com.omofresh.oppersdk.dto.OpperDevice;
import com.omofresh.oppersdk.helper.OpperHelper;
import com.omofresh.oppersdk.misc.Att;
import com.omofresh.oppersdk.misc.AttCommandHelper;
import com.omofresh.oppersdk.misc.SysUnits;
import com.omofresh.oppersdk.ui.DialogUtil;
import com.omofresh.oppersdk.ui.Ui;
import com.omofresh.oppersdk.util.StringUtil;
import com.opper.demo.misc.Constants;
import com.opper.demo.misc.OpCodes;
import com.opper.demo.utils.AssetsUtils;
import com.opper.demo.utils.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

import cn.wch.blelib.utils.LogUtil;

public class OpperActivity extends BaseActivity implements ActivityLauncher.OnActivityResult,
        OpperHelper.OnBleListener, OpperHelper.OnFirmwareListener {
    private static final String TAG = "BleActivity";

    private String mac;
    private TextView tvFilePath;
    private File targetFile;
    private Button btnFlush;
    private ProgressBar progressBar;
    private TextView tvModel;
    private TextView tvSn;
    private TextView tvFirmwareRevision;
    private TextView tvHardwareRevision;
    private TextView tvSoftwareRevision;
    private TextView tvWeight;
    private TextView tvUnit;
    private TextView tvBattery;
    private SeekBar idleBar;
    private TextView tvIdle;
    private SeekBar accuracyBar;
    private TextView tvAccuracy;
    private SeekBar vibrateGramsBar;
    private TextView tvVibrateGrams;
    private volatile boolean afterFirmwareUpgrade = false;
    private final FileUtils fileUtils = new FileUtils(this);
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opper_layout);
        OpperHelper.setOnBleListener(this);
        OpperHelper.setOnFirmwareListener(this);
        handler = new Handler(getMainLooper());
        Button btnRef0 = findViewById(R.id.btnRef0);
        btnRef0.setOnClickListener(v -> {
            AttCommandHelper.send(Att.ref0((reqId, success) -> {
                if (success) {
                    showToast("已归零");
                } else {
                    showToast("归零失败");
                }
            }));
        });
        TextView tvAdjustWeight = findViewById(R.id.tvAdjustWeight);
        Button btnRef1 = findViewById(R.id.btnRef1);
        btnRef1.setOnClickListener(v -> {
            if (tvAdjustWeight.getText() != null) {
                String input = tvAdjustWeight.getText().toString();
                try {
                    double val = Double.parseDouble(input);
                    // 砝码重量转换为克
                    AttCommandHelper.send(Att.ref1(Double.valueOf(val).intValue(), (reqId, success) -> {
                        if (success) {
                            showToast("校准成功");
                        } else {
                            showToast("校准失败");
                        }
                    }));
                } catch (Exception e) {
                    Log.e(TAG, "adjust err", e);
                    showToast("请输入有效重量");
                }
            } else {
                showToast("请输入有效重量");
            }
        });
        idleBar = findViewById(R.id.idleBar);
        accuracyBar = findViewById(R.id.accuracyBar);
        vibrateGramsBar = findViewById(R.id.vibrateGramsBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            idleBar.setMin(1);
            accuracyBar.setMin(1);
        }
        idleBar.setMax(30);
        accuracyBar.setMax(100);
        vibrateGramsBar.setMax(4000);
        tvIdle = findViewById(R.id.tvIdle);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvVibrateGrams = findViewById(R.id.tvVibrateGrams);
        idleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (setSeekBarText(tvIdle, "休眠", progress, "分钟")) {
                    Debouncer.getInstance().debounce("idleBar", () -> {
                        // 设置休眠
                        AttCommandHelper.send(Att.idleMinutes(progress, (reqId, success) -> {
                            if (success) {
                                Log.d(TAG, "休眠时间设置成功: " + progress + "分钟");
                            } else {
                                Log.w(TAG, "休眠时间设置失败: " + progress + "分钟");
                            }
                        }));
                    });
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        accuracyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (setSeekBarText(tvAccuracy, "精度", progress, "克")) {
                    Debouncer.getInstance().debounce("accuracyBar", () -> {
                        // 设置休眠
                        AttCommandHelper.send(Att.accuracy(progress, (reqId, success) -> {
                            if (success) {
                                Log.d(TAG, "精度设置成功: " + progress + "克");
                            } else {
                                Log.w(TAG, "精度设置失败: " + progress + "克");
                            }
                        }));
                    });
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        vibrateGramsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (setSeekBarText(tvVibrateGrams, "振幅", progress, "克")) {
                    Settings.vibrateGrams = progress;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        vibrateGramsBar.setProgress(Settings.vibrateGrams);

        tvBattery = findViewById(R.id.tvBattery);
        tvWeight = findViewById(R.id.tvWeight);
        tvUnit = findViewById(R.id.tvUnit);
        tvUnit.setText(SysUnits.get(Settings.unit).getName());
        tvModel = findViewById(R.id.tvModel);
        tvSn = findViewById(R.id.tvSn);
        tvFirmwareRevision = findViewById(R.id.tvFirmwareRevision);
        tvHardwareRevision = findViewById(R.id.tvHardwareRevision);
        tvSoftwareRevision = findViewById(R.id.tvSoftwareRevision);
        ImageButton btnPickFile = findViewById(R.id.btnPickFile);
        tvFilePath = findViewById(R.id.tvFilePath);
        btnFlush = findViewById(R.id.btnFlush);
        btnFlush.setOnClickListener(v -> {
            if (btnFlush.getText().equals("更新固件")) {
                if (targetFile == null || !targetFile.exists()) {
                    showToast("请选择HEX升级文件");
                    return;
                }
                btnFlush.setText("准备更新");
                doUpgrade();
            } else {
                btnFlush.setText("更新固件");
                BleOpperManager.getInstance().cancel();
            }
        });
        progressBar = findViewById(R.id.progressBar);
        btnPickFile.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                pickFileUnder();
            } else {
                pickFile();
            }
        });
        if (getIntent() != null) {
            mac = getIntent().getStringExtra(Constants.ADDRESS);
        }
        if (mac == null) {
            LogUtil.d("mac address is null");
            finish();
        }
        OpperHelper.connect(mac);
        copyUpgradeHex();
    }

    /**
     * 将 hex 升级文件复制到测试路径上，用于测试固件升级
     */
    private void copyUpgradeHex() {
        AssetManager assetManager = getAssets();
        String path = Environment.getExternalStorageDirectory() + File.separator + "opper" + File.separator + "upgrade";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        AssetsUtils.writeBytesToFile(AssetsUtils.getBytes(assetManager, "upgrade.hex"), path + File.separator + "upgrade.hex");
    }

    private boolean setSeekBarText(TextView view, String label, int value, String unit) {
        if (!view.getText().toString().contains(String.valueOf(value))) {
            view.setText(String.format("%s(%s %s)：", label, value, unit));
            return true;
        }
        return false;
    }

    public void doUpgrade(){
        if (!targetFile.exists()) {
            return;
        }
        OpperHelper.doUpgrade(targetFile);
    }

    private void pickFile() {
        Intent addAttachment = new Intent(Intent.ACTION_GET_CONTENT);
        addAttachment.setType("*/*");
        addAttachment.setAction(Intent.ACTION_GET_CONTENT);
        addAttachment.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(addAttachment, OpCodes.REQ_PICK_FILE, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void pickFileUnder() {
        StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        //String startDir = "Android";
        //String startDir = "Download"; // Not choosable on an Android 11 device
        //String startDir = "DCIM";
        //String startDir = "DCIM/Camera";  // replace "/", "%2F"
        //String startDir = "DCIM%2FCamera";
        String startDir = "opper%2Fupgrade";
        Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
        String scheme = uri.toString();
        Log.d(TAG, "INITIAL_URI scheme: " + scheme);
        scheme = scheme.replace("/root/", "/document/");
        scheme += "%3A" + startDir;
        uri = Uri.parse(scheme);
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        Log.d(TAG, "uri: " + uri.toString());
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, OpCodes.REQ_PICK_FILE, this);
    }

    private void refreshHardwareInfo(Hardware o) {
        runOnUiThread(() -> {
            tvModel.setText(o.getModelNumber());
            tvSn.setText(o.getSerialNumber());
            tvFirmwareRevision.setText(o.getFirmwareRevision());
            tvHardwareRevision.setText(o.getHardwareRevision());
            tvSoftwareRevision.setText(o.getSoftwareRevision());
            int vol = o.getVol().intValue();
            if (vol < 20) {
                Log.d(TAG, "电量低");
            }
            tvBattery.setText(String.format("%s%%", o.getVol().intValue()));
            if (o.getCharging()) {
                tvBattery.setBackgroundResource(R.drawable.ic_battery_charging);
            } else {
                tvBattery.setBackgroundResource(R.drawable.ic_battery_empty);
            }
            setSeekBarText(tvIdle,"休眠", o.getIdleMinutes(),"分钟");
            idleBar.setProgress(o.getIdleMinutes());
            setSeekBarText(tvAccuracy,"精度", o.getAccuracy(),"克");
            accuracyBar.setProgress(o.getAccuracy());
        });
    }

    private void clearInfo() {
        tvModel.setText("");
        tvSn.setText("");
        tvFirmwareRevision.setText("");
        tvHardwareRevision.setText("");
        tvSoftwareRevision.setText("");
        tvWeight.setText("0");
    }

    private final DecimalFormat[] DECIMAL_FORMATS = new DecimalFormat[]{
            new DecimalFormat("0"),
            new DecimalFormat("0.0#"),
            new DecimalFormat("0.00#"),
            new DecimalFormat("0.000#"),
    };

    private void showToast(String msg) {
        runOnUiThread(() -> Ui.showToast(OpperActivity.this, msg));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Ui.fullscreen(getWindow());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OpperHelper.disconnect();
    }

    private void setFilePath(String path) {
        if (StringUtil.isNullOrEmpty(path)) {
            targetFile = null;
            tvFilePath.setText("未选中文件");
            btnFlush.setEnabled(false);
        } else {
            targetFile = new File(path);
            tvFilePath.setText(path);
            btnFlush.setEnabled(true);
        }
    }

    @Override
    public void onActivityResultCallback(int requestCode, int resultCode, Intent data) {
        if (requestCode == OpCodes.REQ_PICK_FILE && data != null) {
            Uri selectedDocUri = data.getData();
            String path = fileUtils.getPath(selectedDocUri);
            if (!path.endsWith(".hex") && !path.endsWith(".HEX")) {
                setFilePath(null);
                showToast("请选择HEX文件");
            } else {
                setFilePath(path);
            }
        }
    }

    @Override
    public void onBleWeight(double weight, boolean stable) {
//        Log.d(TAG, String.format(Locale.CHINA, "weight: %s stable: %s", weight, stable));
        runOnUiThread(() -> {
            tvWeight.setText(DECIMAL_FORMATS[Settings.decimals].format(weight));
        });
    }

    @Override
    public void onBleConnecting() {
        runOnUiThread(() -> DialogUtil.getInstance().showLoadingDialog(OpperActivity.this, "正在连接"));
    }

    @Override
    public void onBleConnectTimeout() {
        runOnUiThread(() -> {
            Ui.showToast(this, "连接超时");
            DialogUtil.getInstance().hideLoadingDialog();
        });
        handler.postDelayed(this::finish, 1000);
    }

    @Override
    public void onBleConnectError() {
        runOnUiThread(() -> {
            Ui.showToast(this, "连接异常");
            DialogUtil.getInstance().hideLoadingDialog();
        });
        handler.postDelayed(this::finish, 1000);
    }

    @Override
    public void onBleConnected(@Nullable OpperDevice opperDevice) {
        runOnUiThread(() -> {
            DialogUtil.getInstance().hideLoadingDialog();
            invalidateOptionsMenu();
        });
        if (opperDevice != null) {
            LogUtil.d(opperDevice.getAdvertise().getName() + " is connected" + "\r\n");
        } else {
            LogUtil.d(mac + " is connected");
        }
    }

    @Override
    public void onBleDisconnected() {
        runOnUiThread(this::invalidateOptionsMenu);
        LogUtil.d(mac + " is disconnected" + "\r\n");
        if (!afterFirmwareUpgrade) {
            showToast("蓝牙已断开连接");
            handler.postDelayed(() -> {
                clearInfo();
                setResult(BLE_DISCONNECT_ERR);
                finish();
            }, 1000);
        } else {
            afterFirmwareUpgrade = false;
            handler.postDelayed(() -> {
                clearInfo();
                setResult(BLE_DISCONNECT_AFTER_FIRMWARE_UPDATE);
                finish();
            }, 1000);
        }
    }

    @Override
    public void onBleInfo(Hardware hardware) {
        refreshHardwareInfo(hardware);
    }

    @Override
    public void onFirmwareUpdateBegin() {
        btnFlush.setText("取消更新");
    }

    @Override
    public void onFirmwareUpdateStart() {
        showToast("更新固件");
        runOnUiThread(() -> progressBar.setProgress(0));
    }

    @Override
    public void onFirmwareUpdateProgress(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    public void onFirmwareVerifyStart() {
        showToast("校验更新");
        runOnUiThread(() -> progressBar.setProgress(0));
    }

    @Override
    public void onFirmwareVerifyProgress(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    public void onFirmwareUpdateComplete() {
        showToast("更新成功");
        progressBar.setProgress(0);
        btnFlush.setText("更新固件");
        afterFirmwareUpgrade = true;
    }

    @Override
    public void onFirmwareUpdateError() {
        showToast("更新异常");
        // 重置更新状态
        progressBar.setProgress(0);
        OpperHelper.disconnect();
        // 延迟重新连接
        handler.postDelayed(() -> OpperHelper.connect(mac), 1000);
    }
}
