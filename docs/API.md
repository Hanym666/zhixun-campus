# 前后端接口约定

## 通用约定

- 开发地址：`http://localhost:8080`
- 请求和响应编码：UTF-8
- JSON 请求：`Content-Type: application/json`
- 登录鉴权：`Authorization: Bearer <accessToken>`
- 成功业务码：`0`

统一响应：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "timestamp": 1784131200000
}
```

分页数据包含 `records`、`page`、`size`、`total` 和 `pages`。时间字段使用 `yyyy-MM-dd'T'HH:mm:ss`。

## 认证与用户

| 方法 | 地址 | 登录 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 否 | 注册 |
| POST | `/api/auth/login` | 否 | 登录并返回 access/refresh token |
| POST | `/api/auth/refresh` | 否 | 使用 refreshToken 换取新令牌 |
| POST | `/api/auth/logout` | 是 | 注销 refreshToken |
| GET | `/api/users/me` | 是 | 当前用户资料 |

注册字段：`username`、`password`、`realName`、`identityType`、`studentStaffNo`、`phone?`、`email?`。`identityType` 为 `STUDENT` 或 `STAFF`。

## 物品与分类

| 方法 | 地址 | 登录 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/categories` | 否 | 分类列表 |
| GET | `/api/items` | 否 | 公开物品分页查询 |
| GET | `/api/items/{id}` | 可选 | 物品详情；发布者可看到私密特征 |
| GET | `/api/items/mine` | 是 | 我的发布 |
| POST | `/api/items` | 是 | 创建物品帖 |
| PUT | `/api/items/{id}` | 是 | 修改草稿或寻找中的帖子 |
| POST | `/api/items/{id}/publish` | 是 | 发布帖子 |
| DELETE | `/api/items/{id}` | 是 | 删除帖子 |
| GET | `/api/items/{id}/matches` | 是 | 智能匹配结果 |
| POST | `/api/items/{id}/reindex` | 是 | 重新建立搜索与向量索引 |

创建物品主要字段：`itemType`、`title`、`categoryId`、`itemName`、`description`、`publicFeatures?`、`privateFeatures?`、`occurredAt?`、`campusArea?`、`locationName?`、`color?`、`brand?`、`contactHint?`、`publishNow?`。`itemType` 为 `LOST` 或 `FOUND`。

列表常用查询参数：`page`、`size`、`itemType`、`status`、`categoryId`、`keyword`、`campusArea`。

## 图片

| 方法 | 地址 | 登录 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/items/{postId}/images` | 是 | `multipart/form-data` 上传，字段名 `file` |
| DELETE | `/api/items/{postId}/images/{imageId}` | 是 | 删除图片 |
| GET | `/uploads/items/{fileName}` | 否 | 获取图片文件 |

仅支持真实 PNG/JPEG，单张不超过 5 MB，每个帖子最多 6 张。第一张图片自动成为封面。

## 认领与会话

| 方法 | 地址 | 登录 | 说明 |
| --- | --- | --- | --- |
| POST | `/api/claims` | 是 | 提交认领，字段 `postId`、`evidence` |
| GET | `/api/claims/mine` | 是 | 我发起的认领 |
| GET | `/api/claims/received` | 是 | 我的帖子收到的认领 |
| POST | `/api/claims/{id}/review` | 是 | 发布者审核认领 |
| POST | `/api/claims/{id}/complete` | 是 | 确认完成交接 |
| POST | `/api/claims/{id}/cancel` | 是 | 取消认领 |
| POST | `/api/claims/{claimId}/messages` | 是 | 发送消息，字段 `content` |
| GET | `/api/claims/{claimId}/messages` | 是 | 会话消息，支持 `beforeId`、`limit` |

审核字段：`approved`、`reviewComment?`、`handoverLocation?`、`handoverAt?`。

## 通知

| 方法 | 地址 | 登录 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/notifications` | 是 | 通知列表 |
| POST | `/api/notifications/{id}/read` | 是 | 标记已读 |

## AI 内部接口

`/api/ai/items/**` 主要供物品服务调用，前端通常不应直接调用。

## 联调规则

接口字段以代码 DTO 为最终依据。前端遇到非 `0` 的业务码时显示 `message`；HTTP 401 清理本地登录态，HTTP 403 显示无权限，HTTP 404 显示资源不存在。
