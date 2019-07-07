package conglin.clrpc.common;

abstract public class AbstractBuilder<T>{
    private T product;

    public AbstractBuilder(T product){
        this.product = product;
    }

    public T build(){
        return product;
    }

    /**
     * @return the product
     */
    public T getProduct() {
        return product;
    }
}