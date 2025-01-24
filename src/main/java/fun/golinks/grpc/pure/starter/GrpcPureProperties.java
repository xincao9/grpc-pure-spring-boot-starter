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
     * 发现中心
     */
    private Discovery discovery = new Discovery();

    /**
     * 服务
     */
    private Server server = new Server();

    @Data
    public static class Discovery {
        /**
         * 类型
         */
        private String type = "nacos";

        /**
         * nacos配置
         */
        private Nacos nacos = new Nacos();
    }

    @Data
    public static class Nacos {
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

    @Data
    public static class Server {

        /**
         * 监听端口
         */
        private Integer port = 9999;
    }

}
