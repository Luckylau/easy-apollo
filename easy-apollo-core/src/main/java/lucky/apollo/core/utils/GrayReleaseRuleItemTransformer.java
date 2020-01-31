package lucky.apollo.core.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lucky.apollo.common.entity.dto.GrayReleaseRuleItemDTO;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/10/11
 */
public class GrayReleaseRuleItemTransformer {
    private static final Gson gson = new Gson();
    private static final Type grayReleaseRuleItemsType = new TypeToken<Set<GrayReleaseRuleItemDTO>>() {
    }.getType();

    public static Set<GrayReleaseRuleItemDTO> batchTransformFromJSON(String content) {
        return gson.fromJson(content, grayReleaseRuleItemsType);
    }

    public static String batchTransformToJSON(Set<GrayReleaseRuleItemDTO> ruleItems) {
        return gson.toJson(ruleItems);
    }
}
