package lucky.apollo.exception;

import org.springframework.http.HttpStatus;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
public class BadRequestException extends AbstractApolloHttpException {

    public BadRequestException(String str) {
        super(str);
        setHttpStatus(HttpStatus.BAD_REQUEST);
    }
}