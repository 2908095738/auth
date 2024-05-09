package com.bbs.content.service;

import com.bbs.content.entity.SensitiveWord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 路晨霖
* @description 针对表【sensitive_word(敏感词汇)】的数据库操作Service
* @createDate 2024-05-05 17:08:04
*/
public interface SensitiveWordService extends IService<SensitiveWord> {

    void tryReload();

    Boolean reload(Boolean direct);

    Boolean match(String text);

    Boolean notMatch(String text);
}
