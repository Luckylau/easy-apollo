package lucky.apollo.constant;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @Author luckylau
 * @Date 2019/7/19
 */
public class GsonType {
    public static Type CONFIG = new TypeToken<Map<String, String>>() {
    }.getType();

}