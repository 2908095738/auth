package com.bbs.api.auth.company;

import com.bbs.vo.Company;

import java.util.Collection;
import java.util.List;

public interface CompanyAPI {

    List<Company> list(Collection<Long> ids);
}
