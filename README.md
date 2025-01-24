# grpc-pure-spring-boot-starter

Grpc作为优秀的开源框架受到大厂的青睐，但是对于小企业来说，不具备进行扩展的能力。有幸本人曾就职过一家互联网公司做过大规模Grpc服务的实战落地项目。所以将以往经验沉淀到这个项目中提供给需要的人使用

## 提供的能力点

1. 服务端添加对注册中心的插座，方便添加注册中心；目前内置对nacos的支持
2. 客户端添加ping机制
3. 客户端添加对注册中心的插座，方便添加服务发现；目前内置对nacos://{服务名} 协议的支持

## 代码示例

演示代码依赖nacos-server，请自行安装 [nacos-server](https://nacos.io/docs/v2.3/quickstart/quick-start/?spm=5238cd80.6a33be36.0.0.378b1e5dQqZvG2)

### 添加依赖

```xml

<dependency>
    <groupId>fun.golinks</groupId>
    <artifactId>grpc-pure-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 编译protobuf文件插件 【可选】

```xml
<build>
<extensions>
    <extension>
        <groupId>kr.motd.maven</groupId>
        <artifactId>os-maven-plugin</artifactId>
        <version>1.6.2</version>
    </extension>
</extensions>
<plugins>
    <plugin>
        <groupId>org.xolstice.maven.plugins</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>0.6.1</version>
        <configuration>
            <protocArtifact>com.google.protobuf:protoc:3.19.2:exe:${os.detected.classifier}</protocArtifact>
            <pluginId>grpc-java</pluginId>
            <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.42.1:exe:${os.detected.classifier}</pluginArtifact>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                    <goal>compile-custom</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
</build>
```

### 定义protobuf文件（greeter.proto）; 一般位于 src/main/proto目录下

```protobuf
syntax = "proto3";

package fun.golinks.grpc.pure;

option java_multiple_files = true;

// The greeting service definition.
service Greeter {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// The request message containing the user's name.
message HelloRequest {
  string name = 1;
}

// The response message containing the greetings
message HelloReply {
  string message = 1;
}
```

### 【服务端】实现Greeter服务

```java
import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.HelloReply;
import fun.golinks.grpc.pure.HelloRequest;
import io.grpc.stub.StreamObserver;

public class GreeterRemote extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage(String.format("Server:Hello %s", req.getName())).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
```

### 【客户端】 GrpcPure配置

```java
import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.GrpcChannels;
import fun.golinks.grpc.pure.starter.GrpcPureAutoConfiguration;
import fun.golinks.grpc.pure.starter.remote.GreeterRemote;
import io.grpc.ManagedChannel;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


```

### 【客户端】 调用远程服务

```java
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
```