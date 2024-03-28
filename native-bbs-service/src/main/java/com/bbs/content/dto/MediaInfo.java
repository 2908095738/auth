package com.bbs.content.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 媒体信息
 * @author Gao
 *
 */
@Data
public class MediaInfo implements Serializable{
	private static final long serialVersionUID = -4032600666415370715L;

	/** 媒体文件URL **/
	private String mediaUrl;
	/** 封面文件 **/
	private String cover;
	
	/** 缩略图文件 **/
	private String thumbnail;
	
	
	
}
