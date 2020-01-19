package conglin.clrpc.service.context;

import java.util.function.Consumer;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transport.component.ProviderChooserAdapter;
import conglin.clrpc.transport.component.RequestSender;

public class BasicConsumerContext extends BasicCommonContext implements ConsumerContext {

    private RequestSender requestSender;

    @Override
    public void setRequestSender(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    @Override
    public RequestSender getRequestSender() {
        return requestSender;
    }

    private FuturesHolder<Long> futuresHolder;

    @Override
    public FuturesHolder<Long> getFuturesHolder() {
        return futuresHolder;
    }

    @Override
    public void setFuturesHolder(FuturesHolder<Long> futuresHolder) {
        this.futuresHolder = futuresHolder;
    }

    private ProviderChooserAdapter providerChooserAdapter;

    @Override
    public ProviderChooserAdapter getProviderChooserAdapter() {
        return providerChooserAdapter;
    }

    @Override
    public void setProviderChooserAdapter(ProviderChooserAdapter providerChooserAdapter) {
        this.providerChooserAdapter = providerChooserAdapter;
    }

    private IdentifierGenerator identifierGenerator;

    @Override
    public IdentifierGenerator getIdentifierGenerator() {
        return identifierGenerator;
    }

    @Override
    public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }

    private Consumer<String> providerRefresher;

    @Override
    public Consumer<String> getProviderRefresher() {
        return providerRefresher;
    }

    @Override
    public void setProviderRefresher(Consumer<String> providerRefresher) {
        this.providerRefresher = providerRefresher;
    }
}