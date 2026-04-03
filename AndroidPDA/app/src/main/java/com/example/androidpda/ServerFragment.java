package com.example.androidpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ServerFragment extends Fragment {

    private EditText etServerUrl;
    private EditText etPlcIp;
    private EditText etPlcPort;
    private EditText etPlcProtocol;
    private TextView tvResult;
    private Button btnSave;
    private Button btnReset;
    private Button btnConnectPlc;

    // 存储服务器地址的SharedPreferences名称
    private static final String PREF_NAME = "ServerConfig";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_PLC_IP = "plc_ip";
    private static final String KEY_PLC_PORT = "plc_port";
    private static final String KEY_PLC_PROTOCOL = "plc_protocol";

    public ServerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_server, container, false);

        etServerUrl = view.findViewById(R.id.et_server_url);
        etPlcIp = view.findViewById(R.id.et_plc_ip);
        etPlcPort = view.findViewById(R.id.et_plc_port);
        etPlcProtocol = view.findViewById(R.id.et_plc_protocol);
        tvResult = view.findViewById(R.id.tv_result);
        btnSave = view.findViewById(R.id.btn_save);
        btnReset = view.findViewById(R.id.btn_reset);
        btnConnectPlc = view.findViewById(R.id.btn_connect_plc);

        // 加载保存的配置
        loadConfig();

        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> saveConfig());

        // 设置重置按钮点击事件
        btnReset.setOnClickListener(v -> resetConfig());

        // 设置PLC连接按钮点击事件
        btnConnectPlc.setOnClickListener(v -> {
            // 先保存配置，然后连接PLC
            saveConfig();
            PlcStatusManager.getInstance(getContext()).connectPlc();
        });

        return view;
    }

    // 加载保存的配置
    private void loadConfig() {
        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String serverUrl = preferences.getString(KEY_SERVER_URL, ApiConfig.BASE_URL);
        String plcIp = preferences.getString(KEY_PLC_IP, "192.168.1.100");
        String plcPort = preferences.getString(KEY_PLC_PORT, "8888");
        String plcProtocol = preferences.getString(KEY_PLC_PROTOCOL, "TCP/IP");
        
        etServerUrl.setText(serverUrl);
        etPlcIp.setText(plcIp);
        etPlcPort.setText(plcPort);
        etPlcProtocol.setText(plcProtocol);
    }

    // 保存配置
    private void saveConfig() {
        String serverUrl = etServerUrl.getText().toString().trim();
        String plcIp = etPlcIp.getText().toString().trim();
        String plcPort = etPlcPort.getText().toString().trim();
        String plcProtocol = etPlcProtocol.getText().toString().trim();

        if (serverUrl.isEmpty()) {
            showResult("请输入服务器地址", false);
            return;
        }

        if (plcIp.isEmpty()) {
            showResult("请输入PLC IP地址", false);
            return;
        }

        if (plcPort.isEmpty()) {
            showResult("请输入PLC端口", false);
            return;
        }

        // 保存到SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        editor.putString(KEY_PLC_IP, plcIp);
        editor.putString(KEY_PLC_PORT, plcPort);
        editor.putString(KEY_PLC_PROTOCOL, plcProtocol);
        editor.apply();

        showResult("配置保存成功", true);
        Toast.makeText(getContext(), "配置保存成功，需要重启应用才能生效", Toast.LENGTH_LONG).show();
    }

    // 重置配置为默认值
    private void resetConfig() {
        etServerUrl.setText(ApiConfig.BASE_URL);
        etPlcIp.setText("192.168.1.100");
        etPlcPort.setText("8888");
        etPlcProtocol.setText("TCP/IP");
        showResult("配置已重置为默认值", true);
    }

    // 显示结果信息
    private void showResult(String message, boolean isSuccess) {
        tvResult.setText(message);
        tvResult.setTextColor(getResources().getColor(isSuccess ? R.color.success : R.color.danger));
        tvResult.setVisibility(View.VISIBLE);
    }

    // 从SharedPreferences获取服务器地址
    public static String getServerUrl(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_SERVER_URL, ApiConfig.BASE_URL);
    }
    
    // 从SharedPreferences获取PLC IP地址
    public static String getPlcIp(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PLC_IP, "192.168.1.100");
    }
    
    // 从SharedPreferences获取PLC端口
    public static int getPlcPort(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String portStr = preferences.getString(KEY_PLC_PORT, "8888");
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return 8888;
        }
    }
    
    // 从SharedPreferences获取PLC通信协议
    public static String getPlcProtocol(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PLC_PROTOCOL, "TCP/IP");
    }
}
