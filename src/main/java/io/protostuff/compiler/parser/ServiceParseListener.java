package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.model.Service;
import io.protostuff.compiler.model.ServiceMethod;
import org.antlr.v4.runtime.BufferedTokenStream;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class ServiceParseListener extends AbstractProtoParserListener {


    protected ServiceParseListener(BufferedTokenStream tokens, ProtoContext context) {
        super(tokens, context);
    }

    @Override
    public void enterServiceBlock(ProtoParser.ServiceBlockContext ctx) {
        Service service = new Service();
        context.push(service);
    }

    @Override
    public void exitServiceBlock(ProtoParser.ServiceBlockContext ctx) {
        Service service = context.pop(Service.class);
        Proto proto = context.peek(Proto.class);
        String name = ctx.name().getText();
        service.setName(name);
        service.setSourceCodeLocation(getSourceCodeLocation(ctx));
        proto.addService(service);
        attachComments(ctx, service, false);
    }

    @Override
    public void enterRpcMethod(ProtoParser.RpcMethodContext ctx) {
        ServiceMethod method = new ServiceMethod();
        context.push(method);
    }

    @Override
    public void exitRpcMethod(ProtoParser.RpcMethodContext ctx) {
        ServiceMethod method = context.pop(ServiceMethod.class);
        Service service = context.peek(Service.class);
        String name = ctx.name().getText();
        String arg = ctx.typeReference(0).getText();
        String ret = ctx.typeReference(1).getText();
        method.setName(name);
        method.setArgTypeName(arg);
        method.setReturnTypeName(ret);
        method.setSourceCodeLocation(getSourceCodeLocation(ctx));
        service.addMethod(method);
        attachComments(ctx, method, true);
    }
}
