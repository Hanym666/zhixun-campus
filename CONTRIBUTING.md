# 协作开发规范

## 分支流程

1. 开始任务前更新 `develop`：`git switch develop && git pull`。
2. 从 `develop` 创建分支：`git switch -c feature/任务名称`。
3. 小步提交，提交信息使用 `类型(范围): 描述`。
4. 推送功能分支并向 `develop` 创建 Pull Request。
5. 至少一名组员审查且 CI 通过后合并；不要直接向 `main` 推送。

常用提交类型：

- `feat`：新功能
- `fix`：缺陷修复
- `docs`：文档
- `refactor`：不改变行为的重构
- `test`：测试
- `chore`：构建或工具调整

示例：`feat(web): add login page`、`fix(item): validate image type`。

## 前后端接口协作

- 前端只请求网关的 `/api/**` 和 `/uploads/**`。
- 新增或修改接口时，同一个 Pull Request 必须同步更新 `docs/API.md`。
- 时间统一使用 ISO 8601，例如 `2026-07-16T10:30:00`。
- 枚举值使用后端定义的大写英文值，不在前端自行改名。
- 请求需要登录时使用 `Authorization: Bearer <accessToken>`。
- 不把密码、Token、真实个人信息或 `.env` 提交到仓库。

## 合并前检查

```powershell
mvn clean test
git diff --check
git status
```

前端目录加入后还应执行：

```powershell
npm run build
npm run lint
```
