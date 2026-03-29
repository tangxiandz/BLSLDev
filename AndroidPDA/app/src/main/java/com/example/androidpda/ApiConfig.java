package com.example.androidpda;

public class ApiConfig {
    // API服务器地址
    // 注意：在实际部署时，需要修改为服务器的实际IP地址
    // 例如："http://192.168.1.100:5018"
    public static final String BASE_URL = "https://blslapi.tangxiandz.com";
    
    // API端点
    public static final String MIXING_MATERIALS = "/api/Mixing/materials/";
    public static final String MIXING_RECORD = "/api/Mixing/record";
    public static final String FEEDING_BUCKETS = "/api/Feeding/buckets/";
    public static final String FEEDING_VALIDATE = "/api/Feeding/validate";
    public static final String FEEDING_RECORD = "/api/Feeding/record";
}
