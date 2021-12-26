# clrpc

这是一个基于 `Java` 、 由 **Netty** 负责传输 、默认使用 **Protostuff** 负责编解码的简单的RPC(远程过程调用)工具。

服务提供者将服务发布注册到 注册中心 **ZooKeeper** 上后，服务消费者请求 注册中心 **ZooKeeper** 查找订阅服务后与服务提供者通信调用服务( 支持 *同步服务* 和 *异步服务* )。

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://apache.org/licenses/LICENSE-2.0)

## Setup

当前阶段均为 `SNAPSHOT` 版本，暂时不提供依赖配置。

你可以进入 [Release页面](https://github.com/CongLinDev/clrpc/releases/latest) 下载jar包使用，或是使用命令 `git clone git@github.com:CongLinDev/clrpc.git` 克隆到本地。

## Usage

### Define Service And Implement it

```java
// define a service interface
interface HelloService {
    String hello(String arg);
    String hi(String arg);
}

// implements interface HelloService
class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String arg) {
        return "Hello " + arg;
    }

    @Override
    public String hi(String arg) {
        return "Hi " + arg;
    }
}
```

### Service Provider

```java
// 创建服务提供者
RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();

// 创建服务对象
ServiceObject serviceObject = new SimpleServiceObject.Builder()
        .name("HelloService")
        .object(new HelloServiceImpl())
        .build();

// 发布服务并开启服务
bootstrap.publish(serviceObject) // 发布享元模式的服务对象
        .hookStop() // 注册关闭钩子，用于优雅关闭服务提供者
        .start(new CommonOption());
```

### Service Consumer

```java
// 创建服务消费者
RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
// 开启服务消费者
bootstrap.start(new CommonOption());
// 创建服务接口对象
ServiceInterface<HelloService> serviceInterface = new SimpleServiceInterface.Builder<HelloService>()
        .name("HelloService")
        .interfaceClass(HelloService.class)
        .build();

// 提前刷新需要订阅的服务
bootstrap.subscribe(serviceInterface);

//使用同步服务
HelloService syncService = bootstrap.proxy(serviceInterface, false);
String result = syncService.hello("I am consumer!"); // 一直阻塞，直到返回结果

// 使用异步服务
HelloService asyncService = bootstrap.proxy(serviceInterface, true);
String fakeResult = asyncService.hello("I am consumer!"); // 直接返回默认值
assert fakeResult == null;
RpcFuture future = AsyncObjectProxy.lastFuture(); // 获取该线程最新一次操作的产生的future对象
future.callback(new Callback(){ // 使用回调处理结果
    @Override
    public void success(Object res) {}
    @Override
    public void fail(Exception e) {}
});

// 关闭服务消费者
bootstrap.stop();
```

### Service Consumer (With Transaction)

```java
// 创建服务消费者
RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
// 开启服务消费者
bootstrap.start(new CommonOption());
// 创建服务接口对象
ServiceInterface<HelloService> serviceInterface = new SimpleServiceInterface.Builder<HelloService>()
        .name("HelloService")
        .interfaceClass(HelloService.class)
        .build();
// 提前刷新需要订阅的服务
bootstrap.subscribe(serviceInterface);

TransactionProxy proxy = (TransactionProxy)bootstrap.proxy(ZooKeeperTransactionProxy.class);
HelloService service = proxy.proxy(serviceInterface);

proxy.begin(); // 事务开启

service.hello("first request"); // 异步发送第一条请求
RpcFuture f1 = AsyncObjectProxy.lastFuture(); // 获取第一条请求产生的future对象
service.hi("second request"); // 异步发送第二条请求
RpcFuture f2 = AsyncObjectProxy.lastFuture(); // 获取第二条请求产生的future对象

RpcFuture future = proxy.commit(); // 事务提交 返回事务 Future

// 关闭服务消费者
bootstrap.stop();
```

## Architecture

![architecture.png](https://i.loli.net/2020/01/21/63Ea7nbxez5Hkmd.png)

## Config

默认配置文件名为 `config.properties`。

[默认配置文件模板](https://github.com/CongLinDev/clrpc/blob/master/src/main/resources/config.properties)。

### Config File

配置文件位置默认在项目 `resources` 目录下，默认格式为 `properties` ，默认文件为 `config.properties`。

### Config Items

|              Field               |  Type   | Required | Default |                         Remark                          |
| :------------------------------: | :-----: | :------: | :-----: | :-----------------------------------------------------: |
|           registry.url           | String  |   True   |         |                      注册中心地址                       |
|     registry.register-class      | String  |   True   |         |                        注册类名                         |
|     registry.discovery-class     | String  |   True   |         |                        发现类名                         |
|          provider.port           | Integer |  False   |    0    |                    服务提供者端口号                     |
|       provider.thread.boss       | Integer |  False   |    1    |               服务提供者的bossGroup线程数               |
|      provider.thread.worker      | Integer |  False   |    4    |              服务提供者的workerGroup线程数              |
| provider.channel.handler-factory | String  |  False   | `null`  |      实现ChannelHandlerFactory，可自定义添加处理器      |
|      consumer.thread.worker      | Integer |  False   |    4    |              服务使用者的workerGroup线程数              |
|   consumer.retry.check-period    | Integer |  False   |  3000   |           重试机制执行周期(非正数代表不开启)            |
| consumer.retry.initial-threshold | Integer |  False   |  3000   |                    初始重试时间门槛                     |
|   consumer.fallback.max-retry    | Integer |  False   |   -1    | 降级机制允许重试最大的次数(负数代表不开启，0代表不重试) |
| consumer.channel.handler-factory | String  |  False   | `null`  |      实现ChannelHandlerFactory，可自定义添加处理器      |
|  service.thread-pool.core-size   | Integer |  False   |    5    |                  业务线程池核心线程数                   |
|   service.thread-pool.max-size   | Integer |  False   |   10    |                  业务线程池最大线程数                   |
|  service.thread-pool.keep-alive  | Integer |  False   |  1000   |    线程池多余空闲线程在终止之前等待新任务的最长时间     |
|    service.thread-pool.queue     | Integer |  False   |   10    |                    业务线程池队列数                     |

### Extension config Items

|          Field          |  Type  | Required | Default |             Remark             |
| :---------------------: | :----: | :------: | :-----: | :----------------------------: |
|  extension.logger.url   | String |   True   |         | 日志中心地址，目前用于记录日志 |
| extension.atomicity.url | String |   True   |         | 原子服务地址，目前用于事务管理 |

#### About customized address url

配置项中的 `registry.url` 为必填项，其url解析规则如下：

`zookeeper://127.0.0.1:2181/clrpc?session-timeout=5000`

1. 协议名，如 `zookeeper`；
2. 服务地址部分，如 `127.0.0.1:2181`；
3. 根节点部分，如 `/clrpc` （若未给出默认为 `/` ）；
4. 参数部分，如 `session-timeout=5000` 。

#### About customized channel handler

[Click me](#Extension).

## Distributed transaction

**clrpc** 使用 **ZooKeeper** 实现了类似于两段式提交(2PC)的分布式事务协调服务。

注意：该服务仅支持 返回值为 `conglin.clrpc.extension.transaction.TransactionResult` 及其子类的服务方法。不满足条件的服务方法执行方式与普通调用相同。

1. 阶段一 执行服务方法，并返回 `conglin.clrpc.extension.transaction.TransactionResult` 对象。并通过 `conglin.clrpc.extension.transaction.TransactionResult#result()` 方法获取一阶段提交结果，返回给调用者。
2. 阶段二 从 `conglin.clrpc.extension.transaction.TransactionResult#callback()` 获取 `conglin.clrpc.common.Callback` 对象。根据实际需求执行 `conglin.clrpc.common.Callback#success(Object)` 或 `conglin.clrpc.common.Callback#fail(Exception)` 方法。

流程图如下：

![distributed.png](https://i.loli.net/2021/10/16/loCg5LTF2uitGMh.png)

## Extension

**clrpc** 利用了 **Netty** 的 `ChannelPipeline` 作为处理消息的责任链，并提供消息处理扩展点。

使用者实现接口 `conglin.clrpc.service.handler.factory.ChannelHandlerFactory`，并声明在配置文件中，即可完成对消息处理的扩展。

在创建 `conglin.clrpc.service.handler.factory.ChannelHandlerFactory` 对象时，必须提供一个无参构造函数。该对象可以通过实现 `conglin.clrpc.service.context.ContextAware` 接口来实现 `conglin.clrpc.service.context.RpcContext` 的注入。

## License

[Apache 2.0](http://apache.org/licenses/LICENSE-2.0)
