package conglin.clrpc.common.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class SimpleSynchronizer extends AbstractQueuedSynchronizer{

    private static final long serialVersionUID = 1L;
    
    private final int DONE = 1;//完成
    private final int PENDING = 0;//等待

    @Override
    protected boolean tryAcquire(int arg) {
        return getState() == DONE;
    }

    @Override
    protected boolean tryRelease(int arg) {
        if(getState() == PENDING){
           return compareAndSetState(PENDING, DONE);
        }else{
            return true;
        }
    }

    public boolean isDone(){
        return getState() == DONE;
    }
}