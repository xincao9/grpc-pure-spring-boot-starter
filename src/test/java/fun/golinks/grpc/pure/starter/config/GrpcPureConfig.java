package fun.golinks.grpc.pure.starter.config;

import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.GrpcChannels;
import fun.golinks.grpc.pure.starter.GrpcPureAutoConfiguration;
import fun.golinks.grpc.pure.starter.remote.GreeterRemote;
import io.grpc.ManagedChannel;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ImportAutoConfiguration(GrpcPureAutoConfiguration.class)
@Configuration
public class GrpcPureConfig {

    @Bean
    public GreeterGrpc.GreeterBlockingStub greeterBlockingStub(GrpcChannels grpcChannels) {
        ManagedChannel managedChannel = grpcChannels.create("nacos://greeter");
        return GreeterGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public GreeterRemote greeterRemote() {
        return new GreeterRemote();
    }
}
