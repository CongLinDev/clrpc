package conglin.clrpc.test.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import conglin.clrpc.test.service.FileService;

public class FileServiceImpl implements FileService {

    @Override
    public String receiveFile(String filename, byte[] bytes) {
        File file = new File(filename);
        try {
            if(!file.exists())
                file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

}