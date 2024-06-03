package com.bbs.financial.converter;

import com.bbs.financial.api.asset.AddAsset;
import com.bbs.financial.api.asset.SearchAssetList;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.entity.AssetAccountCertificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AssetConverter {

    @Mappings({
        @Mapping(target = "assetAccountCertificate", ignore = true),
        @Mapping(target = "numUnit", ignore = true),
        @Mapping(target = "assetType", ignore = true),
        @Mapping(target = "companyStructure", ignore = true),
        @Mapping(target = "useUser", ignore = true),
        @Mapping(target = "createUser", ignore = true),
        @Mapping(target = "updateUser", ignore = true)
    })
    Asset toEntity(AddAsset.Param param);
    Asset toEntity(SearchAssetList.Param param);

    @Mappings({
            @Mapping(target = "id", ignore = true),
    })
    AssetAccountCertificate toAccountCertificateEntity(AddAsset.Param param);
}
