package lucky.apollo.client.exception;

/**
 * @Author luckylau
 * @Date 2019/12/13
 */
public class ApolloConfigException extends RuntimeException {
    public ApolloConfigException(String message) {
        super(message);
    }

    public ApolloConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

