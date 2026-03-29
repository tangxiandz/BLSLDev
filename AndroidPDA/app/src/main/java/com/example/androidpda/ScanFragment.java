package com.example.androidpda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ScanFragment extends Fragment {

    private Button btnScan;
    private TextView tvScanResult;

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        btnScan = view.findViewById(R.id.btn_scan);
        tvScanResult = view.findViewById(R.id.tv_scan_result);

        // 设置扫码按钮点击事件
        btnScan.setOnClickListener(v -> scanCode());

        return view;
    }

    private void scanCode() {
        // 模拟扫码操作
        // 实际项目中这里会调用扫码API或启动扫码Activity
        tvScanResult.setText(getString(R.string.text_scan_success) + "：RM001 - A物料");
        tvScanResult.setVisibility(View.VISIBLE);
    }
}
