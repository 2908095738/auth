package com.bbs.financial.exception;

import com.bbs.enums.CodeEnum;
import com.bbs.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataMissingException extends BusinessException {

    private Integer code;

    private String msg;

    public DataMissingException(CodeEnum codeEnum) {
        code = codeEnum.getCode();
        msg = codeEnum.getMsg();
    }

    public DataMissingException(String msg) {
        code = 400;
        this.msg = msg;
    }
}
