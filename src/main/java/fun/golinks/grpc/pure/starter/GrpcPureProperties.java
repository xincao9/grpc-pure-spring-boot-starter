package fun.golinks.grpc.pure.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * grpc-pure配置属性
 */
@Data
@ConfigurationProperties(prefix = "grpc.pure")
public class GrpcPureProperties {

    /**
     * 应用名
     */
    private String appName;

    /**
     * 发现中心配置
     */
    private DiscoveryConfig discovery = new DiscoveryConfig();

    /**
     * 服务配置
     */
    private ServerConfig server = new ServerConfig();

    @Data
    public static class DiscoveryConfig {
        /**
         * 类型 (默认值：direct)
         */
        public static final String DEFAULT_TYPE = "direct";

        private String type = DEFAULT_TYPE;

        /**
         * Nacos 配置
         */
        private NacosConfig nacos = new NacosConfig();
    }

    @Data
    public static class ServerConfig {
        /**
         * 监听端口
         */
        private Integer port = 9999;
    }
}