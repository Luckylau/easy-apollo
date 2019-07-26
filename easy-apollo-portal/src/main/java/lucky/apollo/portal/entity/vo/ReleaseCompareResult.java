package lucky.apollo.portal.entity.vo;


import lucky.apollo.common.entity.EntityPair;
import lucky.apollo.portal.constant.ChangeType;
import lucky.apollo.portal.entity.bo.KeyValueInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
public class ReleaseCompareResult {
    private List<Change> changes = new LinkedList<>();

    public void addEntityPair(ChangeType type, KeyValueInfo firstEntity, KeyValueInfo secondEntity) {
        changes.add(new Change(type, new EntityPair<>(firstEntity, secondEntity)));
    }

    public boolean hasContent() {
        return !changes.isEmpty();
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }
}