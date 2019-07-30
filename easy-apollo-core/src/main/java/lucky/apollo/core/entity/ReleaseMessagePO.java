package lucky.apollo.core.entity;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author luckylau
 * @Date 2019/7/25
 */
@Entity
@Table(name = "ReleaseMessage")
public class ReleaseMessagePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private long id;

    @Column(name = "Message", nullable = false)
    private String message;

    @Column(name = "DataChange_LastTime")
    private Date dataChangeLastModifiedTime;

    public ReleaseMessagePO() {
    }

    public ReleaseMessagePO(String message) {
        this.message = message;
    }

    @PrePersist
    protected void prePersist() {
        if (this.dataChangeLastModifiedTime == null) {
            dataChangeLastModifiedTime = new Date();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("message", message)
                .add("dataChangeLastModifiedTime", dataChangeLastModifiedTime)
                .toString();
    }
}