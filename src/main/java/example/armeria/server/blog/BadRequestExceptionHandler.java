package example.armeria.server.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction;

public class BadRequestExceptionHandler implements ExceptionHandlerFunction {
    public static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public HttpResponse handleException(ServiceRequestContext ctx, HttpRequest req, Throwable cause) {
        if (cause instanceof IllegalArgumentException) {
            String message = cause.getMessage();
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("error", message);
            return HttpResponse.ofJson(HttpStatus.BAD_REQUEST, objectNode);
        }
        return ExceptionHandlerFunction.fallthrough();
    }
}
