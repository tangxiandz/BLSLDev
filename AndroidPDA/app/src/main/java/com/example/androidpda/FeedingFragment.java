package com.example.androidpda;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FeedingFragment extends Fragment {

    private EditText etRawMaterialCode, etBucketCode;
    private TextView tvResult;
    private Button btnValidate;
    // 异步任务引用
    private ValidateAndSubmitTask validateAndSubmitTask;

    public FeedingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeding, container, false);

        etRawMaterialCode = view.findViewById(R.id.et_raw_material_code);
        etBucketCode = view.findViewById(R.id.et_bucket_code);
        tvResult = view.findViewById(R.id.tv_result);
        btnValidate = view.findViewById(R.id.btn_validate);

        // 检测网络连接
        checkNetwork();

        // 确保回车事件正确处理
        etRawMaterialCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    // 强制将焦点设置到桶号文本框
                    etBucketCode.requestFocus();
                    etBucketCode.setSelection(etBucketCode.getText().length());
                    // 确保事件被完全处理，不传递给其他处理程序
                    return true;
                }
                return false;
            }
        });
        
        // 设置桶号文本框的焦点监听，确保焦点能够正确获取
        etBucketCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // 当桶号文本框获取焦点时，将光标移到末尾
                etBucketCode.setSelection(etBucketCode.getText().length());
            }
        });

        // 设置桶号文本框回车触发验证
        etBucketCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                validateAndSubmit();
                return true;
            }
            return false;
        });

        // 设置验证按钮点击事件
        btnValidate.setOnClickListener(v -> validateAndSubmit());

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

    // 验证并提交加料记录
        private void validateAndSubmit() {
            String rawMaterialCode = etRawMaterialCode.getText().toString().trim();
            String bucketCode = etBucketCode.getText().toString().trim();

            // 添加日志
            android.util.Log.d("FeedingFragment", "原料料号: " + rawMaterialCode + ", 长度: " + rawMaterialCode.length());
            android.util.Log.d("FeedingFragment", "桶号: " + bucketCode + ", 长度: " + bucketCode.length());
            android.util.Log.d("FeedingFragment", "原料料号是否包含空格: " + rawMaterialCode.contains(" "));
            android.util.Log.d("FeedingFragment", "桶号是否包含空格: " + bucketCode.contains(" "));
            
            // 输出原始字符串的ASCII码
            StringBuilder rawMaterialCodeAscii = new StringBuilder();
            for (char c : rawMaterialCode.toCharArray()) {
                rawMaterialCodeAscii.append((int) c).append(" ");
            }
            android.util.Log.d("FeedingFragment", "原料料号ASCII: " + rawMaterialCodeAscii.toString());
            
            StringBuilder bucketCodeAscii = new StringBuilder();
            for (char c : bucketCode.toCharArray()) {
                bucketCodeAscii.append((int) c).append(" ");
            }
            android.util.Log.d("FeedingFragment", "桶号ASCII: " + bucketCodeAscii.toString());

            if (rawMaterialCode.isEmpty()) {
                tvResult.setText("请输入原料物料号");
                tvResult.setTextColor(getResources().getColor(R.color.danger));
                tvResult.setVisibility(View.VISIBLE);
                return;
            }

            if (bucketCode.isEmpty()) {
                tvResult.setText("请输入桶号");
                tvResult.setTextColor(getResources().getColor(R.color.danger));
                tvResult.setVisibility(View.VISIBLE);
                return;
            }

        // 取消之前的异步任务
        if (validateAndSubmitTask != null && !validateAndSubmitTask.isCancelled()) {
            validateAndSubmitTask.cancel(true);
        }

        // 直接提交到接口进行验证和保存
        validateAndSubmitTask = new ValidateAndSubmitTask();
        validateAndSubmitTask.execute(rawMaterialCode, bucketCode);
    }

    // 验证并提交加料记录的异步任务
    private class ValidateAndSubmitTask extends AsyncTask<String, Void, Boolean> {
        private String rawMaterialCode;
        private String bucketCode;

        @Override
        protected void onPreExecute() {
            tvResult.setText("验证中...");
            tvResult.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            rawMaterialCode = params[0];
            bucketCode = params[1];

            try {
                // 检查任务是否被取消
                if (isCancelled()) {
                    return false;
                }

                // 调用验证接口（该接口会同时验证并保存记录）
                URL url = new URL(ServerFragment.getServerUrl(getContext()) + ApiConfig.FEEDING_VALIDATE);
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

                // 检查任务是否被取消
                if (isCancelled()) {
                    connection.disconnect();
                    return false;
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 检查任务是否被取消
                        if (isCancelled()) {
                            reader.close();
                            connection.disconnect();
                            return false;
                        }
                        response.append(line);
                    }
                    reader.close();

                    // 解析JSON响应
                    JSONObject responseObject = new JSONObject(response.toString());
                    boolean isValid = responseObject.getBoolean("IsValid");
                    
                    if (isValid && !isCancelled()) {
                        // 验证成功，请求PLC开桶
                        openBucketByPLC(bucketCode);
                    }
                    
                    connection.disconnect();
                    return isValid;
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            // 检查Fragment是否已经分离
            if (!isAdded()) {
                return;
            }
            if (isValid) {
                // 显示成功消息
                new AlertDialog.Builder(getContext())
                        .setTitle("验证成功")
                        .setMessage("原料与桶号匹配一致，加料记录已提交")
                        .setPositiveButton("确认", (dialog, which) -> {
                            // 清空文本框，准备下一次扫描
                            etRawMaterialCode.setText("");
                            etBucketCode.setText("");
                            tvResult.setVisibility(View.GONE);
                            // 原料号输入框获取焦点
                            etRawMaterialCode.requestFocus();
                        })
                        .show();
            } else {
                // 验证失败，显示错误信息
                new AlertDialog.Builder(getContext())
                        .setTitle("验证失败")
                        .setMessage("桶号验证失败，原料与桶号不匹配")
                        .setPositiveButton("确认", (dialog, which) -> {
                            // 点击确认后清空桶号输入框
                            etBucketCode.setText("");
                            tvResult.setVisibility(View.GONE);
                        })
                        .show();
            }
        }
    }

    // 通过PLC开桶
    private void openBucketByPLC(String bucketCode) {
        // 使用PLC工具类打开桶盖
        PlcUtil.openBucketByPLC(getContext(), bucketCode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消所有正在执行的异步任务
        if (validateAndSubmitTask != null && !validateAndSubmitTask.isCancelled()) {
            validateAndSubmitTask.cancel(true);
        }
    }
}
