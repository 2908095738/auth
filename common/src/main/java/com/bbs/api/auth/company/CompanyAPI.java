package com.bbs.api.auth.company;

import com.bbs.vo.Company;
import com.bbs.vo.CompanyStructure;

import java.util.Collection;
import java.util.List;

public interface CompanyAPI {

    List<Company> list(Collection<Long> ids);

    List<CompanyStructure> searchStructureNames(Long companyId, Collection<String> name);
}
