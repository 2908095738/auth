package com.clinic.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Inform implements Serializable {

    private int id;

    private String title;

    private String content;

    private String time;

    private int type;

    private static final long serialVersionUID = 1L;

}
