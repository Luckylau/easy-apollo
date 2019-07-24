package lucky.apollo.exception;

import org.springframework.http.HttpStatus;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
public class NotFoundException extends AbstractApolloHttpException {


    public NotFoundException(String str) {
        super(str);
        setHttpStatus(HttpStatus.NOT_FOUND);
    }
}