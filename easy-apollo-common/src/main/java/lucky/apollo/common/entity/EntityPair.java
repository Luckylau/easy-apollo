package lucky.apollo.common.entity;

import lombok.Data;

/**
 * @Author luckylau
 * @Date 2019/7/22
 */
@Data
public class EntityPair<E> {

    private E firstEntity;
    private E secondEntity;

    public EntityPair(E firstEntity, E secondEntity) {
        this.firstEntity = firstEntity;
        this.secondEntity = secondEntity;
    }
}