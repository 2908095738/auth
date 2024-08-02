package com.clinic.cache.auxiliary.type;

import com.clinic.entity.AuxiliaryType;

import java.text.ParseException;
import java.util.List;

public interface AuxiliaryTypeCache {

    List<AuxiliaryType> get() throws InterruptedException, ParseException;

    boolean set(String name);

}
