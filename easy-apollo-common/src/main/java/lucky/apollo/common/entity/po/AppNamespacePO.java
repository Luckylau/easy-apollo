package lucky.apollo.common.entity.po;

import lucky.apollo.common.constant.ConfigFileFormat;
import lucky.apollo.common.utils.Validator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Entity
@Table(name = "AppNamespace")
@SQLDelete(sql = "Update AppNamespace set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class AppNamespacePO extends BasePO {
    @NotBlank(message = "App Name cannot be blank")
    @Pattern(
            regexp = Validator.APP_CLUSTER_NAMESPACE_VALIDATOR,
            message = "Namespace格式错误: " + Validator.INVALID_APP_CLUSTER_NAMESPACE_MESSAGE + " & " + Validator.INVALID_NAMESPACE_MESSAGE
    )
    @Column(name = "Name", nullable = false)
    private String name;

    @NotBlank(message = "AppId cannot be blank")
    @Column(name = "AppId", nullable = false)
    private String appId;

    @Column(name = "Format", nullable = false)
    private String format;

    @Column(name = "Comment")
    private String comment;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConfigFileFormat formatAsEnum() {
        return ConfigFileFormat.fromString(this.format);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return toStringHelper().add("name", name).add("appId", appId).add("comment", comment)
                .add("format", format).toString();
    }
}