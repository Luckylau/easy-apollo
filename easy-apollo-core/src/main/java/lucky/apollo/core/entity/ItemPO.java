package lucky.apollo.core.entity;


import lucky.apollo.common.entity.po.BasePO;

import javax.persistence.Column;
import javax.persistence.Lob;

/**
 * @Author luckylau
 * @Date 2019/7/4
 */
public class ItemPO extends BasePO {

    @Column(name = "NamespaceId", nullable = false)
    private long namespaceId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value")
    @Lob
    private String value;

    @Column(name = "comment")
    private String comment;

    @Column(name = "LineNum")
    private Integer lineNum;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(long namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return toStringHelper().add("namespaceId", namespaceId).add("key", key).add("value", value)
                .add("lineNum", lineNum).add("comment", comment).toString();
    }
}