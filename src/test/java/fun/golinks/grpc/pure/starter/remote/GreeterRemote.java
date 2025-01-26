package fun.golinks.grpc.pure.starter.remote;

import fun.golinks.grpc.pure.GreeterGrpc;
import fun.golinks.grpc.pure.HelloReply;
import fun.golinks.grpc.pure.HelloRequest;
import fun.golinks.grpc.pure.util.GrpcConsumer;
import io.grpc.stub.StreamObserver;

public class GreeterRemote extends GreeterGrpc.GreeterImplBase {

    private static final String GREETING_PREFIX = "Server:Hello ";

    private static final GrpcConsumer<HelloRequest, HelloReply> grpcConsumer = GrpcConsumer.wrap(helloRequest -> buildHelloReply(helloRequest.getName()));

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        grpcConsumer.accept(req, responseObserver);
    }

    private static HelloReply buildHelloReply(String name) {
        return HelloReply.newBuilder()
                .setMessage(GREETING_PREFIX + name)
                .build();
    }
}