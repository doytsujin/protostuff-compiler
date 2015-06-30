package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.EnumConstant;
import io.protostuff.compiler.model.EnumContainer;
import org.antlr.v4.runtime.BufferedTokenStream;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class EnumParseListener extends AbstractProtoParserListener {

    protected EnumParseListener(BufferedTokenStream tokens, ProtoContext context) {
        super(tokens, context);
    }

    @Override
    public void enterEnumBlock(ProtoParser.EnumBlockContext ctx) {
        Enum enumBuilder = new Enum();
        context.push(enumBuilder);
    }

    @Override
    public void exitEnumBlock(ProtoParser.EnumBlockContext ctx) {
        Enum e = context.pop(Enum.class);
        EnumContainer container = context.peek(EnumContainer.class);
        String name = ctx.NAME().getText();
        e.setName(name);
        e.setSourceCodeLocation(getSourceCodeLocation(ctx));
        container.addEnum(e);
        attachComments(ctx, e, false);
    }

    @Override
    public void enterEnumConstant(ProtoParser.EnumConstantContext ctx) {
        EnumConstant enumConstant = new EnumConstant();
        context.push(enumConstant);
    }

    @Override
    public void exitEnumConstant(ProtoParser.EnumConstantContext ctx) {
        EnumConstant enumConstant = context.pop(EnumConstant.class);
        Enum e = context.peek(Enum.class);
        String name = ctx.NAME().getText();
        int number = Integer.decode(ctx.INTEGER_VALUE().getText());
        enumConstant.setName(name);
        enumConstant.setValue(number);
        enumConstant.setSourceCodeLocation(getSourceCodeLocation(ctx));
        e.addValue(enumConstant);
        attachComments(ctx, enumConstant, true);
    }

}
