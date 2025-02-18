package fun.golinks.grpc.pure.starter;

import com.alibaba.nacos.api.exception.NacosException;
import fun.golinks.grpc.pure.GrpcChannels;
import fun.golinks.grpc.pure.GrpcServer;
import fun.golinks.grpc.pure.discovery.ServerRegister;
import fun.golinks.grpc.pure.discovery.nacos.NacosNameResolverProvider;
import fun.golinks.grpc.pure.discovery.nacos.NacosNamingService;
import fun.golinks.grpc.pure.discovery.nacos.NacosServerRegister;
import fun.golinks.grpc.pure.util.EnhanceThreadPoolExecutor;
import fun.golinks.grpc.pure.util.Executors;
import io.grpc.BindableService;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import io.grpc.ServerInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
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
    public EnhanceThreadPoolExecutor enhanceThreadPoolExecutor() {
        return Executors.newGrpcThreadPoolExecutor("grpc-invoke", CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MINUTES, new LinkedBlockingDeque<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public GrpcChannels grpcChannels(ObjectProvider<NameResolverProvider> nameResolverProvider,
            EnhanceThreadPoolExecutor enhanceThreadPoolExecutor, List<ClientInterceptor> clientInterceptors)
            throws Throwable {
        GrpcChannels.Builder builder = GrpcChannels.newBuilder()
                .setNameResolverProvider(nameResolverProvider.getIfAvailable()).setExecutor(enhanceThreadPoolExecutor);
        if (clientInterceptors != null && !clientInterceptors.isEmpty()) {
            builder.addClientInterceptor(clientInterceptors.toArray(new ClientInterceptor[0]));
        }
        return builder.build();
    }

    @Bean
    public GrpcServer grpcServer(ObjectProvider<ServerRegister> serverRegister, GrpcPureProperties grpcPureProperties,
            List<BindableService> bindableServices, List<ServerInterceptor> serverInterceptors) throws Throwable {
        GrpcServer.Builder builder = GrpcServer.newBuilder().setPort(grpcPureProperties.getServer().getPort())
                .addService(bindableServices.toArray(new BindableService[0]))
                .setServerRegister(serverRegister.getIfAvailable());
        if (serverInterceptors != null && !serverInterceptors.isEmpty()) {
            builder.addServerInterceptor(serverInterceptors.toArray(new ServerInterceptor[0]));
        }
        return builder.build();
    }

    @ConditionalOnProperty(prefix = "grpc.pure.discovery", name = "type", havingValue = "nacos")
    public static class NacosDiscoveryConfiguration {

        private final String applicationName;

        public NacosDiscoveryConfiguration(@Value("${spring.application.name}") String applicationName) {
            this.applicationName = applicationName;
        }

        @Bean
        public NacosNamingService nacosNamingService(GrpcPureProperties grpcPureProperties) throws NacosException {
            NacosConfig nacosConfig = grpcPureProperties.getDiscovery().getNacos();
            return NacosNamingService.newBuilder().setServerAddress(nacosConfig.getAddress())
                    .setUsername(nacosConfig.getUsername()).setPassword(nacosConfig.getPassword())
                    .setNamespace(nacosConfig.getNamespace()).build();
        }

        @Bean
        public NameResolverProvider nameResolverProvider(NacosNamingService nacosNamingService) {
            return NacosNameResolverProvider.newBuilder().setNacosNamingService(nacosNamingService).build();
        }

        @Bean
        public ServerRegister serverRegister(GrpcPureProperties grpcPureProperties,
                NacosNamingService nacosNamingService) {
            String appName = grpcPureProperties.getAppName();
            if (StringUtils.isBlank(appName)) {
                appName = applicationName;
            }
            return NacosServerRegister.newBuilder().setNacosNamingService(nacosNamingService).setAppName(appName)
                    .setPort(grpcPureProperties.getServer().getPort()) // 后端服务监听端口
                    .build();
        }
    }
}
