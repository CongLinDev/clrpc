# clrpc

这是一个基于 `Java` 、 由 **Netty** 负责传输 、默认使用 **Protostuff** 负责编解码的简单的RPC(远程过程调用)工具。

服务提供者将服务发布注册到 注册中心 **ZooKeeper** 上后，服务消费者请求 注册中心 **ZooKeeper** 查找订阅服务后与服务提供者通信调用服务( 支持 *同步服务* 和 *异步服务* )。

## Setup

当前阶段均为 `SNAPSHOT` 版本，暂时不提供依赖配置。

你可以使用命令 `git clone git@github.com:CongLinDev/clrpc.git` 克隆到本地进行使用。

## Usage

### Service Provider

```java
// 创建服务提供者
RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();

// 发布服务并开启服务
bootstrap.publish(ServiceBean1.class) // 配合 @conglin.clrpc.service.annotation.Service 注解
        .publish(new ServiceBean2())  // 配合 @conglin.clrpc.service.annotation.Service 注解
        .publish("service3", new ServiceBean3())
        .hookStop() // 注册关闭钩子，用于优雅关闭服务提供者
        .start();
```

### Service Consumer

```java
// 创建服务消费者
RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
// 开启服务消费者
bootstrap.start();
// 提前刷新需要订阅服务，亦可直接调用 refreshAndSubscribe 刷新并获取代理
bootstrap.refresh("service1").refresh("service2").refresh("service3");

// 使用通用的服务
CommonProxy commonProxy = bootstrap.subscribe();

// 订阅同步服务
Interface1 i1 = bootstrap.subscribe("service1", Interface1.class);
Interface2 i2 = bootstrap.subscribe(Interface2.class); // 配合 @conglin.clrpc.service.annotation.Service 注解

// 订阅异步服务
ObjectProxy objectProxy = bootstrap.subscribe("service3");

// 订阅事务服务
TransactionProxy transactionProxy = bootstrap.subscribeTransaction();

// 下面是你的业务逻辑代码
// ......

// 关闭服务消费者
bootstrap.stop();
```

### Service Monitor

```java
// 由监视器工厂创建监视器
RpcMonitorBootstrap bootstrap = new ConsoleRpcMonitorBootstrap();

// 设置监视器的配置以及你需要监视的服务
// 并开启服务监视器
bootstrap.monitor("service1").monitor(Interface2.class).start();

// 下面是你的业务逻辑代码
// ......

// 关闭服务监视器
bootstrap.stop();
```

## Architecture

