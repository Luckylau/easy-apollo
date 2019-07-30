package lucky.apollo.core.entity;


import lucky.apollo.common.entity.po.BasePO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @Author luckylau
 * @Date 2019/7/24
 */
@Entity
@Table(name = "Release")
@SQLDelete(sql = "Update Release set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ReleasePO extends BasePO {
    @Column(name = "ReleaseKey", nullable = false)
    private String releaseKey;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "AppId", nullable = false)
    private String appId;

    @Column(name = "ClusterName", nullable = false)
    private String clusterName;

    @Column(name = "NamespaceName", nullable = false)
    private String namespaceName;

    @Column(name = "Configurations", nullable = false)
    @Lob
    private String configurations;

    @Column(name = "Comment", nullable = false)
    private String comment;

    @Column(name = "IsAbandoned", columnDefinition = "Bit default '0'")
    private boolean isAbandoned;

    public String getReleaseKey() {
        return releaseKey;
    }

    public void setReleaseKey(String releaseKey) {
        this.releaseKey = releaseKey;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getConfigurations() {
        return configurations;
    }

    public void setConfigurations(String configurations) {
        this.configurations = configurations;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAbandoned() {
        return isAbandoned;
    }

    public void setAbandoned(boolean abandoned) {
        isAbandoned = abandoned;
    }

    @Override
    public String toString() {
        return toStringHelper().add("name", name).add("appId", appId).add("clusterName", clusterName)
                .add("namespaceName", namespaceName).add("configurations", configurations)
                .add("comment", comment).add("isAbandoned", isAbandoned).toString();
    }
}