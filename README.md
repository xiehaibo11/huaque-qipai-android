# 南北娱乐安卓客户端（HuaqueUi）

Android 客户端，架构方向对齐 `浙江游戏大厅`（Cocos2dx-Lua 启动链、金币场匹配、
Taizhou 台州麻将玩法等），详细设计见 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 构建

```sh
./gradlew :app:assembleDebug
```

## 仓库导航（南北娱乐全平台）

| 端 | 仓库地址 |
| --- | --- |
| 后端（Spring Boot / Java 21 / PostgreSQL） | https://github.com/xiehaibo11/huaque-qipai-backend |
| 前端官网（Vue 3 / TypeScript / Vite） | https://github.com/xiehaibo11/huaque-qipai-frontend |
| 安卓客户端（Android，架构对齐浙江游戏大厅） | https://github.com/xiehaibo11/huaque-qipai-android |
| UI 设计源（PSD 源文件 / 生图方案，Git LFS） | https://github.com/xiehaibo11/huaque-qipai-ui |
| 浙江游戏大厅逆向资料（原版设计证据） | https://github.com/xiehaibo11/zhejiang-game-hall |

克隆任意一端后，按上表地址补齐其余仓库即可组成完整工作区；各仓库均为私有仓库，需要账号
xiehaibo11 授权访问。
