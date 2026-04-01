package com.example.androidpda;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MixingFragment extends Fragment {

    private EditText etSemiProductCode;
    private LinearLayout llMaterialsList;
    private TextView tvResult;
    private Button btnSubmit;

    // 原料数据
    private List<MaterialInfo> materialList = new ArrayList<>();

    public MixingFragment() {
        // Required empty public constructor
    }

    private TextView tvSemiProductDesc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mixing, container, false);

        etSemiProductCode = view.findViewById(R.id.et_semi_product_code);
        llMaterialsList = view.findViewById(R.id.ll_materials_list);
        tvResult = view.findViewById(R.id.tv_result);
        tvSemiProductDesc = view.findViewById(R.id.tv_semi_product_desc);
        btnSubmit = view.findViewById(R.id.btn_submit);

        // 检测网络连接
        checkNetwork();

        // 设置文本变化监听器，当扫描输入时自动触发
        etSemiProductCode.setOnEditorActionListener((v, actionId, event) -> {
            searchMaterials();
            return true;
        });

        // 设置提交按钮点击事件
        btnSubmit.setOnClickListener(v -> searchMaterials());

        // 设置文本框获取焦点，便于扫描枪扫描
        etSemiProductCode.requestFocus();
        // 确保键盘不会自动弹出
        getActivity().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return view;
    }

    // 检测网络连接
    private void checkNetwork() {
        if (!NetworkUtil.isNetworkConnected(getContext())) {
            new AlertDialog.Builder(getContext())
                    .setTitle("网络连接提示")
                    .setMessage("当前设备未连接网络，无法获取数据。请检查网络连接后重试。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        // 点击确定后关闭对话框
                    })
                    .show();
        }
    }

    private void searchMaterials() {
        String semiProductCode = etSemiProductCode.getText().toString().trim();

        if (semiProductCode.isEmpty()) {
            return;
        }

        // 清空已开通的桶料集合
        openedBuckets.clear();

        // 调用API获取原料列表
        new GetMaterialsTask().execute(semiProductCode);
    }

    private void loadMaterials(List<MaterialInfo> materials) {
        // 清空原有列表
        llMaterialsList.removeAllViews();

        // 显示原料列表
        for (MaterialInfo material : materials) {
            addMaterialItem(material);
        }

        if (materials.isEmpty()) {
            tvResult.setText("未找到该半成品的原料信息");
            tvResult.setTextColor(getResources().getColor(R.color.danger));
            tvResult.setVisibility(View.VISIBLE);
        }
    }

    private void addMaterialItem(MaterialInfo material) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_material, llMaterialsList, false);

        TextView tvMaterialCode = itemView.findViewById(R.id.tv_material_code);
        TextView tvBucketCode = itemView.findViewById(R.id.tv_bucket_code);
        TextView tvQuantity = itemView.findViewById(R.id.tv_quantity);
        TextView tvOpenBucket = itemView.findViewById(R.id.tv_open_bucket);

        tvMaterialCode.setText("原料：" + material.getMaterialCode());
        tvBucketCode.setText("桶号：" + material.getBucketCode());
        tvQuantity.setText("数量：" + material.getQuantity());

        // 设置开桶文本点击事件
        tvOpenBucket.setOnClickListener(v -> openBucket(material));

        llMaterialsList.addView(itemView);
    }

    private void openBucket(MaterialInfo material) {
        // 打开桶盖（通过PLC通讯）
        PlcUtil.openBucketByPLC(getContext(), material.getBucketCode());
        
        // 显示开桶成功提示
        Toast.makeText(getContext(), "原料桶已打开并提交记录", Toast.LENGTH_LONG).show();

        // 提交拌料记录
        new SubmitMixingRecordTask().execute(etSemiProductCode.getText().toString().trim(), material.getMaterialCode(), material.getBucketCode(), material.getQuantity(), material);
    }

    // 获取物料信息的异步任务
    private class GetMaterialsTask extends AsyncTask<String, Void, List<MaterialInfo>> {
        @Override
        protected void onPreExecute() {
            llMaterialsList.removeAllViews();
            TextView loadingView = new TextView(getContext());
            loadingView.setText("加载中...");
            llMaterialsList.addView(loadingView);
        }

        @Override
        protected List<MaterialInfo> doInBackground(String... params) {
            String semiProductCode = params[0];
            List<MaterialInfo> materials = new ArrayList<>();

            try {
                URL url = new URL(ServerFragment.getServerUrl(getContext()) + ApiConfig.MIXING_MATERIALS + semiProductCode);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 解析JSON响应
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String materialCode = jsonObject.getString("RawMaterialCode");
                        String quantity = jsonObject.getString("Quantity");
                        String semiProductDesc = jsonObject.optString("RawMaterialDesc", "");

                        // 这里简化处理，实际项目中可能需要从桶料表中获取对应的桶号
                        String bucketCode = getBucketCodeByMaterialCode(materialCode);
                        materials.add(new MaterialInfo(materialCode, bucketCode, quantity, semiProductDesc));
                    }
                    
                    // 如果有数据，显示第一个半成品的描述
                    if (jsonArray.length() > 0) {
                        JSONObject firstObject = jsonArray.getJSONObject(0);
                        String semiProductDesc = firstObject.optString("RawMaterialDesc", "");
                        if (!semiProductDesc.isEmpty()) {
                            getActivity().runOnUiThread(() -> {
                                tvSemiProductDesc.setText(semiProductDesc);
                                tvSemiProductDesc.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return materials;
        }

        @Override
        protected void onPostExecute(List<MaterialInfo> result) {
            materialList = result;
            loadMaterials(result);
        }
    }

    // 已开通的桶料集合
    private Set<String> openedBuckets = new HashSet<>();

    // 提交拌料记录的异步任务
    private class SubmitMixingRecordTask extends AsyncTask<Object, Void, Boolean> {
        private MaterialInfo material;

        @Override
        protected void onPreExecute() {
            // 已经在openBucket方法中显示了提示，这里不再显示
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            String semiProductCode = (String) params[0];
            String rawMaterialCode = (String) params[1];
            String bucketCode = (String) params[2];
            String quantity = (String) params[3];
            material = (MaterialInfo) params[4];

            try {
                URL url = new URL(ServerFragment.getServerUrl(getContext()) + ApiConfig.MIXING_RECORD);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // 构建请求体
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("SemiProductCode", semiProductCode);
                jsonObject.put("RawMaterialCode", rawMaterialCode);
                jsonObject.put("BucketCode", bucketCode);
                jsonObject.put("Quantity", quantity);

                OutputStream os = connection.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // 添加到已开通的桶料集合
                openedBuckets.add(material.getBucketCode());
                // 检查是否所有桶料都已开通
                if (openedBuckets.size() >= materialList.size()) {
                    // 清空当前界面的信息
                    etSemiProductCode.setText("");
                    llMaterialsList.removeAllViews();
                    materialList.clear();
                    openedBuckets.clear();
                    tvSemiProductDesc.setVisibility(View.GONE);
                    // 显示所有桶料已开通的提示
                    Toast.makeText(getContext(), "所有桶料已开通，请扫描下一个半成品料号", Toast.LENGTH_LONG).show();
                }
            } else {
                // 显示提交失败的提示
                Toast.makeText(getContext(), "拌料记录提交失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 根据物料号获取桶号（模拟）
    private String getBucketCodeByMaterialCode(String materialCode) {
        // 实际项目中这里会从API获取
        switch (materialCode) {
            case "RM001":
                return "1#";
            case "RM002":
                return "2#";
            case "RM003":
                return "3#";
            default:
                return "未知桶号";
        }
    }

    // 原料信息类
    private static class MaterialInfo {
        private String materialCode;
        private String bucketCode;
        private String quantity;
        private String semiProductDesc;

        public MaterialInfo(String materialCode, String bucketCode, String quantity, String semiProductDesc) {
            this.materialCode = materialCode;
            this.bucketCode = bucketCode;
            this.quantity = quantity;
            this.semiProductDesc = semiProductDesc;
        }

        public String getMaterialCode() {
            return materialCode;
        }

        public String getBucketCode() {
            return bucketCode;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getSemiProductDesc() {
            return semiProductDesc;
        }
    }
}