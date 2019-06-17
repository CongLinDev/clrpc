# clrpc

这是一个基于 `Java` 由 **Netty** 负责传输，由 **Protobuf** 负责编解码的简单的RPC(远程过程调用)软件。服务端将服务注册到 **Zookeeper** 上，客户端查找服务后调用。

## 开始

### 服务端

```java

    RpcServerBootstrap serverBootstrap = new RpcServerBootstrap();

    bootstrap.addService(Interface1.class, Implement1.class)
             .addService(Interface2.class, Implement2.class)
             .start();
```

### 客户端

```java

    RpcClientBootstrap bootstrap = new RpcClientBootstrap();
    bootstrap.start();

    Interface1 i1 = bootstrap.getService(Interface1.class);
    Interface2 i2 = bootstrap.getService(Interface2.class);

    // 下面是你的业务逻辑代码
    // ......

    bootstrap.stop();
```
