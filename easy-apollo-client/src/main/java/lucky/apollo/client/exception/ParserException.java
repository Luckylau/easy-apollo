package lucky.apollo.client.exception;

/**
 * @Author luckylau
 * @Date 2020/9/19
 */
public class ParserException extends Exception {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
