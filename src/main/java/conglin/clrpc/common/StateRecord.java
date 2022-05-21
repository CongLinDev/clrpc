package conglin.clrpc.common;

public class StateRecord<T extends Enum<?>> {
    private T state;

    public StateRecord(T state) {
        this.state = state;
    }

    /**
     * 是否是某个state
     * 
     * @param state
     * @return
     */
    public boolean isState(T state) {
        return this.state.equals(state);
    }

    /**
     * 返回当前状态
     * 
     * @return
     */
    public T getState() {
        return this.state;
    }

    /**
     * 设置当前状态
     * 
     * @param state
     */
    public void setState(T state) {
        this.state = state;
    }

    /**
     * compare and set
     * 
     * @param exceptState
     * @param newState
     * @return
     */
    public boolean compareAndSetState(T exceptState, T newState) {
        if (this.state.equals(exceptState)) {
            this.state = newState;
            return true;
        }
        return false;
    }

    /**
     * 判断是否是预期的state
     * 
     * @param exceptState
     * @throws IllegalStateException
     */
    public void except(T exceptState) throws IllegalStateException {
        if (!this.state.equals(exceptState)) {
            throw new IllegalStateException(
                    String.format("expectState=%s realState=%s", exceptState, state));
        }
    }
}
