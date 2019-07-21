package lucky.apollo.config;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import lucky.apollo.utils.ApolloThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Slf4j
public abstract class RefreshableConfig {

    private static final String LIST_SEPARATOR = ",";
    //TimeUnit: second
    private static final int CONFIG_REFRESH_INTERVAL = 60;

    /**
     * 例如："a, b, c, , ,d"" return "a", "b", "c", "d"
     */
    protected Splitter splitter = Splitter.on(LIST_SEPARATOR).omitEmptyStrings().trimResults();

    private RefreshablePropertySource propertySource;

    @Autowired
    private ConfigurableEnvironment environment;

    /**
     * 获取刷新资源
     *
     * @return
     */
    protected abstract RefreshablePropertySource getRefreshablePropertySource();

    @PostConstruct
    public void setup() {
        propertySource = getRefreshablePropertySource();
        if (propertySource == null) {
            throw new IllegalStateException("Property sources can not be empty.");
        }
        //该属性优先级放后面
        propertySource.refresh();
        environment.getPropertySources().addLast(propertySource);


        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, ApolloThreadFactory.create("ConfigRefresher", true));
        executorService.scheduleWithFixedDelay(() -> {
            try {
                propertySource.refresh();
            } catch (Throwable t) {
                log.error("Refresh configs failed.", t);
            }
        }, CONFIG_REFRESH_INTERVAL, CONFIG_REFRESH_INTERVAL, TimeUnit.SECONDS);
    }

    public String getValue(String key, String defaultValue) {
        try {
            return environment.getProperty(key, defaultValue);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public String getValue(String key) {
        return environment.getProperty(key);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            String value = getValue(key);
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        try {
            String value = getValue(key);
            return value == null ? defaultValue : "true".equals(value);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public String[] getArrayProperty(String key, String[] defaultValue) {
        try {
            String value = getValue(key);
            return Strings.isNullOrEmpty(value) ? defaultValue : value.split(LIST_SEPARATOR);
        } catch (Throwable e) {
            return defaultValue;
        }
    }


}