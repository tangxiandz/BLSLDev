package com.example.androidpda;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PLC工具类，用于统一管理PLC通信
 */
public class PlcUtil {

    private static final String LOG_FILE_NAME = "plc_comm.log";

    /**
     * 通过PLC打开桶盖
     * @param context 上下文
     * @param bucketCode 桶号
     */
    public static void openBucketByPLC(Context context, String bucketCode) {
        // 检查PLC连接状态
        if (!PlcStatusManager.getInstance(context).isPlcConnected()) {
            String message = "PLC未连接，跳过开桶操作";
            log(context, "INFO", message);
            showToast(context, message);
            return;
        }

        new Thread(() -> {
            Socket socket = null;
            PrintWriter writer = null;
            try {
                // 从配置中获取PLC信息
                String plcIp = ServerFragment.getPlcIp(context);
                int plcPort = ServerFragment.getPlcPort(context);
                String plcProtocol = ServerFragment.getPlcProtocol(context);

                if (plcIp.isEmpty() || plcPort == 0) {
                    String message = "PLC配置未设置";
                    log(context, "ERROR", message);
                    showToast(context, message);
                    return;
                }

                log(context, "INFO", "通过PLC打开桶盖：" + bucketCode);
                log(context, "INFO", "PLC配置：IP=" + plcIp + ", Port=" + plcPort + ", Protocol=" + plcProtocol);

                // 实际项目中需要根据PLC的通信协议发送相应的指令
                // 示例：
                log(context, "INFO", "开始建立PLC连接");
                socket = new Socket();
                // 设置连接超时为5秒
                socket.connect(new java.net.InetSocketAddress(plcIp, plcPort), 5000);
                // 设置读写超时为3秒
                socket.setSoTimeout(3000);
                log(context, "INFO", "PLC连接建立成功");
                
                writer = new PrintWriter(socket.getOutputStream(), true);
                // 发送开桶指令，格式根据PLC协议确定
                String command = "OPEN_BUCKET:" + bucketCode;
                log(context, "INFO", "发送指令：" + command);
                writer.println(command);
                writer.flush();
                log(context, "INFO", "指令发送成功");
                
                // 等待响应（如果需要）
                // 这里可以根据PLC协议添加响应处理逻辑

                String successMessage = "桶盖已打开";
                log(context, "INFO", successMessage);
                showToast(context, successMessage);

            } catch (java.net.SocketTimeoutException e) {
                String errorMessage = "PLC连接超时：" + e.getMessage();
                log(context, "ERROR", errorMessage);
                e.printStackTrace();
                showToast(context, "PLC连接超时");
                // 更新PLC连接状态为离线
                PlcStatusManager.getInstance(context).setPlcConnected(false);
            } catch (java.net.UnknownHostException e) {
                String errorMessage = "PLC主机未知：" + e.getMessage();
                log(context, "ERROR", errorMessage);
                e.printStackTrace();
                showToast(context, "PLC主机未知");
                // 更新PLC连接状态为离线
                PlcStatusManager.getInstance(context).setPlcConnected(false);
            } catch (java.io.IOException e) {
                String errorMessage = "PLC通讯失败：" + e.getMessage();
                log(context, "ERROR", errorMessage);
                e.printStackTrace();
                showToast(context, "PLC通信失败");
                // 更新PLC连接状态为离线
                PlcStatusManager.getInstance(context).setPlcConnected(false);
            } catch (Exception e) {
                String errorMessage = "PLC操作异常：" + e.getMessage();
                log(context, "ERROR", errorMessage);
                e.printStackTrace();
                showToast(context, "PLC操作异常");
                // 更新PLC连接状态为离线
                PlcStatusManager.getInstance(context).setPlcConnected(false);
            } finally {
                // 确保资源被正确释放
                try {
                    if (writer != null) {
                        writer.close();
                    }
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        log(context, "INFO", "PLC连接已关闭");
                    }
                } catch (Exception e) {
                    log(context, "ERROR", "关闭PLC连接失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 显示Toast消息
     * @param context 上下文
     * @param message 消息内容
     */
    private static void showToast(Context context, String message) {
        // 在UI线程上显示Toast
        if (context != null) {
            android.os.Handler handler = new android.os.Handler(context.getMainLooper());
            handler.post(() -> {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 记录日志
     * @param context 上下文
     * @param level 日志级别
     * @param message 日志内容
     */
    private static void log(Context context, String level, String message) {
        // 打印到控制台
        System.out.println("[" + level + "] " + message);
        
        // 写入到日志文件
        writeLogToFile(level, message);
    }

    /**
     * 写入日志到文件
     * @param level 日志级别
     * @param message 日志内容
     */
    private static void writeLogToFile(String level, String message) {
        try {
            // 获取存储目录
            File logDir = new File(Environment.getExternalStorageDirectory(), "BLSL/Logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // 创建日志文件
            File logFile = new File(logDir, LOG_FILE_NAME);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            
            // 写入日志
            FileWriter writer = new FileWriter(logFile, true);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());
            writer.write("[" + timestamp + "] [" + level + "] " + message + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
