package conglin.clrpc.service.handler.factory;

public enum ChannelHandlerPhase {
    BEFORE_CODEC,
    CODEC,
    BEFORE_HANDLE,
    HANDLE,
    AFTER_HANDLE;
}
