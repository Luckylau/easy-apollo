package lucky.apollo.portal.listener;

import com.google.common.base.Preconditions;
import lucky.apollo.common.entity.po.AppPO;
import org.springframework.context.ApplicationEvent;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
public class AppDeletionEvent extends ApplicationEvent {

    public AppDeletionEvent(Object source) {
        super(source);
    }

    public AppPO getApp() {
        Preconditions.checkState(source != null);
        return (AppPO) this.source;
    }
}