package com.bbs.financial.api.count.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundBalance {

    private Long sum;

    Map<String, Long> items;
}
