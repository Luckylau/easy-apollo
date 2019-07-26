package lucky.apollo.portal.entity.po;


import lucky.apollo.common.entity.po.BasePO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * @Author luckylau
 * @Date 2019/7/15
 */
@Entity
@Table(name = "ServerConfig")
@SQLDelete(sql = "Update ServerConfig set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ServerConfigPO extends BasePO {
    @NotBlank(message = "ServerConfig.Key cannot be blank")
    @Column(name = "Key", nullable = false)
    private String key;

    @NotBlank(message = "ServerConfig.Value cannot be blank")
    @Column(name = "Value", nullable = false)
    private String value;

    @Column(name = "Comment", nullable = false)
    private String comment;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return toStringHelper().add("key", key).add("value", value).add("comment", comment).toString();
    }
}