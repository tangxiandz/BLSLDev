package com.example.androidpda;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.List;

public class FeedingFragment extends Fragment {

    private EditText etRawMaterialCode, etBucketCode;
    private TextView tvBucketList, tvResult;
    private List<String> bucketList;

    public FeedingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeding, container, false);

        etRawMaterialCode = view.findViewById(R.id.et_semi_product_code);
        etBucketCode = view.findViewById(R.id.et_bucket_code);
        tvBucketList = view.findViewById(R.id.tv_bucket_list);
        tvResult = view.findViewById(R.id.tv_result);

        // 检测网络连接
        checkNetwork();

        // 初始化桶号列表
        bucketList = new ArrayList<>();

        // 设置文本变化监听器，当扫描输入时自动触发
        etRawMaterialCode.setOnEditorActionListener((v, actionId, event) -> {
            getBucketList();
            return true;
        });

        etBucketCode.setOnEditorActionListener((v, actionId, event) -> {
            checkBucket();
            return true;
        });

        // 设置文本框获取焦点，便于扫描枪扫描
        etRawMaterialCode.requestFocus();

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

    private void getBucketList() {
        String rawMaterialCode = etRawMaterialCode.getText().toString().trim();

        if (rawMaterialCode.isEmpty()) {
            return;
        }

        // 调用API获取桶号列表
        new GetBucketsTask().execute(rawMaterialCode);
    }

    private void checkBucket() {
        String rawMaterialCode = etRawMaterialCode.getText().toString().trim();
        String bucketCode = etBucketCode.getText().toString().trim();

        if (bucketCode.isEmpty()) {
            return;
        }

        if (bucketList.isEmpty()) {
            tvResult.setText("请先扫描原料物料号获取桶号");
            tvResult.setTextColor(getResources().getColor(R.color.danger));
            tvResult.setVisibility(View.VISIBLE);
            return;
        }

        // 验证桶号是否在范围内
        boolean isInRange = bucketList.contains(bucketCode);

        if (isInRange) {
            // 比对一致，提交加料记录
            new ValidateBucketTask().execute(rawMaterialCode, bucketCode, "验证成功");
        } else {
            // 比对不一致，显示错误信息，并清空桶料
            tvResult.setText("验证失败：桶号不在范围内");
            tvResult.setTextColor(getResources().getColor(R.color.danger));
            tvResult.setVisibility(View.VISIBLE);
            // 清空桶号输入框
            etBucketCode.setText("");
        }
    }

    // 获取桶号列表的异步任务
    private class GetBucketsTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            tvBucketList.setText("加载中...");
        }

        @Override
        protected List<String> doInBackground(String... params) {
            String rawMaterialCode = params[0];
            List<String> buckets = new ArrayList<>();

            try {
                URL url = new URL("http://localhost:5018/api/Feeding/buckets/" + rawMaterialCode);
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
                        buckets.add(jsonArray.getString(i));
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return buckets;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            bucketList = result;
            if (bucketList.isEmpty()) {
                tvBucketList.setText("未找到对应的桶号列表");
            } else {
                StringBuilder sb = new StringBuilder();
                for (String bucket : bucketList) {
                    sb.append(bucket).append(" ");
                }
                tvBucketList.setText(sb.toString());
            }
        }
    }

    // 验证桶号并提交加料记录的异步任务
    private class ValidateBucketTask extends AsyncTask<String, Void, Boolean> {
        private String validationResult;

        @Override
        protected void onPreExecute() {
            tvResult.setText("验证中...");
            tvResult.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String rawMaterialCode = params[0];
            String bucketCode = params[1];
            validationResult = params[2];

            try {
                URL url = new URL("http://localhost:5018/api/Feeding/validate");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // 构建请求体
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("RawMaterialCode", rawMaterialCode);
                jsonObject.put("BucketCode", bucketCode);

                OutputStream os = connection.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

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
                    JSONObject responseObject = new JSONObject(response.toString());
                    return responseObject.getBoolean("IsValid");
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            if (isValid) {
                tvResult.setText("验证成功：桶号在范围内");
                tvResult.setTextColor(getResources().getColor(R.color.success));
                // 清空文本框，准备下一次扫描
                etRawMaterialCode.setText("");
                etBucketCode.setText("");
                tvBucketList.setText("");
                bucketList.clear();
            }
            tvResult.setVisibility(View.VISIBLE);
        }
    }
}