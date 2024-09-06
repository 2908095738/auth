package com.clinic.cache.inform;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.clinic.entity.Inform;
import com.clinic.enums.RedisKeys;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InformCache {

    @Resource(name = "protoStuffTemplate")
    private RedisTemplate<String, String> redis;

    /**
     * 获取所有消息
     * @param userId 用户ID
     * @return List<Inform>
     */
    public List<Inform> getInformList(Long userId) {
        List<Inform> result = new ArrayList<>();
        List<String> notReadInformIds = new ArrayList<>();
        if(ObjUtil.isEmpty(userId)){//获取所有消息
            notReadInformIds = redis.opsForList().range(informKey(), 0, -1);
        }else{//获取指定用户的未读消息
            String userIsReadInformId = informUserKey(userId);
            //没有数据说明是新用户，需修改为最新的消息
            if(ObjUtil.isEmpty(userIsReadInformId)){
                Long size = redis.opsForList().size(informKey());
                redis.opsForHash().put(informKey(), informUserKey(userId), size.toString());
            }else{
                notReadInformIds = redis.opsForList().range(informKey(), (Long.parseLong(userIsReadInformId)-1),-1);
            }
        }
        if(CollUtil.isNotEmpty(notReadInformIds)){
            result = notReadInformIds.stream().map(o -> JSONUtil.toBean(o, Inform.class)).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * 添加消息
     * @param inform 消息
     */
    public void putInform(Inform inform) {
        redis.opsForList().rightPush(informKey(), JSONUtil.toJsonStr(inform));
    }

    /**
     * 更新用户已读消息
     * @param userId 用户ID
     */
    public void updateUserReadInform(Long userId) {
        Long size = redis.opsForList().size(informKey());
        redis.opsForHash().put(informKey(), informUserKey(userId), size.toString());
    }


    public String informUserKey(Long userId) {
        return RedisKeys.INFORM.key(userId);
    }

    public String informKey() {
        return RedisKeys.INFORM_USER.getDescription();
    }


}
