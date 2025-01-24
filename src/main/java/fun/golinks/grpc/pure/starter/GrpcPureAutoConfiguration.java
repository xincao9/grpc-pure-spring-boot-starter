package fun.golinks.grpc.pure.starter;

import fun.golinks.grpc.pure.GrpcChannels;
import fun.golinks.grpc.pure.GrpcServer;
import fun.golinks.grpc.pure.discovery.ServerRegister;
import fun.golinks.grpc.pure.discovery.nacos.NacosNameResolverProvider;
import fun.golinks.grpc.pure.discovery.nacos.NacosServerRegister;
import fun.golinks.grpc.pure.interceptor.LoggerClientInterceptor;
import fun.golinks.grpc.pure.util.GrpcExecutors;
import fun.golinks.grpc.pure.util.GrpcThreadPoolExecutor;
import io.grpc.BindableService;
import io.grpc.NameResolverProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(GrpcPureProperties.class)
@Configuration
@ImportAutoConfiguration(GrpcPureAutoConfiguration.NacosDiscoveryConfiguration.class)
public class GrpcPureAutoConfiguration {

    @Bean
    public GrpcThreadPoolExecutor grpcThreadPoolExecutor() {
        return GrpcExecutors.newGrpcThreadPoolExecutor("grpc-invoke", 1, 2, 1L, TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public GrpcChannels grpcChannels(NameResolverProvider nameResolverProvider,
                                     GrpcThreadPoolExecutor grpcThreadPoolExecutor) throws Throwable {
        return GrpcChannels.newBuilder().setNameResolverProvider(nameResolverProvider)
                .setExecutor(grpcThreadPoolExecutor)
                .setClientInterceptors(Collections.singleton(new LoggerClientInterceptor())).build();
    }

    @Bean
    public GrpcServer grpcServer(ServerRegister serverRegister, GrpcPureProperties grpcPureProperties,
                                 List<BindableService> bindableServices) throws Throwable {
        return GrpcServer.newBuilder().setPort(grpcPureProperties.getServer().getPort())
                .addService(bindableServices.toArray(new BindableService[0])).setServerRegister(serverRegister).build();
    }

    @ConditionalOnProperty(prefix = "grpc.pure.discovery", name = "type", havingValue = "nacos", matchIfMissing = true)
    public static class NacosDiscoveryConfiguration {

        @Value("${spring.application.name}")
        private String application;

        @Bean
        public NameResolverProvider nameResolverProvider(GrpcPureProperties grpcPureProperties) throws Throwable {
            GrpcPureProperties.Nacos nacos = grpcPureProperties.getDiscovery().getNacos();
            return NacosNameResolverProvider.newBuilder().setServerAddress(nacos.getAddress())
                    .setUsername(nacos.getUsername()).setPassword(nacos.getPassword()).build();
        }

        @Bean
        public ServerRegister serverRegister(GrpcPureProperties grpcPureProperties) throws Throwable {
            GrpcPureProperties.Nacos nacos = grpcPureProperties.getDiscovery().getNacos();
            return NacosServerRegister.newBuilder().setAppName(application).setServerAddress(nacos.getAddress())
                    .setUsername(nacos.getUsername()).setPassword(nacos.getPassword())
                    .setPort(grpcPureProperties.getServer().getPort()) // 后端服务监听端口
                    .build();
        }
    }
}
