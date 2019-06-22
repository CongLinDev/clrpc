package conglin.clrpc.common;

public abstract class Builder<T>{
    private T product;

    public Builder(T product){
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