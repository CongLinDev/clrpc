package conglin.clrpc.test.service;

import conglin.clrpc.service.annotation.Service;

@Service(name = "FileService")
public interface FileService {
    /**
     * 接收文件
     * 
     * @param filename 文件名
     * @param bytes    文件字节流
     * @return
     */
    String receiveFile(String filename, byte[] bytes);
}