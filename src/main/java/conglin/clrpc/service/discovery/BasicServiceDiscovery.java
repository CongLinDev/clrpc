package conglin.clrpc.service.discovery;

import java.util.List;
import java.util.function.Consumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.zookeeper.ZooKeeperUtils;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * 服务发现
 * 使用 Zookeeper 作为服务注册
 */

public class BasicServiceDiscovery implements ServiceDiscovery{

    private static final Logger log = LoggerFactory.getLogger(BasicServiceDiscovery.class);

    private final String registryAddress; //服务注册地址
    private ZooKeeper zooKeeper;
    private final String rootPath;

    private volatile List<String> dataList;

    private String serviceName;

    public BasicServiceDiscovery(String serviceName){
        this.serviceName = serviceName;

        String path = ConfigParser.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        //服务注册地址
        registryAddress = ConfigParser.getOrDefault("zookeeper.discovery.address", "localhost:2181");
        log.debug("Discovering zookeeper service address = " + registryAddress);
        //session timeout in milliseconds
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);

        zooKeeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);
    }

    @Override
    public void init(String localAddress, Consumer<List<String>> initMethod) {
        if(zooKeeper != null){
            registerConsumer(serviceName, localAddress);
            String absPath = rootPath + "/service/" + serviceName + "/providers";
            ZooKeeperUtils.watchChildrenData(zooKeeper, absPath, 
                data -> {
                BasicServiceDiscovery.this.dataList = data;
                initMethod.accept(BasicServiceDiscovery.this.dataList);
            });
        }
    }

    @Override
    public String discover() {
        int size = dataList.size();
        if(size == 0) return null;

        if(size == 1){
            return dataList.get(0);
        }else{
            return dataList.get(ThreadLocalRandom.current().nextInt());
        }
    }

    @Override
    public void stop(){
        if(zooKeeper != null){
            try{
                zooKeeper.close();
            }catch(InterruptedException e){
                log.error("ZooKeeper stops with error. " + e.getMessage());
            }
        }
    }

    @Override
    public void registerConsumer(String serviceName, String data) {
        //创建服务节点
        String serviceNode = rootPath + "/service/" + serviceName;
        ZooKeeperUtils.createNode(zooKeeper,serviceNode, serviceName);
        //创建消费者节点
        String absPath = rootPath + "/service/" + serviceName + "/consumers/consumer";
        ZooKeeperUtils.createNode(zooKeeper, absPath, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
}