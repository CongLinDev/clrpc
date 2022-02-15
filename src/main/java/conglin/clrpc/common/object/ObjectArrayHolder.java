package conglin.clrpc.common.object;

public class ObjectArrayHolder<T extends Enum<?>> implements ObjectHolder<T> {

    private final Object[] holder;

    public ObjectArrayHolder(Object[] holder) {
        this.holder = holder;
    }

    public ObjectArrayHolder(int size) {
        this(new Object[size]);
    }

    @Override
    public void put(T key, Object value) {
        if(holder[key.ordinal()] != null) {
            putIfContains(key, holder[key.ordinal()], value);
        } else {
            doPut(key, value);
        }
    }

    protected void doPut(T key, Object value) {
        holder[key.ordinal()] = value;
    }

    @Override
    public Object get(T key) {
        return holder[key.ordinal()];
    }

    /**
     * put before contains
     * @param key
     * @param oldValue
     * @param value
     */
    protected void putIfContains(T key, Object oldValue, Object value) {
        // default do nothing
    }
}
