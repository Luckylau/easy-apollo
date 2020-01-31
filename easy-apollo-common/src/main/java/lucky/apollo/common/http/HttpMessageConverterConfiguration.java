package lucky.apollo.common.http;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/9/27
 */
@Configuration
public class HttpMessageConverterConfiguration {
    @Bean("messageConverters")
    @Primary
    public HttpMessageConverters messageConverters() {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(
                new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create());
        final List<HttpMessageConverter<?>> converters = Lists.newArrayList(
                new ByteArrayHttpMessageConverter(), new StringHttpMessageConverter(),
                new AllEncompassingFormHttpMessageConverter(), gsonHttpMessageConverter);
        return new HttpMessageConverters() {
            @Override
            public List<HttpMessageConverter<?>> getConverters() {
                return converters;
            }
        };
    }
}
