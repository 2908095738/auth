package com.bbs.content.util;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;


@Slf4j
@Component
public class TianDiTuUtil {

    @Value("${tianditu.host}")
    private String host;

    @Value("${tianditu.api.search}")
    private String searchPath;

    @Value("${tianditu.api.timeout}")
    private Integer timeout;

    @Value("${tianditu.tk}")
    private String tk;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Result<T> {

        @ApiModelProperty("编码")
        private Integer status;

        @ApiModelProperty("数据")
        private T result;

        @ApiModelProperty("消息")
        private String msg;

    }

    public String getCityBy(Double log,Double lat) {
        if(nonNull(log)&&nonNull(lat)) {
            String serverHost = host + searchPath+"?type=geocode&tk="+tk+"&postStr={'lon':"+log+",'lat':"+lat+",'ver':1}";
            try {
                HttpResponse response = HttpRequest.get(serverHost).timeout(timeout).execute();
                if(response.isOk()) {
                    String body = response.body();
                    Result result = JSONUtil.toBean(body, Result.class);
                    if(0 == result.getStatus()) {
                        Object data = result.getResult();
                        if(nonNull(data)) {
                            JSONObject jsonObject = JSONUtil.parseObj(data);
                            Object addressComponent = jsonObject.get("addressComponent");
                            if(nonNull(addressComponent)) {
                                JSONObject jsonAddressComponent = JSONUtil.parseObj(addressComponent);
                                String city = jsonAddressComponent.get("city").toString();
                                if(StringUtils.isEmpty(city)) {
                                    return jsonAddressComponent.get("county").toString();
                                }{
                                    return city;
                                }
                            }
                        }
                    }
                }
                return null;
            } catch (HttpException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


}
