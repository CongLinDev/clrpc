package conglin.clrpc.common;

abstract public class AbstractBuilder<T>{
    
    protected final T PRODUCT;

    public AbstractBuilder(T product){
        this.PRODUCT = product;
    }

    /**
     * @return 构造完成返回产品
     */
    public T build(){
        return PRODUCT;
    }
}