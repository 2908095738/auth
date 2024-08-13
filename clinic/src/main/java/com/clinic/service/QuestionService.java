package com.clinic.service;

import com.clinic.entity.Question;

import java.util.List;

/**
 *
 */
public interface QuestionService{

    List<Question> search(String val);
}
