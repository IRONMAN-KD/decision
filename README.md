# decision
#### 目标群体
- 对于多团队开发多个服务时，每个团队都需要自己的一套开发环境进行及时的验证测试，导致开发环境资源和维护成本过高
- 开发人员平时想在本地进行调试时，需服务注册到注册中心，且并不是每次请求都能到达本地，而且还影响其他人使用
- 需要对服务进行灰度时，不知如何选择服务版本
#### 介绍
decision是一款基于Spring Cloud Discovery服务注册发现、Ribbon负载均衡、Feign和RestTemplate调用等组件全方位增强的微服务解决方案，可通过设置Header或者配置中心配置来定义路由策略，并将路由策略传递到全链路服务中，实现在全链路中访问指定的服务版本。
decision是基于开源的字节码框架bytebuddy开发的环境治理工具，通过javaagent字节码织入技术实现，对业务代码完全透明无侵入，业务代码无需做修改即可实现服务的自定义路由，借助于类隔离机制，保证了业务代码的安全性，从而实现高效稳定的微服务环境治理方案
#### 软件架构



#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
