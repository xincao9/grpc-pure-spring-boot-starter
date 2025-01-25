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

@Slf4j
@SpringBootTest(classes = GrpcPureConfig.class)
public class GreeterRemoteTests {

    private static final int RANDOM_STRING_LENGTH = 32;

    @Resource
    private GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Test
    public void testSayHello() {
        for (int i = 0; i < 100; i++) {
            HelloRequest request = createHelloRequest();
            log.info("REQUEST: {}", request);
            HelloReply response = greeterBlockingStub.sayHello(request);
            log.info("RESPONSE: {}", response);
        }
    }

    private HelloRequest createHelloRequest() {
        String randomName = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
        return HelloRequest.newBuilder().setName(randomName).build();
    }
}