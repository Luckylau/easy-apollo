package lucky.apollo.client.exception;

/**
 * @Author luckylau
 * @Date 2020/3/11
 */
public class ApolloConfigStatusCodeException extends RuntimeException {
    private final int m_statusCode;

    public ApolloConfigStatusCodeException(int statusCode, String message) {
        super(String.format("[status code: %d] %s", statusCode, message));
        this.m_statusCode = statusCode;
    }

    public ApolloConfigStatusCodeException(int statusCode, Throwable cause) {
        super(cause);
        this.m_statusCode = statusCode;
    }

    public int getStatusCode() {
        return m_statusCode;
    }
}
