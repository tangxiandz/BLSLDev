package com.example.androidpda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class StatisticsFragment extends Fragment {

    private EditText etKeyword;
    private Button btnSearch, btnReset, btnExport;
    private TextView tvNoData;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        etKeyword = view.findViewById(R.id.et_keyword);
        btnSearch = view.findViewById(R.id.btn_search);
        btnReset = view.findViewById(R.id.btn_reset);
        btnExport = view.findViewById(R.id.btn_export);
        tvNoData = view.findViewById(R.id.tv_no_data);

        // 设置按钮点击事件
        btnSearch.setOnClickListener(v -> searchStatistics());
        btnReset.setOnClickListener(v -> resetForm());
        btnExport.setOnClickListener(v -> exportStatistics());

        // 初始化数据
        searchStatistics();

        return view;
    }

    private void searchStatistics() {
        String keyword = etKeyword.getText().toString().trim();
        // 模拟搜索操作
        // 实际项目中这里会调用API进行搜索
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void resetForm() {
        etKeyword.setText("");
    }

    private void exportStatistics() {
        // 导出统计数据
        // 实际项目中这里会调用API导出数据
    }
}
