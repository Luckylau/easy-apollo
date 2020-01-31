package lucky.apollo.common.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.common.exception.AbstractApolloHttpException;
import lucky.apollo.common.exception.BadRequestException;
import org.slf4j.event.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Type;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.WARN;
import static org.springframework.http.HttpStatus.*;

/**
 * @Author luckylau
 * @Date 2019/7/12
 */
@ControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler {

    private Gson gson = new Gson();

    private static Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, Object>> exception(HttpServletRequest request, Throwable ex) {
        return handleError(request, INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeException.class})
    public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request,
                                                          ServletException ex) {
        return handleError(request, BAD_REQUEST, ex, WARN);
    }


    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, Object>> restTemplateException(HttpServletRequest request,
                                                                     HttpStatusCodeException ex) {
        return handleError(request, ex.getStatusCode(), ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accessDeny(HttpServletRequest request,
                                                          AccessDeniedException ex) {
        return handleError(request, FORBIDDEN, ex);
    }

    @ExceptionHandler({AbstractApolloHttpException.class})
    public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request, AbstractApolloHttpException ex) {
        return handleError(request, ex.getHttpStatus(), ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            HttpServletRequest request, MethodArgumentNotValidException ex
    ) {
        final Optional<ObjectError> firstError = ex.getBindingResult().getAllErrors().stream().findFirst();
        if (firstError.isPresent()) {
            final String firstErrorMessage = firstError.get().getDefaultMessage();
            return handleError(request, BAD_REQUEST, new BadRequestException(firstErrorMessage));
        }
        return handleError(request, BAD_REQUEST, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            HttpServletRequest request, ConstraintViolationException ex
    ) {
        return handleError(request, BAD_REQUEST, new BadRequestException(ex.getMessage()));
    }

    private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
                                                            HttpStatus status, Throwable ex) {
        return handleError(request, status, ex, ERROR);
    }

    private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
                                                            HttpStatus status, Throwable ex, Level logLevel) {
        String message = ex.getMessage();
        printLog(message, ex, logLevel);

        Map<String, Object> errorAttributes = new HashMap<>();
        boolean errorHandled = false;

        if (ex instanceof HttpStatusCodeException) {
            try {
                //try to extract the original error info if it is thrown from apollo programs, e.g. admin service
                errorAttributes = gson.fromJson(((HttpStatusCodeException) ex).getResponseBodyAsString(), mapType);
                status = ((HttpStatusCodeException) ex).getStatusCode();
                errorHandled = true;
            } catch (Throwable th) {
                //ignore
            }
        }

        if (!errorHandled) {
            errorAttributes.put("url", request.getRequestURL());
            errorAttributes.put("status", status.value());
            errorAttributes.put("message", message);
            errorAttributes.put("timestamp",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            errorAttributes.put("exception", ex.getClass().getName());

        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(errorAttributes, headers, status);
    }

    private void printLog(String message, Throwable ex, Level logLevel) {
        switch (logLevel) {
            case ERROR:
                log.error(message, ex);
                break;
            case WARN:
                log.warn(message, ex);
                break;
            case DEBUG:
                log.debug(message, ex);
                break;
            case INFO:
                log.info(message, ex);
                break;
            case TRACE:
                log.trace(message, ex);
                break;
            default:
                break;
        }
    }
}
