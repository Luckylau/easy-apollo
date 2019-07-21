package lucky.apollo.listener;

import com.google.common.base.Preconditions;
import lucky.apollo.entity.po.AppPO;
import org.springframework.context.ApplicationEvent;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public class AppCreationEvent extends ApplicationEvent {

    public AppCreationEvent(Object source) {
        super(source);
    }

    public AppPO getApp() {
        Preconditions.checkState(source != null);
        return (AppPO) this.source;
    }
}