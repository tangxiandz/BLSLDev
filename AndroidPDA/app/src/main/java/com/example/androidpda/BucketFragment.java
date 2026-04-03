package com.example.androidpda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class BucketFragment extends Fragment {

    private EditText etBucketCode;
    private Button btnSearch, btnRefresh, btnAdd, btnImport, btnExport;
    private TextView tvNoData;

    public BucketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bucket, container, false);

        etBucketCode = view.findViewById(R.id.et_bucket_code);
        btnSearch = view.findViewById(R.id.btn_search);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        btnAdd = view.findViewById(R.id.btn_add);
        btnImport = view.findViewById(R.id.btn_import);
        btnExport = view.findViewById(R.id.btn_export);
        tvNoData = view.findViewById(R.id.tv_no_data);

        // 设置按钮点击事件
        btnSearch.setOnClickListener(v -> searchBuckets());
        btnRefresh.setOnClickListener(v -> refreshBuckets());
        btnAdd.setOnClickListener(v -> addBucket());
        btnImport.setOnClickListener(v -> importBuckets());
        btnExport.setOnClickListener(v -> exportBuckets());

        // 初始化数据
        refreshBuckets();

        return view;
    }

    private void searchBuckets() {
        String code = etBucketCode.getText().toString().trim();
        // 模拟搜索操作
        // 实际项目中这里会调用API进行搜索
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void refreshBuckets() {
        // 模拟刷新操作
        // 实际项目中这里会调用API获取最新数据
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void addBucket() {
        // 跳转到新增料桶页面
        // 实际项目中这里会启动一个新的Activity或显示一个Dialog
    }

    private void importBuckets() {
        // 跳转到导入料桶页面
        // 实际项目中这里会启动一个新的Activity或显示一个Dialog
    }

    private void exportBuckets() {
        // 导出料桶数据
        // 实际项目中这里会调用API导出数据
    }
}
