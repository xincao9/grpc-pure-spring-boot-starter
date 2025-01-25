package fun.golinks.grpc.pure.starter.remote;

import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.HelloReply;
import fun.golinks.grpc.pure.HelloRequest;
import io.grpc.stub.StreamObserver;

public class GreeterRemote extends GreeterGrpc.GreeterImplBase {

    private static final String GREETING_PREFIX = "Server:Hello ";

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = buildHelloReply(req.getName());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private HelloReply buildHelloReply(String name) {
        return HelloReply.newBuilder()
                .setMessage(GREETING_PREFIX + name)
                .build();
    }
}