package lucky.apollo.utils;

import lucky.apollo.exception.BeanUtilsException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
public class BeanUtils {

    private static final String[] COPY_IGNORED_PROPERTIES = {"id", "dataChangeCreatedBy", "dataChangeCreatedTime", "dataChangeLastModifiedTime"};

    /**
     * The copy will ignore <em>BaseEntity</em> field
     *
     * @param source
     * @param target
     */
    public static void copyPropertiesWithIgnore(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, COPY_IGNORED_PROPERTIES);
    }

    public static <T> T transformWithIgnoreNull(Class<T> clazz, Object src) {
        if (src == null) {
            return null;
        }
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        org.springframework.beans.BeanUtils.copyProperties(src, instance, getNullPropertyNames(src));
        return instance;
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}