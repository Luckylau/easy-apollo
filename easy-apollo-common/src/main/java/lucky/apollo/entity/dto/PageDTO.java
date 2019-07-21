package lucky.apollo.entity.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Author luckylau
 * @Date 2019/7/16
 */
@Getter
@Setter
public class PageDTO<T> {
    private final long total;
    private final List<T> content;
    private final int page;
    private final int size;

    public PageDTO(List<T> content, Pageable pageable, long total) {
        this.total = total;
        this.content = content;
        this.page = pageable.getPageNumber();
        this.size = pageable.getPageSize();
    }
}