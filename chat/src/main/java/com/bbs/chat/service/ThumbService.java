package com.bbs.chat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bbs.Result;
import com.bbs.chat.dto.param.CancelThumbParam;
import com.bbs.chat.entity.Thumb;

public interface ThumbService extends IService<Thumb> {
    Result createThumb(Thumb thumb);

    Result cancelThumb(CancelThumbParam cancelThumbParam);
}