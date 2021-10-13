package conglin.clrpc.extension.annotation;

/**
 * key value 信息
 */
public @interface Entry {

    /**
     * key
     * 
     * @return
     */
    String key();

    /**
     * value
     * 
     * @return
     */
    String value();
}
