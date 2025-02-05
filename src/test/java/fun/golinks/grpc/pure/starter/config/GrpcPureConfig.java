package fun.golinks.grpc.pure.starter.config;

import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.GrpcChannels;
import fun.golinks.grpc.pure.starter.GrpcPureAutoConfiguration;
import fun.golinks.grpc.pure.starter.remote.GreeterRemote;
import io.grpc.ManagedChannel;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;

@ImportAutoConfiguration(GrpcPureAutoConfiguration.class)
public class GrpcPureConfig {

    private static final String GREETER_APP_URL = "127.0.0.1:9999";

    @Bean
    public GreeterGrpc.GreeterBlockingStub greeterBlockingStub(GrpcChannels grpcChannels) {
        ManagedChannel channel = grpcChannels.create(GREETER_APP_URL);
        return GreeterGrpc.newBlockingStub(channel);
    }

    @Bean
    public GreeterRemote greeterRemote() {
        return new GreeterRemote();
    }
}