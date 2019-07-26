package lucky.apollo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public abstract class AbstractApolloHttpException extends RuntimeException {
    protected HttpStatus httpStatus;

    public AbstractApolloHttpException(String msg) {
        super(msg);
    }

    public AbstractApolloHttpException(String msg, Exception e) {
        super(msg, e);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    protected void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}