# clrpc

这是一个基于 `Java` 、 由 **Netty** 负责传输 、**Protostuff** 负责编解码的简单的RPC(远程过程调用)工具。

服务提供者将服务发布注册到 **ZooKeeper** 上后，服务消费者请求 **ZooKeeper** 查找订阅服务后与服务提供者通信调用服务( 支持 *同步服务* 和 *异步服务* )。

## Setup

**开发阶段** 暂时不提供依赖配置。

你可以使用命令 `git clone git@github.com:CongLinDev/clrpc.git` 克隆到本地进行使用。

## Usage

### Service Provider

```java
    // 创建服务提供者
    RpcProviderBootstrap bootstrap = new RpcProviderBootstrap();

    // 发布服务并开启服务
    bootstrap.publishService("service1", ServiceBean1.class)
                .publishService("service2", new ServiceBean2())
                .publishService(Interface3.class, Implement3.class)
                .start();
    // 关闭服务
    bootstrap.stop();

```

### Service Consumer

```java
    // 创建服务消费者
    RpcConsumerBootstrap bootstrap = new RpcConsumerBootstrap();
    // 开启服务消费者
    bootstrap.start();

    // 订阅同步服务
    Interface1 i1 = bootstrap.subscribeService("service1");
    Interface2 i2 = bootstrap.subscribeService(Interface2.class);

    // 订阅异步服务
    ObjectProxy proxy = bootstrap.subscribeServiceAsync("service3");

    // 下面是你的业务逻辑代码
    // ......

    // 关闭服务消费者
    bootstrap.stop();
```

### Service Monitor

```java
    // 由监视器工厂创建监视器
    RpcMonitorBootstrap bootstrap = RpcMonitorBootstrapFactory.getRpcMonitorBootstrap(MonitorType.CONSOLE);

    try {
        // 设置监视器的配置以及你需要监视的服务
        // 并开启监视器
        bootstrap.monitor().monitorService().start();

        // 下面是你的业务逻辑代码
        // ......

        // 关闭监视器
        bootstrap.stop();
    } catch (InterruptedException e) {
        // 处理中断异常
        e.printStackTrace();
    }
```

## Architecture

