package lucky.apollo.entity.po;

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
@Table(name = "Role")
@SQLDelete(sql = "Update Role set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class RolePO extends BasePO {
    @Column(name = "RoleName", nullable = false)
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}