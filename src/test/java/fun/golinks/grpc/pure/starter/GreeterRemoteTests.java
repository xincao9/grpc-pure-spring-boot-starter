package fun.golinks.grpc.pure.starter;

import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.HelloReply;
import fun.golinks.grpc.pure.HelloRequest;
import fun.golinks.grpc.pure.starter.config.GrpcPureConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest(classes = GrpcPureConfig.class)
public class GreeterRemoteTests {

    @Resource
    private GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Test
    public void sayHello() {
        for (int i = 0; i < 100; i++) {
            HelloReply helloReply = greeterBlockingStub.withDeadlineAfter(10000, TimeUnit.MILLISECONDS)
                    .sayHello(HelloRequest.newBuilder().setName(RandomStringUtils.randomAlphabetic(32)).build());
            log.info("helloReply: {}", helloReply);
        }
    }

}
