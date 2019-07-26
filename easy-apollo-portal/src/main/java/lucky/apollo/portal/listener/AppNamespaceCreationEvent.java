package lucky.apollo.portal.listener;

import com.google.common.base.Preconditions;
import lucky.apollo.common.entity.po.AppNamespacePO;
import org.springframework.context.ApplicationEvent;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
public class AppNamespaceCreationEvent extends ApplicationEvent {

    public AppNamespaceCreationEvent(Object source) {
        super(source);
    }

    public AppNamespacePO getAppNamespace() {
        Preconditions.checkState(source != null);
        return (AppNamespacePO) this.source;
    }
}