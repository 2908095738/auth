package com.auth.api.auth.company.staff;

import java.util.Set;

public interface SearchChildStaff {

    Set<ChildStaff> search(Long uid, Long companyID);
}
