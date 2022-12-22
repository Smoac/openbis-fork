package ch.ethz.sis.afsserver.exception;

import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afs.api.dto.ExceptionType;
import ch.ethz.sis.afs.exception.RuntimeExceptionTemplate;
import ch.ethz.sis.shared.exception.ExceptionTemplateHolder;

import java.util.List;

import static ch.ethz.sis.afs.api.dto.ExceptionType.*;

public enum HttpExceptions implements ExceptionTemplateHolder {
    // ApiServer
    UNKNOWN(                                    RuntimeException.class,                         List.of(UnknownError),                             20001, "Unknown error of type %s, please contact support, this error comes with message: %s"),
    INVALID_PARAMETERS(                IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               20002, "Invalid parameters"),
    INVALID_HTTP_METHOD(                IllegalArgumentException.class,                 List.of(ClientDeveloperCodingError),               20003, "Invalid HTTP method");

    private RuntimeExceptionTemplate template;

    HttpExceptions(Class clazz, List<ExceptionType> types, int code, String messageTemplate) {
        this.template = new RuntimeExceptionTemplate(3, clazz, types, code, messageTemplate);
    }

    public RuntimeException getInstance(Object... args) {
        return template.getInstance(args);
    }

    public Exception getCheckedInstance(Object... args) {
        return template.getCheckedInstance(args);
    }

    public static void throwInstance(HttpExceptions exception, Object... args) {
        throw exception.getInstance(args);
    }

    public ExceptionReason getCause(Object... args) {
        return template.getReason(args);
    }
}
