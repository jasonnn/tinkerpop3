package com.tinkerpop.gremlin.driver.ser;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tinkerpop.gremlin.driver.MessageSerializer;
import com.tinkerpop.gremlin.driver.message.RequestMessage;
import com.tinkerpop.gremlin.driver.message.ResponseMessage;
import com.tinkerpop.gremlin.driver.message.ResultCode;
import com.tinkerpop.gremlin.driver.message.ResultType;
import com.tinkerpop.gremlin.structure.io.kryo.GremlinKryo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class KryoMessageSerializerV1d0 implements MessageSerializer {
    // todo: maybe need a special interface for MessageSerializer because implementers can only binary serializers
    private static final GremlinKryo gremlinKryo = GremlinKryo.create(GremlinKryo.Version.V_1_0_0).build();
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String MIME_TYPE = "application/vnd.gremlin-v1.0+kryo";

    public static final String TOKEN_RESULT = "result";
    public static final String TOKEN_CODE = "code";
    public static final String TOKEN_REQUEST = "requestId";
    public static final String TOKEN_TYPE = "type";
    public static final String TOKEN_PROCESSOR = "processor";
    public static final String TOKEN_OP = "op";
    public static final String TOKEN_ARGS = "args";

    @Override
    public String[] mimeTypesSupported() {
        return new String[]{MIME_TYPE};
    }

    @Override
    public Optional<ResponseMessage> deserializeResponse(final ByteBuf msg) {
        try {
            final Kryo kryo = gremlinKryo.createKryo();
            final byte[] payload = new byte[msg.readableBytes()];
            msg.readBytes(payload);
            try (final Input input = new Input(payload)) {
                final Map<String,Object> responseData = (Map<String,Object>) kryo.readClassAndObject(input);
                return Optional.ofNullable(ResponseMessage.create(UUID.fromString(responseData.get(TOKEN_REQUEST).toString()), "")
                        .code(ResultCode.getFromValue((Integer) responseData.get(TOKEN_CODE)))
                        .result(responseData.get(TOKEN_RESULT))
                        .contents(ResultType.getFromValue((Integer) responseData.get(TOKEN_TYPE)))
                        .build());
            }
        } catch (Exception ex) {
            logger.warn("Response [{}] could not be deserialized by {}.", msg, KryoMessageSerializerV1d0.class.getName());
            return Optional.empty();
        }
    }

    @Override
    public ByteBuf serializeResponseAsBinary(final ResponseMessage responseMessage, final ByteBufAllocator allocator) {
        ByteBuf encodedMessage = null;
        try {
            final Map<String, Object> result = new HashMap<>();
            result.put(TOKEN_CODE, responseMessage.getCode().getValue());
            result.put(TOKEN_RESULT, responseMessage.getResult());
            result.put(TOKEN_REQUEST, responseMessage.getRequestId() != null ? responseMessage.getRequestId() : null);
            result.put(TOKEN_TYPE, responseMessage.getResultType().getValue());

            final Kryo kryo = gremlinKryo.createKryo();
            try (final OutputStream baos = new ByteArrayOutputStream()) {
                final Output output = new Output(baos);
                kryo.writeClassAndObject(output, result);

                // todo: could feasibly write a request bigger than an int?
                encodedMessage = allocator.buffer((int) output.total());
                encodedMessage.writeBytes(output.toBytes());
            }

            return encodedMessage;
        } catch (Exception ex) {
            if (encodedMessage != null) ReferenceCountUtil.release(encodedMessage);

            logger.warn("Response [{}] could not be serialized by {}.", responseMessage.toString(), KryoMessageSerializerV1d0.class.getName());
            throw new RuntimeException("Error during serialization.", ex);
        }
    }

    @Override
    public Optional<RequestMessage> deserializeRequest(final ByteBuf msg) {
        try {
            final Kryo kryo = gremlinKryo.createKryo();
            final byte[] payload = new byte[msg.readableBytes()];
            msg.readBytes(payload);
            try (final Input input = new Input(payload)) {
                final Map<String,Object> requestData = (Map<String,Object>) kryo.readClassAndObject(input);
                final RequestMessage.Builder builder = RequestMessage.create((String) requestData.get(TOKEN_OP))
                        .overrideRequestId((UUID) requestData.get(TOKEN_REQUEST))
                        .processor((String) requestData.get(TOKEN_PROCESSOR));
                final Map<String,Object> args = (Map<String,Object>) requestData.get(TOKEN_ARGS);
                args.forEach(builder::addArg);
                return Optional.ofNullable(builder.build());
            }
        } catch (Exception ex) {
            logger.warn("Request [{}] could not be deserialized by {}.", msg, KryoMessageSerializerV1d0.class.getName());
            return Optional.empty();
        }
    }

    @Override
    public ByteBuf serializeRequestAsBinary(final RequestMessage requestMessage, final ByteBufAllocator allocator) {
        ByteBuf encodedMessage = null;
        try {
            final Kryo kryo = gremlinKryo.createKryo();
            try (final OutputStream baos = new ByteArrayOutputStream()) {
                final Output output = new Output(baos);
                output.writeByte(MIME_TYPE.length());
                output.write(MIME_TYPE.getBytes(UTF8));

                final Map<String, Object> request = new HashMap<>();
                request.put(TOKEN_REQUEST, requestMessage.getRequestId());
                request.put(TOKEN_PROCESSOR, requestMessage.getProcessor());
                request.put(TOKEN_OP, requestMessage.getOp());
                request.put(TOKEN_ARGS, requestMessage.getArgs());

                kryo.writeClassAndObject(output, request);
                encodedMessage = allocator.buffer((int) output.total());
                encodedMessage.writeBytes(output.toBytes());
            }

            return encodedMessage;
        } catch (Exception ex) {
            if (encodedMessage != null) ReferenceCountUtil.release(encodedMessage);

            logger.warn("Request [{}] could not be serialized by {}.", requestMessage.toString(), KryoMessageSerializerV1d0.class.getName());
            throw new RuntimeException("Error during serialization.", ex);
        }
    }

    @Override
    public String serializeResponseAsString(final ResponseMessage responseMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String serializeRequestAsString(final RequestMessage requestMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ResponseMessage> deserializeResponse(final String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RequestMessage> deserializeRequest(final String msg) {
        throw new UnsupportedOperationException();
    }
}