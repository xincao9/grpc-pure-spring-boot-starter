package fun.golinks.grpc.pure.starter.remote;

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
