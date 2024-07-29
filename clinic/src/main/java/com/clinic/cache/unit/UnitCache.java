package com.clinic.cache.unit;

import com.clinic.entity.Unit;

import java.util.List;
import java.util.Map;

public interface UnitCache {

    List<String> getUnitStrList(List<String> ids);

    Unit getUnit(Integer id);

    List<Unit> getUnit(List<Integer> ids);

    Map<Integer, Unit> getUnitMap(List<Integer> ids);

    void reload();

    String generateKey(Integer id);

    List<Unit> search(String name);
}
