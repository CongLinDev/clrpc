# clrpc

这是一个基于 `Java` 、 由 **Netty** 负责传输 、**Protostuff** 负责编解码的简单的RPC(远程过程调用)工具。

服务提供者将服务注册到 **ZooKeeper** 上后，服务消费者请求 **ZooKeeper** 查找服务后调用服务( *同步服务* 和 *异步服务* )。

## 开始

### 服务端

```java
    // 创建服务端
    RpcServerBootstrap bootstrap = new RpcServerBootstrap();

    try{
        // 添加服务并开启服务端
        bootstrap.addService("service1", ServiceBean1.class)
                 .addService("service2", new ServiceBean2())
                 .addService(Interface3.class, Implement3.class)
                 .start();
    }finally{
        // 关闭服务端
        bootstrap.stop();
    }
```

### 客户端

```java
    // 创建客户端
    RpcClientBootstrap bootstrap = new RpcClientBootstrap();
    // 开启客户端
    bootstrap.start();

    // 获取同步服务
    Interface1 i1 = bootstrap.getService("service1");
    Interface2 i2 = bootstrap.getService(Interface2.class);

    // 获取异步服务
    ObjectProxy proxy = bootstrap.getAsynchronousService("service3");

    // 下面是你的业务逻辑代码
    // ......

    // 关闭客户端
    bootstrap.stop();
```

### 监视器

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

## 架构

![architecture.png](https://i.loli.net/2019/08/09/7B2nMXNZJS1cmDH.png)

## 配置

配置文件名为 `clrpc-config.yml`，位置默认在项目根目录下。

[配置文件模板](https://github.com/CongLinDev/clrpc/blob/master/clrpc-config.yml)。

### 配置文件位置

文件位置默认在项目根目录下。

### 配置项

| Field | Type | Null | Default | Remark |
| :------: | :------: | :------: | :------: | :------: |
| zookeeper.registry.address | String | YES | localhost:2181 | 服务注册地址 |
| zookeeper.registry.root-path | String | YES | /clrpc | 服务注册根节点 |
| zookeeper.discovery.address | String | YES | localhost:2181 | 服务搜索地址 |
| zookeeper.discovery.root-path | String | YES | /clrpc | 服务搜索根节点 |
| zookeeper.session.timeout | Long | YES | 5000 | 超时时间，单位为毫秒 |
| server.address | String | YES | localhost:5100 | 服务提供者地址 |
| server.thread.boss | Integer | YES | 1 | 服务提供者的bossGroup线程数 |
| server.thread.worker | Integer | YES | 4 | 服务提供者的workerGroup线程数 |
| server.request-receiver | conglin.clrpc.<br>transfer.net.<br>receiver.<br>RequestReceiver | YES | conglin.clrpc.<br>transfer.net.<br>receiver.<br>BasicRequestReceiver | 请求接收器 |
| client.address | String | YES | localhost:5200 | 服务使用者地址 |
| client.session.timeout | Integer | YES | 5000 | 超时时间，单位为毫秒 |
| client.thread.worker | Integer | YES | 4 | 服务使用者的workerGroup线程数 |
| client.request-sender | conglin.clrpc.<br>transfer.net.<br>sender.<br>RequestSender | YES | conglin.clrpc.<br>transfer.net.<br>sender.<br>BasicRequestSender | 请求发送器 |
| service.thread.pool.class | conglin.clrpc.<br>common.util.<br>threadpool.<br>ThreadPool | YES | conglin.clrpc.<br>common.util.<br>threadpool.<br>FixedThreadPool | 业务线程池 |
| service.thread.pool.core-size | Integer | YES | 5 | 业务线程池核心线程数 |
| service.thread.pool.max-size | Integer | YES | 10 | 业务线程池最大线程数 |
| service.thread.pool.keep-alive | Integer | YES | 1000 | 当线程数大于核心时，多余空闲线程在终止之前等待新任务的最长时间 |
| service.thread.pool.queue | Integer | YES | 10 | 业务线程池队列数 |
| service.session.time-threshold | Integer | YES | 5000 | 响应时间阈值，单位为毫秒 |
| service.codec.serialization-handler | conglin.clrpc.<br>transfer.codec.<br>SerializationHandler | YES | conglin.clrpc.<br>transfer.codec.<br>protostuff.<br>ProtostuffSerializationHandler | 序列化处理器，默认使用 Protostuff |

## 测试

### 同步

在同步测试中，**尽量了排除业务逻辑占用时间的干扰**。

[服务端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/benchmark/sync/SyncServerTimeTest.java)

[客户端](https://github.com/CongLinDev/clrpc/blob/master/src/test/java/conglin/clrpc/test/benchmark/sync/SyncClientTimeTest.java)

使用 [默认配置](https://github.com/CongLinDev/clrpc/blob/master/clrpc-config.yml) 进行本机模拟RPC测试。

1. 操作系统：Windows 10 (18362.239) 企业版
2. 处理器：Inter(R) Core(TM) i5-6300HQ CPU @ 2.30GHz
3. 内存: 12.0 GB

### 结论

1. 本机基础上，且只有一台服务器的情况下，1000次的*同步请求*大约在 **650毫秒** 内完成。
2. 本机基础上，且只有一台服务器的情况下，10000次的*同步请求*大约在 **3300毫秒** 内完成。
3. 本机基础上，且只有一台服务器的情况下，100000次的*同步请求*大约在 **23000毫秒** 内完成。

## 扩展

`clrpc` 自身暂时不支持 熔断、服务降级等功能。

你可以使用诸如 [resilience4j](https://github.com/resilience4j/resilience4j) 、 [Hystrix](https://github.com/Netflix/Hystrix) 等框架或库进行 **熔断**、**高频控制**、**隔离**、**限流**、**限时**、**重试**。
