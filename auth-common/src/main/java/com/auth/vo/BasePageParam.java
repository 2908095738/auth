package com.auth.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasePageParam {

    private Integer current = 1;

    private Integer size = 10;

    public <T> Page<T> toPage() {
        return new Page<>(current, size);
    }
}
