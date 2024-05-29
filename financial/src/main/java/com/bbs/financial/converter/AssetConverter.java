package com.bbs.financial.converter;

import com.bbs.financial.api.asset.AddAsset;
import com.bbs.financial.entity.Asset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssetConverter {

    Asset toEntity(AddAsset.Param param);
}
