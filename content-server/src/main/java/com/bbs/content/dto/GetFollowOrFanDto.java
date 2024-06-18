package com.bbs.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetFollowOrFanDto {

    private Long userId;

    private String userName;

    private String url;

}
