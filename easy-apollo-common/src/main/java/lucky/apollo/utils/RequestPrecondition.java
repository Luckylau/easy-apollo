package lucky.apollo.utils;

import lucky.apollo.exception.BadRequestException;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
public class RequestPrecondition {
    private static String CONTAIN_EMPTY_ARGUMENT = "request payload should not be contain empty.";

    private static String ILLEGAL_MODEL = "request model is invalid";

    public static void checkArgumentsNotEmpty(String... args) {
        checkArguments(!StringUtils.isContainEmpty(args), CONTAIN_EMPTY_ARGUMENT);
    }

    public static void checkModel(boolean valid) {
        checkArguments(valid, ILLEGAL_MODEL);
    }

    public static void checkArguments(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new BadRequestException(String.valueOf(errorMessage));
        }
    }
}