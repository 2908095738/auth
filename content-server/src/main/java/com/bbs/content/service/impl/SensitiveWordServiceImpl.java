package com.bbs.content.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.dfa.WordTree;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.content.entity.SensitiveWord;
import com.bbs.content.service.SensitiveWordService;
import com.bbs.content.mapper.SensitiveWordMapper;
import com.bbs.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

/**
* @author 路晨霖
* @description 针对表【sensitive_word(敏感词汇)】的数据库操作Service实现
* @createDate 2024-05-05 17:08:04
*/
@Slf4j
@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
    implements SensitiveWordService, ApplicationListener<ApplicationReadyEvent> {

    private static volatile WordTree badWordTree = null;

    /**
     * 读取数据库最新【敏感关键字】加载到服务缓存
     */
    private void load() {
        try {
            log.info("【敏感关键字】开始加载...");
            TimeInterval timer = DateUtil.timer();
            List<String> badWords = listObjs(new QueryWrapper<SensitiveWord>().lambda().select(SensitiveWord::getBadword), Object::toString);
            WordTree tree = new WordTree();
            tree.addWords(badWords);
            badWordTree = tree;
            log.info("【敏感关键字】加载完成！！！！数量={}; 耗时={};", badWords.size(), timer.interval());
        } catch (Exception e) {
            log.error("【敏感关键字】加载失败！！！");
            e.printStackTrace();
            throw new BusinessException(500, e.getMessage());
        }
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        reload(true);
    }

    @Override
    public void tryReload() {
        reload(false);
    }

    @Override
    public Boolean reload(Boolean direct) {
        if(direct) {
            synchronized (SensitiveWordService.class) {
                load();
            }
        } else {
            if(isNull(badWordTree)) {
                synchronized (SensitiveWordService.class) {
                    if(isNull(badWordTree)) {
                        load();
                        return true;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Boolean match(String text) {
        tryReload();
        return badWordTree.isMatch(text);
    }

    @Override
    public Boolean notMatch(String text) {
        return !match(text);
    }
}




