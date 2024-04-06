### 创建新用户
#### Reuqest

- Method: **PUT**
- URL: ```/chat/createChat```
- Headers： Content-Type:application/json
- Body:
```
{
    "sendUid" : "消息发送用户id",
    "acceptUid" : "消息接收用户id",
    "contentType" : "消息类型:1.文本消息;2.图片消息;3.视频消息",
    "content" : "消息内容;如果是图片、视频就存入url",
    "time" : "消息发送时间"
}
```

#### Response
- Body
```
{
  "code": 200,
  "data": "true",
  "msg": "success"
}
```

### 获取消息页顶部的点赞/收藏、关注、评论角标(2/3)
#### Reuqest

- Method: **GET**
- URL: ```/chat/getTop```
- Body:
```
{
    "userId" : "用户id"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "agreeCount": "点赞、收藏角标",
        "fanCount": "关注角标",
        "commentCount": "评论角标"
    },
    "msg": "success"
}
```

### 获取消息页下方消息列表
#### Reuqest

- Method: **GET**
- URL: ```/chat/getChat```
- Body:
```
{
    "userId" : "用户id",
    "current":"第几页",
    "size":"几条"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "lastList": {
            "records": [
                {
                    "sendName": "发送方用户名称",
                    "contentLast": "最新消息",
                    "avatarPath": "发送方头像路径",
                    "count": 未读消息数量：0：已读最新消息 >0：未读数条最新消息,
                    "timeLast": "最新消息时间",
                    "sendUid": 发送方用户id,
                    "id": 未读消息唯一标识符,
                    "contentType": 未读消息类型，如果是图片、视频以文字提示点击进入聊天记录查看；
                    1.文本消息；2.图片消息；3.视频消息
                },
                {
                    ...
                }
            ],
            "total": 2,
            "size": 9,
            "current": 1,
            "pages": 1
        }
    },
    "msg": "success"
}
```

### 获取聊天记录
#### Reuqest

- Method: **GET**
- URL: ```/chat/getRecord```
- Body:
```
{
    "sendUid" : "发送方用户id",
    "acceptUid" : "接收方用户id",
    "current":"第几页",
    "size":"几条"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "records": [
            {
                "id": 消息唯一标识符,
                "chatUid": 发送消息的用户id(通过请求参数中的sendUid、acceptUid判断发送方),
                "contentType": 消息类型：1.文本消息；2.图片消息；3.视频消息,
                "content": "消息内容",
                "time": "消息时间"
            },
            {
                ...
            }
        ],
        "total": 4,
        "size": 4,
        "current": 1,
        "pages": 1
    },
    "msg": "success"
}
```

### 获取点赞、收藏列表
#### Reuqest

- Method: **GET**
- URL: ```/chat/getAgree```
- Body:
```
{
    "userId" : "用户id",
    "current":"第几页",
    "size":"几条"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "records": [
            {
                "agreeId": 点赞、收藏id,
                "agreeUid": 点赞、收藏用户id,
                "nickName": "点赞、收藏用户呢称",
                "avatarPath": "点赞、收藏用户头像路径",
                "type": 点赞/收藏标识符:1：点赞文章;2：点赞评论;3：收藏文章,
                "time": "点赞/收藏时间"
            }
        ],
        "total": 1,
        "size": 4,
        "current": 1,
        "pages": 1
    },
    "msg": "success"
}
```

### 获取关注
#### Reuqest

- Method: **GET**
- URL: ```/chat/getFan```
- Body:
```
{
    "userId" : "用户id",
    "current":"第几页",
    "size":"几条"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "records": [
            {
                "fanUid": 4,
                "nickName": "Four4",
                "avatarPath": "F4.png",
                "time": "2024-04-11T09:14:20.000+00:00",
                "type": 0
            },
            {
                "fanUid": 新增关注用户id,
                "nickName": "新增关注呢称",
                "avatarPath": "新增关注像路径",
                "time": "新增关注时间",
                "type": 类型：0粉丝 3互关
            }
        ],
        "total": 2,
        "size": 9,
        "current": 1,
        "pages": 1
    },
    "msg": "success"
}
```

### 获取评论
#### Reuqest
- Method: **GET**
- URL: ```/chat/getComm```
- Body:
```
{
    "userId" : "用户id",
    "current":"第几页",
    "size":"几条"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "records": [
            {
                "commUid": 评论用户id,
                "nickName": "评论用户呢称",
                "avatarPath": "评论头像路径",
                "commId": 评论文章/回复评论id,
                "type": 评论类型:1.评论文章;2.回复评论,
                "time": "评论时间"
            },
            {
                ...
            }
        ],
        "total": 1,
        "size": 4,
        "current": 1,
        "pages": 1
    },
    "msg": "success"
}
```

### 获取关注用户昵称
#### Reuqest
- Method: **GET**
- URL: ```/chat/getNick```
- Body:
```
{
    "userId" : "用户id",
    "current":"第几页",
    "size":"几条"
}
```

#### Response
- Body
```
{
    "code": 200,
    "data": {
        "records": [
            "关注用户昵称",
            "..."
        ],
        "total": 1,
        "size": 9,
        "current": 1,
        "pages": 1
    },
    "msg": "success"
}
```

### 互相关注
#### Reuqest
- Method: **GET**
- URL: ```/chat/toFan```
- Body:
```
{
    "sendUid" : "发送方id",
    "acceptUid":"接收方id",
    "type":"互关标识符：1.互关2.取消互关"
}
```

#### Response
- Body
```
{
  "code": 200,
  "data": "true",
  "msg": "success"
}
```

### 删除消息
#### Reuqest
- Method: **DELETE**
- URL: ```/chat/delChat```
- Body:
```
{
    "ids" : "消息id列表",
    "type":"1.消息页删除；2.聊天框删除"
}
```

#### Response
- Body
```
{
  "code": 200,
  "data": "true",
  "msg": "success"
}
```