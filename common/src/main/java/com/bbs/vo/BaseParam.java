package com.bbs.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseParam {

    private Long current;

    private Long size;

    public <T> Page<T> toPage() {
        return new Page<>(current, size);
    }
}
