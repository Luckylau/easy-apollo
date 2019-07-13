package lucky.apollo.entity.model;

import lombok.Data;

@Data
public class HttpResult<T> {

    /**
     *
     */
    private static final long serialVersionUID = 6095433538316185017L;

    private int code;
    private String message;
    private T data;

    public HttpResult() {
    }

    public HttpResult(int code, String message, T data) {
        this.code = code;
        this.setMessage(message);
        this.data = data;
    }

    public HttpResult(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public HttpResult(int code, String message) {
        this.code = code;
        this.setMessage(message);
    }


}