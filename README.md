# clrpc

这是一个基于 `Java` 、 由 **Netty** 负责传输 、默认使用 **Protostuff** 负责编解码的简单的RPC(远程过程调用)工具。

服务提供者将服务发布注册到 注册中心 **ZooKeeper** 上后，服务消费者请求 注册中心 **ZooKeeper** 查找订阅服务后与服务提供者通信调用服务( 支持 *同步服务* 和 *异步服务* )。

## Setup

当前阶段均为 `SNAPSHOT` 版本，暂时不提供依赖配置。

你可以进入 [Release页面](https://github.com/CongLinDev/clrpc/releases/latest) 下载jar包使用，或是使用命令 `git clone git@github.com:CongLinDev/clrpc.git` 克隆到本地。

## Usage

### Define Service And Implement it

```java
// define a service named 'HelloService'
@conglin.clrpc.service.annotation.Service(name = "HelloService")
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

// 发布服务并开启服务
bootstrap.publish(new HelloServiceImpl()) // 发布享元模式的服务对象
//      .publishFactory(HelloServiceImpl::new) // 发布原型模式的服务对象
        .hookStop() // 注册关闭钩子，用于优雅关闭服务提供者
        .start();
```

### Service Consumer

```java
// 创建服务消费者
RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
// 开启服务消费者
bootstrap.start();
// 提前刷新需要订阅的服务
bootstrap.subscribe(HelloService.class);

//使用同步服务
HelloService syncService = bootstrap.proxy(HelloService.class, false);
String result = syncService.hello("I am consumer!"); // 一直阻塞，直到返回结果

// 使用异步服务
HelloService asyncService = bootstrap.proxy(HelloService.class, true);
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
bootstrap.start();
// 提前刷新需要订阅的服务
bootstrap.subscribe(HelloService.class);

TransactionProxy proxy = bootstrap.transaction();
HelloService service = proxy.proxy(HelloService.class);

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

默认配置文件名为 `clrpc-config`。

[默认配置文件模板](https://github.com/CongLinDev/clrpc/blob/master/src/main/resources/clrpc-config.json)。

### Config File

配置文件位置默认在项目 `resources` 目录下，默认格式为 `json` ，默认文件为 `clrpc-config.json`。

### Config Items

|              Field               |           Type            | Required |  Default  |                         Remark                          |
| :------------------------------: | :-----------------------: | :------: | :-------: | :-----------------------------------------------------: |
|             registry             |          String           |   True   |           |                      注册中心地址                       |
|            atomicity             |          String           |   True   |           |                      原子服务地址                       |
|         meta.provider.\*         | Map&lt;String, Object&gt; |  False   | Empty Map |                  服务提供者通用元信息                   |
|         meta.consumer.\*         | Map&lt;String, Object&gt; |  False   | Empty Map |                  服务消费者通用元信息                   |
|          provider.port           |          Integer          |  False   |     0     |                    服务提供者端口号                     |
|       provider.thread.boss       |          Integer          |  False   |     1     |               服务提供者的bossGroup线程数               |
|      provider.thread.worker      |          Integer          |  False   |     4     |              服务提供者的workerGroup线程数              |
| provider.channel.handler-factory |          String           |  False   |  `null`   |      实现ChannelHandlerFactory，可自定义添加处理器      |
|        consumer.wait-time        |          Integer          |  False   |   5000    |         无服务提供者时等待重试时间，单位为毫秒          |
|      consumer.thread.worker      |          Integer          |  False   |     4     |              服务使用者的workerGroup线程数              |
|   consumer.retry.check-period    |          Integer          |  False   |   3000    |           重试机制执行周期(非正数代表不开启)            |
| consumer.retry.initial-threshold |          Integer          |  False   |   3000    |                    初始重试时间门槛                     |
|   consumer.fallback.max-retry    |          Integer          |  False   |    -1     | 降级机制允许重试最大的次数(负数代表不开启，0代表不重试) |
| provider.channel.handler-factory |          String           |  False   |  `null`   |      实现ChannelHandlerFactory，可自定义添加处理器      |
|  service.thread-pool.core-size   |          Integer          |  False   |     5     |                  业务线程池核心线程数                   |
|   service.thread-pool.max-size   |          Integer          |  False   |    10     |                  业务线程池最大线程数                   |
|  service.thread-pool.keep-alive  |          Integer          |  False   |   1000    |    线程池多余空闲线程在终止之前等待新任务的最长时间     |
|    service.thread-pool.queue     |          Integer          |  False   |    10     |                    业务线程池队列数                     |

### Extendsion config Items

|      Field       |  Type  | Required | Default |    Remark    |
| :--------------: | :----: | :------: | :-----: | :----------: |
| extension.logger | String |   True   |         | 日志中心地址 |

#### About customized address url

配置项中的 `registry` `atomicity` 为必填项，其url解析规则如下：

`zookeeper://127.0.0.1:2181/clrpc?session-timeout=5000`

1. 协议名，如 `zookeeper`；
2. 服务地址部分，如 `127.0.0.1:2181`；
3. 根节点部分，如 `/clrpc` （若未给出默认为 `/` ）；
4. 参数部分，如 `session-timeout=5000` 。

#### About customized meta infomation

在一个进程中，针对不同的服务可以使用不同的元信息。

例如服务提供者提供了 `AService` 和 `BService`，那么发布的元信息在配置文件中分别对应于 `meta.provider.AService` 和 `meta.provider.BService` 指向的具体元信息；若具体元信息不存在，则发布 `meta.provider.*` 对应的通用元信息；若通用元信息不存在，则发布空信息。

#### About customized channel handler

[Click me](#Extension).

## Extension

**clrpc** 利用了 **Netty** 的 `ChannelPipeline` 作为处理消息的责任链，并提供消息处理扩展点。

使用者实现接口 `conglin.clrpc.service.handler.factory.ChannelHandlerFactory`，并声明在配置文件中，即可完成对消息处理的扩展。

在创建 `conglin.clrpc.service.handler.factory.ChannelHandlerFactory` 对象时，会向构造方法其中传入一个参数，其参数类型如下：

|   Role   |                             Type                             | Remark |
| :------: | :----------------------------------------------------------: | :----: |
| Provider | conglin.clrpc.service.context.channel.ProviderChannelContext | 上下文 |
| Consumer | conglin.clrpc.service.context.channel.ConsumerChannelContext | 上下文 |

## License

[Apache 2.0](http://apache.org/licenses/LICENSE-2.0)
