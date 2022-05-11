package conglin.clrpc.common;

public enum State {
    UNKNOWN,
    PREPARE,
    INITING,
    AVAILABLE,
    DESTORYING,
    UNAVAILABLE;

    public static class StateRecord {
        private State state;

        public StateRecord() {
            this(UNKNOWN);
        }

        public StateRecord(State state) {
            this.state = state;
        }

        /**
         * 是否是某个state
         * 
         * @param state
         * @return
         */
        public boolean isState(State state) {
            return this.state.equals(state);
        }

        /**
         * 返回当前状态
         * 
         * @return
         */
        public State getState() {
            return this.state;
        }

        /**
         * 设置当前状态
         * 
         * @param state
         */
        public void setState(State state) {
            this.state = state;
        }

        /**
         * compare and set
         * 
         * @param exceptState
         * @param newState
         * @return
         */
        public boolean compareAndSetState(State exceptState, State newState) {
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
         * @throws UnsupportedOperationException
         */
        public void except(State exceptState) throws UnsupportedOperationException {
            if (!this.state.equals(exceptState)) {
                throw new UnsupportedOperationException(
                        String.format("expectState=%s realState=%s", exceptState, state));
            }
        }
    }
}
