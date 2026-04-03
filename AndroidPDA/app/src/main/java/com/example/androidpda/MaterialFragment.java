package com.example.androidpda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class MaterialFragment extends Fragment {

    private EditText etMaterialCode, etMaterialName;
    private Button btnSearch, btnRefresh, btnAdd, btnImport, btnExport;
    private TextView tvNoData;

    public MaterialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_material, container, false);

        etMaterialCode = view.findViewById(R.id.et_material_code);
        etMaterialName = view.findViewById(R.id.et_material_name);
        btnSearch = view.findViewById(R.id.btn_search);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        btnAdd = view.findViewById(R.id.btn_add);
        btnImport = view.findViewById(R.id.btn_import);
        btnExport = view.findViewById(R.id.btn_export);
        tvNoData = view.findViewById(R.id.tv_no_data);

        // 设置按钮点击事件
        btnSearch.setOnClickListener(v -> searchMaterials());
        btnRefresh.setOnClickListener(v -> refreshMaterials());
        btnAdd.setOnClickListener(v -> addMaterial());
        btnImport.setOnClickListener(v -> importMaterials());
        btnExport.setOnClickListener(v -> exportMaterials());

        // 初始化数据
        refreshMaterials();

        return view;
    }

    private void searchMaterials() {
        String code = etMaterialCode.getText().toString().trim();
        String name = etMaterialName.getText().toString().trim();
        // 模拟搜索操作
        // 实际项目中这里会调用API进行搜索
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void refreshMaterials() {
        // 模拟刷新操作
        // 实际项目中这里会调用API获取最新数据
        tvNoData.setVisibility(View.VISIBLE);
    }

    private void addMaterial() {
        // 跳转到新增物料页面
        // 实际项目中这里会启动一个新的Activity或显示一个Dialog
    }

    private void importMaterials() {
        // 跳转到导入物料页面
        // 实际项目中这里会启动一个新的Activity或显示一个Dialog
    }

    private void exportMaterials() {
        // 导出物料数据
        // 实际项目中这里会调用API导出数据
    }
}
