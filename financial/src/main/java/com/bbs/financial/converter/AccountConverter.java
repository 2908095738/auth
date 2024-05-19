package com.bbs.financial.converter;

import com.bbs.financial.api.account.add.AddAccount;
import com.bbs.financial.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountConverter {

    Account toEntity(AddAccount.Param param);
}
