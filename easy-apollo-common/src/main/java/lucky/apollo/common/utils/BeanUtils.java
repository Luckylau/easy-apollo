package lucky.apollo.common.utils;

import lucky.apollo.common.exception.BeanUtilsException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

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

    public static <T> List<T> batchTransformWithIgnoreNull(final Class<T> clazz, List<?> srcList) {
        if (CollectionUtils.isEmpty(srcList)) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(srcList.size());
        for (Object srcObject : srcList) {
            result.add(transformWithIgnoreNull(clazz, srcObject));
        }
        return result;
    }


    /**
     * 用于将一个列表转换为列表中的对象的某个属性映射到列表中的对象
     *
     * <pre>
     *      List<UserDTO> userList = userService.queryUsers();
     *      Map<Integer, userDTO> userIdToUser = BeanUtil.mapByKey("userId", userList);
     * </pre>
     *
     * @param key 属性名
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapByKey(String key, List<?> list) {
        Map<K, V> map = new HashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            return map;
        }
        try {
            Class<?> clazz = list.get(0).getClass();
            Field field = deepFindField(clazz, key);
            if (field == null) {
                throw new IllegalArgumentException("Could not find the key");
            }
            field.setAccessible(true);
            for (Object o : list) {
                map.put((K) field.get(o), (V) o);
            }
        } catch (Exception e) {
            throw new BeanUtilsException(e);
        }
        return map;
    }

    private static Field deepFindField(Class<?> clazz, String key) {
        Field field = null;
        while (!clazz.getName().equals(Object.class.getName())) {
            try {
                field = clazz.getDeclaredField(key);
                if (field != null) {
                    break;
                }
            } catch (Exception e) {
                clazz = clazz.getSuperclass();
            }
        }
        return field;
    }

}