package com.bbs.financial.converter;

import com.bbs.financial.api.asset.SearchAssetList;
import com.bbs.financial.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AssetConverter {

    @Mappings({
            @Mapping(target = "assetsCleanTime", ignore = true)
    })
    Asset toEntity(SearchAssetList.Param param);
}
