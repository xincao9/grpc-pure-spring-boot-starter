package fun.golinks.grpc.pure.starter;

import fun.golinks.grpc.pure.GrpcChannels;
import fun.golinks.grpc.pure.GrpcServer;
import fun.golinks.grpc.pure.discovery.ServerRegister;
import fun.golinks.grpc.pure.discovery.nacos.NacosNameResolverProvider;
import fun.golinks.grpc.pure.discovery.nacos.NacosServerRegister;
import fun.golinks.grpc.pure.util.GrpcExecutors;
import fun.golinks.grpc.pure.util.GrpcThreadPoolExecutor;
import io.grpc.BindableService;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import io.grpc.ServerInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(GrpcPureProperties.class)
@Configuration
@ImportAutoConfiguration(GrpcPureAutoConfiguration.NacosDiscoveryConfiguration.class)
public class GrpcPureAutoConfiguration {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 4;
    private static final long KEEP_ALIVE_TIME = 1L;

    @Bean
    public GrpcThreadPoolExecutor grpcThreadPoolExecutor() {
        return GrpcExecutors.newGrpcThreadPoolExecutor("grpc-invoke", CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MINUTES, new LinkedBlockingDeque<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public GrpcChannels grpcChannels(NameResolverProvider nameResolverProvider,
                                     GrpcThreadPoolExecutor grpcThreadPoolExecutor, List<ClientInterceptor> clientInterceptors) throws Throwable {
        GrpcChannels.Builder builder = GrpcChannels.newBuilder().setNameResolverProvider(nameResolverProvider)
                .setExecutor(grpcThreadPoolExecutor);
        if (clientInterceptors != null && !clientInterceptors.isEmpty()) {
            builder.addClientInterceptor(clientInterceptors.toArray(new ClientInterceptor[0]));
        }
        return builder.build();
    }

    @Bean
    public GrpcServer grpcServer(ServerRegister serverRegister, GrpcPureProperties grpcPureProperties,
                                 List<BindableService> bindableServices, List<ServerInterceptor> serverInterceptors) throws Throwable {
        GrpcServer.Builder builder = GrpcServer.newBuilder().setPort(grpcPureProperties.getServer().getPort())
                .addService(bindableServices.toArray(new BindableService[0])).setServerRegister(serverRegister);
        if (serverInterceptors != null && !serverInterceptors.isEmpty()) {
            builder.addServerInterceptor(serverInterceptors.toArray(new ServerInterceptor[0]));
        }
        return builder.build();
    }

    @ConditionalOnProperty(prefix = "grpc.pure.discovery", name = "type", havingValue = "nacos", matchIfMissing = true)
    public static class NacosDiscoveryConfiguration {

        private final String applicationName;
        private final NacosProperties nacosProperties;

        public NacosDiscoveryConfiguration(@Value("${spring.application.name}") String applicationName,
                GrpcPureProperties grpcPureProperties) {
            this.applicationName = applicationName;
            this.nacosProperties = grpcPureProperties.getDiscovery().getNacos();
        }

        @Bean
        public NameResolverProvider nameResolverProvider() throws Throwable {
            return NacosNameResolverProvider.newBuilder().setServerAddress(nacosProperties.getAddress())
                    .setUsername(nacosProperties.getUsername()).setPassword(nacosProperties.getPassword()).build();
        }

        @Bean
        public ServerRegister serverRegister(GrpcPureProperties grpcPureProperties) throws Throwable {
            String appName = grpcPureProperties.getAppName();
            if (StringUtils.isBlank(appName)) {
                appName = applicationName;
            }
            return NacosServerRegister.newBuilder().setAppName(appName).setServerAddress(nacosProperties.getAddress())
                    .setUsername(nacosProperties.getUsername()).setPassword(nacosProperties.getPassword())
                    .setPort(grpcPureProperties.getServer().getPort()) // 后端服务监听端口
                    .build();
        }
    }
}