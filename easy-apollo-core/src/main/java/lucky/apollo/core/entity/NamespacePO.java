package lucky.apollo.core.entity;

import lucky.apollo.common.entity.po.BasePO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Entity
@Table(name = "Namespace")
@SQLDelete(sql = "Update Namespace set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class NamespacePO extends BasePO {

    @Column(name = "appId", nullable = false)
    private String appId;

    @Column(name = "ClusterName", nullable = false)
    private String clusterName;

    @Column(name = "NamespaceName", nullable = false)
    private String namespaceName;

    public NamespacePO() {

    }

    public NamespacePO(String appId, String clusterName, String namespaceName) {
        this.appId = appId;
        this.clusterName = clusterName;
        this.namespaceName = namespaceName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    @Override
    public String toString() {
        return toStringHelper().add("appId", appId).add("clusterName", clusterName)
                .add("namespaceName", namespaceName).toString();
    }
}