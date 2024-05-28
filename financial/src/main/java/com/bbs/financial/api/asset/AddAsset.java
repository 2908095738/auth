package com.bbs.financial.api.asset;

import com.bbs.Result;
import com.bbs.financial.entity.Asset;
import com.bbs.financial.service.AssetService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import static com.bbs.Result.success;


@RestController
public class AddAsset {



    @Resource
    private AssetService assetService;



    /**
     * 新增资产
     */
    @PostMapping("/asset")
    public Result<Boolean> add(@RequestBody Asset asset)
    {

//        assetType.setCreateBy(LoginUser.getId());
//        assetType.setUpdateBy(LoginUser.getId());
        assetService.save(asset);
        return success();
    }



}
