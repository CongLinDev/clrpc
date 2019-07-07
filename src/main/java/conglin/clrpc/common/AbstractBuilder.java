package conglin.clrpc.common;

abstract public class AbstractBuilder<T>{
    protected T product;

    public AbstractBuilder(T product){
        this.product = product;
    }

    /**
     * @return 构造完成返回产品
     */
    public T build(){
        return product;
    }
}