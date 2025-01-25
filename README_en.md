# grpc-pure-spring-boot-starter

`grpc-pure-spring-boot-starter` is a lightweight framework that simplifies the integration of gRPC into Spring Boot applications. As an excellent open-source RPC framework, gRPC is widely adopted by large enterprises. However, smaller organizations may face challenges in expanding and customizing it effectively. This project is based on my prior experience of implementing large-scale gRPC services in a previous internet company. It aims to provide developers with an easy-to-use and efficient integration example.

## Core Features

1. **Server-Side Enhancements**:
    - Provides integration slots for service registration, enabling easy adaptation to different registries.
    - Built-in support for the `Nacos` registry.

2. **Client-Side Enhancements**:
    - Adds a connection `Ping` mechanism to maintain connection health.
    - Enables effortless service discovery, adaptable to various registries.
    - Supports `nacos://{ServiceName}` protocol format for service discovery out of the box.

## Quick Start

### Prerequisites

The examples in this project depend on the Nacos registry. Please refer to the [Nacos Quick Start Guide](https://nacos.io/docs/v2.3/quickstart/quick-start/) to install and start a Nacos Server instance.

### Add Dependencies

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>fun.golinks</groupId>
    <artifactId>grpc-pure-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Optional: Configure Protobuf Compilation

You can configure the Maven Protobuf plugin to compile `.proto` files as shown below:

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

### Define Protobuf File (`greeter.proto`)

Create a `greeter.proto` file in the `src/main/proto` directory, as shown below:

```protobuf
syntax = "proto3";

package fun.golinks.grpc.pure;

option java_multiple_files = true;

// Greeting Service Definition
service Greeter {
  rpc SayHello (HelloRequest) returns (HelloReply);
}

// Request message containing the user's name
message HelloRequest {
  string name = 1;
}

// Response message containing the greeting
message HelloReply {
  string message = 1;
}
```

### Configuration Properties and Default Values

Configure gRPC-related settings in the `application.yml` file. Below is an example with default values:

```yaml
grpc:
  pure:
    server:
      port: 9999
  discovery:
    type: nacos
    nacos:
      address: 127.0.0.1:8848
      username: nacos
      password: nacos
```

### Server-Side: Implement Greeter Service

Create a Spring-managed Bean representing the server-side implementation of the Greeting Service:

```java
import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.HelloReply;
import fun.golinks.grpc.pure.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GreeterRemote extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage(String.format("Server: Hello %s", req.getName()))
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
```

### Client-Side: Configure GrpcPure

Set up the client configuration to connect to the service using the `nacos://` protocol:

```java
import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.GrpcChannels;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcPureConfig {

    @Bean
    public GreeterGrpc.GreeterBlockingStub greeterBlockingStub(GrpcChannels grpcChannels) {
        ManagedChannel managedChannel = grpcChannels.create("nacos://greeter");
        return GreeterGrpc.newBlockingStub(managedChannel);
    }
}
```

### Client-Side: Call Remote Services

Leverage the client configuration to call the gRPC service:

```java
import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.HelloReply;
import fun.golinks.grpc.pure.HelloRequest;
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
            HelloReply helloReply = greeterBlockingStub
                    .withDeadlineAfter(10, TimeUnit.SECONDS)
                    .sayHello(HelloRequest.newBuilder()
                            .setName(RandomStringUtils.randomAlphabetic(32))
                            .build());
            log.info("Response: {}", helloReply);
        }
    }
}
```

## License

This project is licensed under the [MIT License](./LICENSE), making it freely available for usage and modification.

---

By integrating `grpc-pure-spring-boot-starter`, you can effortlessly integrate gRPC into your Spring Boot applications and enjoy a simple, efficient, and scalable RPC solution!