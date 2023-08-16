package com.opper.demo.utils;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class AssetsUtils {
    private AssetsUtils() {
    }

    private static final String TAG = "AssetsUtil";

    public static byte[] getBytes(AssetManager assetManager, String source) {
        try {
            InputStream is = assetManager.open(source);
            byte[] fileBytes = new byte[is.available()];
            is.read(fileBytes);
            is.close();
            return fileBytes;
        } catch (IOException e) {
            Log.e(TAG, "io err", e);
        }
        return null;
    }

    public static boolean writeBytesToFile(byte[] data, String path) {
        File file = new File(path);
        if (!file.exists()) {
            FileLock lock = null;
            try {
                if (file.createNewFile()) {
                    RandomAccessFile rFile = new RandomAccessFile(file,"rw");
                    FileChannel fc = rFile.getChannel();
                    lock = fc.lock(0, Long.MAX_VALUE, true);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    return true;
                } else {
                    Log.e("FileUtil", "创建文件失败: " + path);
                }
            } catch (IOException e) {
                Log.e(TAG, "io err", e);
            } finally {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // 先删除再重复写入
            if (file.delete()) {
                return writeBytesToFile(data, path);
            } else {
                Log.i("FileUtil", "文件已存在: " + path);
            }
        }
        return false;
    }

}
