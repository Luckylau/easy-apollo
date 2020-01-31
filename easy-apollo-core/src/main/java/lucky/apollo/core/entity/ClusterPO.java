package lucky.apollo.core.entity;

import lucky.apollo.common.entity.po.BasePO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author luckylau
 * @Date 2019/10/11
 */
@Entity
@Table(name = "Cluster")
@SQLDelete(sql = "Update Cluster set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ClusterPO extends BasePO implements Comparable<ClusterPO> {

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "AppId", nullable = false)
    private String appId;

    @Column(name = "ParentClusterId", nullable = false)
    private long parentClusterId;

    public String getAppId() {
        return appId;
    }

    public String getName() {
        return name;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getParentClusterId() {
        return parentClusterId;
    }

    public void setParentClusterId(long parentClusterId) {
        this.parentClusterId = parentClusterId;
    }

    @Override
    public String toString() {
        return toStringHelper().add("name", name).add("appId", appId)
                .add("parentClusterId", parentClusterId).toString();
    }

    @Override
    public int compareTo(ClusterPO o) {
        if (o == null || getId() > o.getId()) {
            return 1;
        }

        if (getId() == o.getId()) {
            return 0;
        }

        return -1;
    }
}

