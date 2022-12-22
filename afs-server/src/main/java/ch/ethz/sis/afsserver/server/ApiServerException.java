package ch.ethz.sis.afsserver.server;

import lombok.NonNull;
import lombok.Value;

@Value
public class ApiServerException extends Exception {

    private String id;
    private ApiServerErrorType type;
    private Object data;

    public ApiServerException(String id, ApiServerErrorType type, @NonNull Object data) {
        super(data.toString());
        this.id = id;
        this.type = type;
        this.data = data;
    }

}
