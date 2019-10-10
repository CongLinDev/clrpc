package conglin.clrpc.common;

abstract public class AbstractChain<T> implements Chain<T>{

    private Chain<T> next;

    @Override
    public Chain<T> next(){
        return next;
    }

    @Override
    public void next(Chain<T> next){
        Chain<T> temp = this.next;
        this.setNextDirectly(next);

        while(next.next() != null){
            next = next.next();
        }
        next.setNextDirectly(temp);
    }

    @Override
    public void setNextDirectly(Chain<T> next){
        this.next = next;
    }
}