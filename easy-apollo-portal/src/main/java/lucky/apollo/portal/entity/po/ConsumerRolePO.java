package lucky.apollo.portal.entity.po;


import lucky.apollo.common.entity.po.BasePO;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author luckylau
 * @Date 2019/7/18
 */
@Entity
@Table(name = "ConsumerRole")
@SQLDelete(sql = "Update ConsumerRole set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class ConsumerRolePO extends BasePO {
    @Column(name = "ConsumerId", nullable = false)
    private long consumerId;

    @Column(name = "RoleId", nullable = false)
    private long roleId;

    public long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return toStringHelper().add("consumerId", consumerId).add("roleId", roleId).toString();
    }
}