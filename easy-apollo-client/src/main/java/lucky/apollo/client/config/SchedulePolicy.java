package lucky.apollo.client.config;

/**
 * @Author luckylau
 * @Date 2020/11/22
 */
public interface SchedulePolicy {
    long fail();

    void success();
}