![architecture.png](https://i.loli.net/2020/01/21/63Ea7nbxez5Hkmd.png)

## Config

默认配置文件名为 `clrpc-config`。

[默认配置文件模板](https://github.com/CongLinDev/clrpc/blob/master/src/main/resources/clrpc-config.json)。

### Config File

配置文件位置默认在项目 `src/main/resources` 目录下，默认格式为 `json` ，默认文件为 `clrpc-config.json`。

### Config Items

|                  Field                  |           Type            | Null  |    Default     |                             Remark                             |
| :-------------------------------------: | :-----------------------: | :---: | :------------: | :------------------------------------------------------------: |
|       zookeeper.registry.address        |          String           |  YES  | 127.0.0.1:2181 |                          服务注册地址                          |
|      zookeeper.registry.root-path       |          String           |  YES  |     /clrpc     |                         服务注册根节点                         |
| zookeeper.registry.<br>session-timeout  |          Integer          |  YES  |      5000      |                      超时时间，单位为毫秒                      |
|       zookeeper.discovery.address       |          String           |  YES  | 127.0.0.1:2181 |                          服务搜索地址                          |
|      zookeeper.discovery.root-path      |          String           |  YES  |     /clrpc     |                         服务搜索根节点                         |
| zookeeper.discovery.<br>session-timeout |          Integer          |  YES  |      5000      |                      超时时间，单位为毫秒                      |
|        zookeeper.monitor.address        |          String           |  YES  | 127.0.0.1:2181 |                          服务监视地址                          |
|       zookeeper.monitor.root-path       |          String           |  YES  |     /clrpc     |                         服务监视根节点                         |
|  zookeeper.monitor.<br>session-timeout  |          Integer          |  YES  |      5000      |                      超时时间，单位为毫秒                      |
|       zookeeper.atomicity.address       |          String           |  YES  | 127.0.0.1:2181 |                         原子服务地址                         |
|      zookeeper.atomicity.root-path      |          String           |  YES  |     /clrpc     |                        原子服务根节点                        |
| zookeeper.atomicity.<br>session-timeout |          Integer          |  YES  |      5000      |                      超时时间，单位为毫秒                      |
|            meta.provider.\*             | Map&lt;String, Object&gt; |  YES  |   Empty Map    |              服务提供者通用元信息，发布至注册中心              |
|            meta.consumer.\*             | Map&lt;String, Object&gt; |  YES  |   Empty Map    |              服务消费者通用元信息，发布至注册中心              |
|              provider.port              |          Integer          |  YES  |       0        |                        服务提供者端口号                        |
|          provider.thread.boss           |          Integer          |  YES  |       1        |                  服务提供者的bossGroup线程数                   |
|         provider.thread.worker          |          Integer          |  YES  |       4        |                 服务提供者的workerGroup线程数                  |
|   provider.channel-handler<br>.before   |    List&lt;String&gt;     |  YES  |   Empty List   |               处理请求之前的自定义ChannelHandler               |
|   provider.channel-handler<br>.after    |    List&lt;String&gt;     |  YES  |   Empty List   |               处理请求之后的自定义ChannelHandler               |
|           consumer.wait-time            |          Integer          |  YES  |      5000      |             无服务提供者时等待重试时间，单位为毫秒             |
|         consumer.thread.worker          |          Integer          |  YES  |       4        |                 服务使用者的workerGroup线程数                  |
|   consumer.channel-handler<br>.before   |    List&lt;String&gt;     |  YES  |   Empty List   |               处理请求之前的自定义ChannelHandler               |
|   consumer.channel-handler<br>.after    |    List&lt;String&gt;     |  YES  |   Empty List   |               处理请求之后的自定义ChannelHandler               |
|    service.thread-pool.<br>core-size    |          Integer          |  YES  |       5        |                      业务线程池核心线程数                      |
|    service.thread-pool.<br>max-size     |          Integer          |  YES  |       10       |                      业务线程池最大线程数                      |
|   service.thread-pool.<br>keep-alive    |          Integer          |  YES  |      1000      | 当线程数大于核心时，多余空闲线程在终止之前等待新任务的最长时间 |
|      service.thread-pool.<br>queue      |          Integer          |  YES  |       10       |                        业务线程池队列数                        |

#### About customized meta infomation

在一个进程中，针对不同的服务可以使用不同的元信息。

例如服务提供者提供了 `AService` 和 `BService`，那么发布的元信息在配置文件中分别对应于 `meta.provider.AService` 和 `meta.provider.BService` 指向的具体元信息；若具体元信息不存在，则发布 `meta.provider.*` 对应的通用元信息；若通用元信息不存在，则发布空信息。

#### About customized channel handler

[Click me](#Extension).

## Test

使用 [默认配置文件](https://github.com/CongLinDev/clrpc/blob/master/src/main/resources/clrpc-config.json) 进行本机模拟RPC测试。

+ OS：Manjaro 19.0 Kyria
+ Kernel: x86_64 Linux 5.4.18-1-MANJARO
+ CPU：Intel Core i5-6300HQ @ 4x 2.30GHz
+ RAM: 11873 MB
+ JDK: openjdk-13.0.2

### Synchronous Test (without cache)

在同步测试中，**尽量了排除业务逻辑占用时间的干扰**。

[服务端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/provider/HelloServiceProviderTest.java)

[客户端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/consumer/sync/HelloServiceSyncConsumerTimeTest.java)

Conclusion:

1. 本机基础上，且只有一台服务器的情况下，1000次的*同步请求*大约在 **500毫秒** 内完成。
2. 本机基础上，且只有一台服务器的情况下，10000次的*同步请求*大约在 **2000毫秒** 内完成。
3. 本机基础上，且只有一台服务器的情况下，100000次的*同步请求*大约在 **10000毫秒** 内完成。

### Asynchronous Test (without cache)

在异步测试中，**尽量了排除业务逻辑占用时间的干扰**。

[服务端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/provider/HelloServiceProviderTest.java)

[客户端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/consumer/sync/HelloServiceAsyncConsumerTimeTest.java)

Conclusion:

1. 本机基础上，且只有一台服务器的情况下，1000次的*异步请求*大约在 **680毫秒** 内完成。（请求调用完成后每500毫秒检查一次）
2. 本机基础上，且只有一台服务器的情况下，10000次的*异步请求*大约在 **1400毫秒** 内完成。（请求调用完成后每500毫秒检查一次）
3. 本机基础上，且只有一台服务器的情况下，100000次的*异步请求*大约在 **3000毫秒** 内完成。（请求调用完成后每500毫秒检查一次）

## Extension

**clrpc** 利用了 **Netty** 的 `ChannelPipeline` 作为处理消息的责任链，并提供消息处理扩展点。

使用者只需要提供实现接口 `io.netty.channel.ChannelHandler` 的类且存在一个参数（类型如下表）的构造方法，并在配置文件中告知 **clrpc** 该类作用的时机即可完成对消息处理的扩展。

|   Role   |                     Type                      | Remark |
| :------: | :-------------------------------------------: | :----: |
| Provider | conglin.clrpc.service.context.ProviderContext | 上下文 |
| Consumer | conglin.clrpc.service.context.ConsumerContext | 上下文 |

## License

[Apache 2.0](http://apache.org/licenses/LICENSE-2.0)
