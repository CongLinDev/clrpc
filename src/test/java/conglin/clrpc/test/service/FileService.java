package conglin.clrpc.test.service;

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