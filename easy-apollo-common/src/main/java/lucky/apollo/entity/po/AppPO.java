package lucky.apollo.entity.po;

import lucky.apollo.utils.Validator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Entity
@Table(name = "App")
@SQLDelete(sql = "Update App set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class AppPO extends BasePO {

    @NotBlank(message = "Name cannot be blank")
    @Column(name = "Name", nullable = false)
    private String name;

    @NotBlank(message = "AppId cannot be blank")
    @Pattern(
            regexp = Validator.APP_CLUSTER_NAMESPACE_VALIDATOR,
            message = Validator.INVALID_APP_CLUSTER_NAMESPACE_MESSAGE
    )
    @Column(name = "AppId", nullable = false)
    private String appId;

    @Column(name = "OrgId", nullable = false)
    private String orgId;

    @Column(name = "OrgName", nullable = false)
    private String orgName;

    @NotBlank(message = "OwnerName cannot be blank")
    @Column(name = "OwnerName", nullable = false)
    private String ownerName;

    @NotBlank(message = "OwnerEmail cannot be blank")
    @Column(name = "OwnerEmail", nullable = false)
    private String ownerEmail;

    public static Builder builder() {
        return new Builder();
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return toStringHelper().add("name", name).add("appId", appId)
                .add("orgId", orgId)
                .add("orgName", orgName)
                .add("ownerName", ownerName)
                .add("ownerEmail", ownerEmail).toString();
    }

    public static class Builder {

        private AppPO app = new AppPO();

        public Builder() {
        }

        public Builder name(String name) {
            app.setName(name);
            return this;
        }

        public Builder appId(String appId) {
            app.setAppId(appId);
            return this;
        }

        public Builder orgId(String orgId) {
            app.setOrgId(orgId);
            return this;
        }

        public Builder orgName(String orgName) {
            app.setOrgName(orgName);
            return this;
        }

        public Builder ownerName(String ownerName) {
            app.setOwnerName(ownerName);
            return this;
        }

        public Builder ownerEmail(String ownerEmail) {
            app.setOwnerEmail(ownerEmail);
            return this;
        }

        public AppPO build() {
            return app;
        }

    }


}
