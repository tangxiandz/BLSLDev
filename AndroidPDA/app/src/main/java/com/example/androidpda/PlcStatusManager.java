package com.example.androidpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class PlcStatusManager {

    private static final String PREF_NAME = "PlcStatus";
    private static final String KEY_PLC_CONNECTED = "plc_connected";
    private static PlcStatusManager instance;
    private boolean isPlcConnected = false;
    private Context context;

    private PlcStatusManager(Context context) {
        this.context = context.getApplicationContext();
        loadStatus();
    }

    public static synchronized PlcStatusManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlcStatusManager(context);
        }
        return instance;
    }

    // 加载PLC连接状态
    private void loadStatus() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isPlcConnected = preferences.getBoolean(KEY_PLC_CONNECTED, false);
    }

    // 保存PLC连接状态
    private void saveStatus() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_PLC_CONNECTED, isPlcConnected);
        editor.apply();
    }

    // 检查PLC连接状态
    public void checkPlcConnection() {
        new Thread(() -> {
            boolean connected = false;
            try {
                String plcIp = ServerFragment.getPlcIp(context);
                int plcPort = ServerFragment.getPlcPort(context);

                if (!plcIp.isEmpty() && plcPort != 0) {
                    // 尝试连接PLC，设置10秒超时
                    Socket socket = new Socket();
                    socket.connect(new java.net.InetSocketAddress(plcIp, plcPort), 10000);
                    socket.close();
                    connected = true;
                }
            } catch (IOException e) {
                connected = false;
            }

            final boolean finalConnected = connected;
            new Handler(Looper.getMainLooper()).post(() -> {
                isPlcConnected = finalConnected;
                saveStatus();
                String message = finalConnected ? "PLC连接成功" : "PLC连接失败，将以离线模式运行";
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // 手动连接PLC
    public void connectPlc() {
        checkPlcConnection();
    }

    // 获取PLC连接状态
    public boolean isPlcConnected() {
        return isPlcConnected;
    }

    // 设置PLC连接状态
    public void setPlcConnected(boolean connected) {
        isPlcConnected = connected;
        saveStatus();
    }
}
