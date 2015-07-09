package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.Descriptor;
import io.protostuff.compiler.model.DescriptorType;
import io.protostuff.compiler.model.DynamicMessage;
import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.FieldType;
import io.protostuff.compiler.model.Group;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.ScalarFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Map;
import java.util.Set;

import static io.protostuff.compiler.parser.DefaultDescriptorProtoProvider.DESCRIPTOR_PROTO;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class OptionsPostProcessor implements ProtoContextPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionsPostProcessor.class);

    private final Provider<ProtoContext> descriptorProtoProvider;

    @Inject
    public OptionsPostProcessor(@Named(DESCRIPTOR_PROTO) Provider<ProtoContext> descriptorProtoProvider) {
        this.descriptorProtoProvider = descriptorProtoProvider;
    }

    @Override
    public void process(ProtoContext context) {
        ProtoWalker.newInstance(context)
                .onProto(this::checkOptions)
                .onMessage(this::checkOptions)
                .walk();
    }

    private void checkOptions(ProtoContext context, Descriptor descriptor) {
        DynamicMessage options = descriptor.getOptions();
        if (options.isEmpty()) {
            // nothing to check - skip this message
            return;
        }
        String descriptorClassName = descriptor.getClass().getSimpleName();
        String descriptorName = descriptor.getName();
        LOGGER.trace("processing class={} name={}", descriptorClassName, descriptorName);
        Message sourceMessage = findSourceMessage(context, descriptor.getDescriptorType());

        for (Map.Entry<DynamicMessage.Key, DynamicMessage.Value> entry : options.getFields()) {
            DynamicMessage.Key key = entry.getKey();
            DynamicMessage.Value value = entry.getValue();
            if (key.isExtension()) {
                // TODO check extension
            } else {
                // check standard option
                String fieldName = key.getName();
                Field field = sourceMessage.getField(fieldName);
                if (field == null) {
                    throw new ParserException(value, "Unknown option: '%s'", fieldName);
                }
                checkFieldValue(field, value);
            }
        }
    }

    private void checkFieldValue(Field field, DynamicMessage.Value value) {
        String fieldName = field.getName();
        FieldType fieldType = field.getType();
        DynamicMessage.Value.Type valueType = value.getType();
        if (fieldType instanceof ScalarFieldType) {
            if (!isAssignableFrom((ScalarFieldType) fieldType, valueType)) {
                throw new ParserException(value, "Cannot set option '%s': expected %s value", fieldName, fieldType);
            }
        } else if (fieldType instanceof Enum) {
            Enum anEnum = (Enum) fieldType;
            Set<String> allowedNames = anEnum.getValueNames();
            if (valueType != DynamicMessage.Value.Type.ENUM
                    || !allowedNames.contains(value.getEnumName())) {
                throw new ParserException(value, "Cannot set option '%s': expected enum = %s", fieldName, allowedNames);
            }
        } else if (fieldType instanceof Message || fieldType instanceof Group) {
            if (valueType != DynamicMessage.Value.Type.MESSAGE) {
                throw new ParserException(value, "Cannot set option '%s': expected message value", fieldName);
            }
        } else {
            throw new IllegalStateException("Unknown field type: " + fieldType);
        }
    }

    private boolean isAssignableFrom(ScalarFieldType target, DynamicMessage.Value.Type valueType) {

        switch (target) {
            case INT32:
            case INT64:
            case UINT32:
            case UINT64:
            case SINT32:
            case SINT64:
            case FIXED32:
            case FIXED64:
            case SFIXED32:
            case SFIXED64:
                // TODO check if value fits into target type
                return valueType == DynamicMessage.Value.Type.INTEGER;
            case FLOAT:
            case DOUBLE:
                // TODO check if value fits into target type
                return valueType == DynamicMessage.Value.Type.INTEGER
                        || valueType == DynamicMessage.Value.Type.FLOAT;
            case BOOL:
                return valueType == DynamicMessage.Value.Type.BOOLEAN;
            case STRING:
                return valueType == DynamicMessage.Value.Type.STRING;
            case BYTES:
                return valueType == DynamicMessage.Value.Type.STRING;
            default:
                throw new IllegalStateException("Unknown field type: " + target);
        }
    }

    private Message findSourceMessage(ProtoContext context, DescriptorType type) {
        Message message = tryResolveFromContext(context, type);
        if (message == null) {
            ProtoContext descriptorProto = descriptorProtoProvider.get();
            return tryResolveFromContext(descriptorProto, type);
        }
        return message;
    }

    private Message tryResolveFromContext(ProtoContext context, DescriptorType type) {
        switch (type) {
            case PROTO:
                return context.resolve(Message.class, ".google.protobuf.FileOptions");
            case ENUM:
                return context.resolve(Message.class, ".google.protobuf.EnumOptions");
            case ENUM_CONSTANT:
                return context.resolve(Message.class, ".google.protobuf.EnumValueOptions");
            case MESSAGE:
                return context.resolve(Message.class, ".google.protobuf.MessageOptions");
            case MESSAGE_FIELD:
                return context.resolve(Message.class, ".google.protobuf.FieldOptions");
            case GROUP:
                // Groups are not fully supported. For simplicity, in this place we assume
                // that only that options that are applicable for messages are also
                // applicable for groups.
                // But, actually it is invalid assumption, because both field and message
                // options are applicable to groups.
                return context.resolve(Message.class, ".google.protobuf.MessageOptions");
            case SERVICE:
                return context.resolve(Message.class, ".google.protobuf.ServiceOptions");
            case SERVICE_METHOD:
                return context.resolve(Message.class, ".google.protobuf.MethodOptions");
            default:
                throw new IllegalStateException("Unknown descriptor type: " + type);
        }
    }
}