![architecture.png](https://i.loli.net/2019/09/01/YbK3NuPi7UcQW9o.png)

## Config

配置文件名为 `clrpc-config.yml`，位置默认在项目根目录下。

[配置文件模板](https://github.com/CongLinDev/clrpc/blob/master/clrpc-config.yml)。

### Config File

配置文件位置默认在项目根目录下，使用 `.yml` 文件进行配置。

### Config Items

| Field | Type | Null | Default | Remark |
| :------: | :------: | :------: | :------: | :------: |
| zookeeper.registry.address | String | YES | localhost:2181 | 服务注册地址 |
| zookeeper.registry.root-path | String | YES | /clrpc | 服务注册根节点 |
| zookeeper.discovery.address | String | YES | localhost:2181 | 服务搜索地址 |
| zookeeper.discovery.root-path | String | YES | /clrpc | 服务搜索根节点 |
| zookeeper.monitor.address | String | YES | localhost:2181 | 服务监视地址 |
| zookeeper.monitor.root-path | String | YES | /clrpc | 服务监视根节点 |
| zookeeper.session.timeout | Long | YES | 5000 | 超时时间，单位为毫秒 |
| provider.address | String | YES | localhost:5100 | 服务提供者地址 |
| provider.cache.enable | Boolean | YES | false | 是否开启缓存 |
| provider.thread.boss | Integer | YES | 1 | 服务提供者的bossGroup线程数 |
| provider.thread.worker | Integer | YES | 4 | 服务提供者的workerGroup线程数 |
| provider.response-sender | conglin.clrpc.<br>transfer.sender.<br>ResponseSender | YES | conglin.clrpc.<br>transfer.<br>sender.<br>BasicResponseSender | 回复发送器 |
| provider.request-receiver | conglin.clrpc.<br>transfer.receiver.<br>RequestReceiver | YES | conglin.clrpc.<br>transfer.<br>receiver.<br>BasicRequestReceiver | 请求接收器 |
| consumer.address | String | YES | localhost:5200 | 服务使用者地址 |
| consumer.cache.enable | Boolean | YES | false | 是否开启缓存 |
| consumer.session.wait-time | Integer | YES | 5000 | 无服务提供者时等待重试时间，单位为毫秒 |
| consumer.thread.worker | Integer | YES | 4 | 服务使用者的workerGroup线程数 |
| consumer.request-sender | conglin.clrpc.<br>transfer.sender.<br>RequestSender | YES | conglin.clrpc.<br>transfer.<br>sender.<br>BasicRequestSender | 请求发送器 |
| consumer.response-receiver | conglin.clrpc.<br>transfer.receiver.<br>ResponseReceiver | YES |conglin.clrpc.<br>transfer.<br>receiver.<br>BasicResponseReceiver | 回复接收器 |
| service.thread.pool.class | conglin.clrpc.<br>common.util.<br>threadpool.<br>ThreadPool | YES | conglin.clrpc.<br>common.util.<br>threadpool.<br>CustomizedThreadPool | 业务线程池 |
| service.thread.pool.core-size | Integer | YES | 5 | 业务线程池核心线程数 |
| service.thread.pool.max-size | Integer | YES | 10 | 业务线程池最大线程数 |
| service.thread.pool.keep-alive | Integer | YES | 1000 | 当线程数大于核心时，多余空闲线程在终止之前等待新任务的最长时间 |
| service.thread.pool.queue | Integer | YES | 10 | 业务线程池队列数 |
| service.session.time-threshold | Integer | YES | 5000 | 响应时间阈值即请求最大超时时间，单位为毫秒 |
| service.codec.serialization-handler | conglin.clrpc<br>.common.codec<br>.SerializationHandler | YES | conglin.clrpc.<br>common.codec.<br>protostuff.<br>ProtostuffSerializationHandler | 序列化处理器，默认使用 Protostuff |

## Test

使用 [默认配置](https://github.com/CongLinDev/clrpc/blob/master/clrpc-config.yml) 进行本机模拟RPC测试。

1. 操作系统：Windows 10 (18362.239) 企业版
2. 处理器：Inter(R) Core(TM) i5-6300HQ CPU @ 2.30GHz
3. 内存: 12.0 GB

### Synchronous Test (without cache)

在同步测试中，**尽量了排除业务逻辑占用时间的干扰**。

[服务端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/benchmark/sync/SyncProviderTimeTest.java)

[客户端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/benchmark/sync/SyncConsumerTimeTest.java)

Conclusion:

1. 本机基础上，且只有一台服务器的情况下，1000次的*同步请求*大约在 **650毫秒** 内完成。
2. 本机基础上，且只有一台服务器的情况下，10000次的*同步请求*大约在 **3300毫秒** 内完成。
3. 本机基础上，且只有一台服务器的情况下，100000次的*同步请求*大约在 **23000毫秒** 内完成。

### Asynchronous Test (without cache)

在异步测试中，**尽量了排除业务逻辑占用时间的干扰**。

[服务端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/benchmark/async/AsyncProviderTimeTest.java)

[客户端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/benchmark/async/AsyncConsumerTimeTest.java)

Conclusion:

1. 本机基础上，且只有一台服务器的情况下，1000次的*异步请求*大约在 **760毫秒** 内完成。（请求调用完成后每500毫秒检查一次）
2. 本机基础上，且只有一台服务器的情况下，10000次的*异步请求*大约在 **1200毫秒** 内完成。（请求调用完成后每500毫秒检查一次）
3. 本机基础上，且只有一台服务器的情况下，100000次的*异步请求*大约在 **3100毫秒** 内完成。（请求调用完成后每500毫秒检查一次）

## Extension

`clrpc` 自身暂时不支持**熔断**、**服务降级**等功能。

你可以使用诸如 [resilience4j](https://github.com/resilience4j/resilience4j) 、 [Hystrix](https://github.com/Netflix/Hystrix) 等框架或库进行 **熔断**、**高频控制**、**隔离**、**限流**。

## License

[Apache 2.0](http://apache.org/licenses/LICENSE-2.0)
