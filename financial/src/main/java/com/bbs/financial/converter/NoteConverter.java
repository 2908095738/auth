package com.bbs.financial.converter;

import com.bbs.financial.api.note.add.AddNote;
import com.bbs.financial.api.note.update.UpdateNote;
import com.bbs.financial.entity.Note;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteConverter {
    Note toEntity(AddNote.Param param);

    Note toEntity(UpdateNote.Param param);
}