package fun.golinks.grpc.pure.starter;

import lombok.Data;

/**
 * Nacos配置
 */
@Data
public class NacosConfig {
    /**
     * 地址
     */
    private String address = "127.0.0.1:8848";

    /**
     * 用户名
     */
    private String username = "nacos";

    /**
     * 密码
     */
    private String password = "nacos";
}