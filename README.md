# decision

### 介绍
decision是一款基于Spring Cloud Discovery服务注册发现、Ribbon负载均衡、Feign和RestTemplate调用等组件全方位增强的微服务解决方案，可通过设置Header或者配置中心配置来定义路由策略，并将路由策略传递到全链路服务中，实现在全链路中访问指定的服务版本。
decision是基于开源的字节码框架bytebuddy开发的环境治理工具，通过javaagent字节码织入技术实现，对业务代码完全透明无侵入，业务代码无需做修改即可实现服务的自定义路由，借助于类隔离机制，保证了业务代码的安全性，从而实现高效稳定的微服务环境治理方案

### 目标群体
- 对于多团队开发多个服务时，每个团队都需要自己的一套开发环境进行及时的验证测试，导致开发环境资源和维护成本过高
- 开发人员平时想在本地进行调试时，需服务注册到注册中心，且并不是每次请求都能到达本地，而且还影响其他人使用
- 需要对服务进行灰度时，不知如何选择服务版本

### 源码
- github：https://github.com/IRONMAN-KD/decision
- 码云：https://gitee.com/lkd_java/decision

### 工程架构
#### 架构核心
![总体路由策略](https://images.gitee.com/uploads/images/2021/0220/162333_67c01b44_687406.png "总体架构.png")


### 安装教程

1.  下载代码 ，git clone https://github.com/IRONMAN-KD/decision.git
2.  打包
~~~shell
cd decision/decision-agent
mvn package
cd decision/decision-plugin
mvn package
~~~
打包后会生成对应的文件在“decision-agent-bin”目录里

3.  vm参数配置
添加启动参数
~~~shell
-javaagent:/home/decision-agent-bin/decision-agent.jar
~~~
### 使用说明

#### 1.  decision-agent-bin 目录文件说明
- decision-agent.jar 核心jar包，agent的入口
- cfg目录，包含decision-logback.xml日志配置文件，后续会添加decision相关的配置
- plugins目录，里面包含了需要织入的插件jar包
#### 2.  支持的微服务组件以及版本说明
##### spring cloud版本说明
| decision版本 | 分支     | Spring Cloud版本                         | Spring Boot版本                                           |
|------------|--------|----------------------------------------|---------------------------------------------------------|
| 1.0.0      | master | Hoxton.SR5 ↑ <br> Hoxton <br> Greenwich <br> Finchley | 2.3.x.RELEASE <br> 2.2.x.RELEASE <br> 2.1.x.RELEASE <br> 2.0.x.RELEASE |

##### spring cloud组件支持说明
<table>
        <tr>
	    <th>组件类型</th>
	    <th>组件名称</th>
	    <th>备注</th>  
	</tr >
	<tr >
	    <td rowspan="3">注册中心</td>
	    <td>nacos</td>
	    <td>已支持</td>
	</tr>
        <tr >
	    <td>Eureka（开发中）</td>
	    <td>已在开发准备中</td>
	</tr>
        <tr>
	    <td>Consul（开发中）</td>
	    <td>已在开发准备中</td>
	</tr>
        <tr >
	    <td rowspan="2">服务网关</td>
	    <td>gateway</td>
	    <td>已支持</td>
	</tr>
        <tr>
	    <td>Zuul（计划中）</td>
	    <td>计划中</td>
	</tr>
        <tr >
	    <td rowspan="2">消息中间件</td>
	    <td>RabbitMq</td>
	    <td>已支持广播模式下根据header选择消费者</td>
	</tr>
        <tr>
	    <td>kafaka(计划中）</td>
	    <td>计划中</td>
	</tr>
</table>

#### 3.如何使用

- 在启动时添加VM参数
~~~shell
-javaagent:/home/decision-agent-bin/decision-agent.jar
~~~

- 需要在nacos的配置中添加metadata的配置， **注意：当header中没有指定服务版本以及环境时，默认会选择common版本和环境进行路由，所以服务必须要有一个common版本存在** 
~~~shell
metadata:
   version: common
   env: common
~~~
![metaData](https://images.gitee.com/uploads/images/2021/0222/105717_6596c11e_687406.png "屏幕截图.png")

### 路由策略
#### 1.version路由策略
![metaData](https://images.gitee.com/uploads/images/2021/0222/133611_9029d116_687406.png "version路由策略 (1).png")

##### 策略说明：
- 通过header中配置对应服务的version信息进行服务版本的路由选择，目前有两种方式添加header信息，①通过网关的配置添加配置参数decision.header.version来配置 ②通过在外部系统的http请求中（例如在Nginx中统一配置、在前端工程中统一配置、或者在postman等工具中配置）设置header值vd-version来配置，如果两种方式都配置的话，外部系统配置优先级大于网关配置中的配置
- 当请求链路中的服务未在header中指定版本，则默认是选择common版本（所以每个服务都需要指定common版本），如为配置common版本并且header中未指定版本会导致找不到对应的服务

#### 2.env路由策略
![env路由策略](https://images.gitee.com/uploads/images/2021/0222/144310_59653cf2_687406.png "env路由策略.png")

##### 策略说明：
- 通过header中配置对应服务的env信息进行服务的路由选择，目前有两种方式添加header信息，①通过网关的配置添加配置参数decision.header.env来配置 ②通过在外部系统的http请求中（例如在Nginx中统一配置、在前端工程中统一配置、或者在postman等工具中配置）设置header值vd-env来配置，如果两种方式都配置的话，外部系统配置优先级大于网关配置中的配置
- 当请求链路中的服务未在header中指定环境或者未找到指定的环境，则默认是选择common环境，如为配置common版本并且header中未指定版本会导致找不到对应的服务

### 交流与反馈
微信群、钉钉群、


### 特别说明
目前还在不断更新迭代中，有兴趣的朋友可以一起参与进来！！

