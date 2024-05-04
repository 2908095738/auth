package com.bbs.content.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片信息
 *
 */
@Data
public class ImageInfo implements Serializable{
	private static final long serialVersionUID = 5884756296465895791L;
	/** 图片路径 **/
	private String path;
	/** 图片名称 **/
	private String name;
	
}
