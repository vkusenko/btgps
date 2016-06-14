package com.vkusenko.btgpslogger.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.vkusenko.btgpslogger.R;

import java.io.File;

public class FileManager {
    private Activity activity;
    private SharedPreferences sharedPreferences;

    public FileManager(Activity activity) {
        this.activity = activity;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public String getPath() {
        String path;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory() +
                    File.separator +
                    sharedPreferences.getString("folder_name", activity.getString(R.string.folder_name_default));
            File folder = new File(path);
            if (!folder.exists()) {
                if (!folder.mkdir())
                    path = null;
            }
        } else {
            path = null;
        }

        return path;
    }

    public File getDir() {
        return new File(getPath());
    }
}
