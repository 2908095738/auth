package com.bbs.financial.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bbs.financial.entity.Note;
import com.bbs.financial.service.NoteService;
import com.bbs.financial.mapper.NoteMapper;
import org.springframework.stereotype.Service;

/**
* @author Mafty
* @description 针对表【note(日记账)】的数据库操作Service实现
* @createDate 2024-06-29 15:36:24
*/
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note>
    implements NoteService{

}




