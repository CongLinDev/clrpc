package conglin.clrpc.service.context;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.executor.AbstractConsumerServiceExecutor;
import conglin.clrpc.service.executor.RequestSender;
import conglin.clrpc.service.executor.ServiceExecutor;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transport.chooser.ProviderChooser;
import conglin.clrpc.transport.chooser.ProviderChooserAdapter;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicConsumerContext extends BasicCommonContext implements ConsumerContext {
 
    protected AbstractConsumerServiceExecutor consumerServiceExecutor;

    @Override
    public void setConsumerServiceExecutor(AbstractConsumerServiceExecutor consumerServiceExecutor) {
        this.consumerServiceExecutor = consumerServiceExecutor;
    }

    @Override
    public ServiceExecutor<BasicResponse> getServiceExecutor() {
        return consumerServiceExecutor;
    }

    @Override
    public RequestSender getRequestSender() {
        return consumerServiceExecutor;
    }

    protected FuturesHolder<Long> futuresHolder;

    @Override
    public FuturesHolder<Long> getFuturesHolder() {
        return futuresHolder;
    }

    @Override
    public void setFuturesHolder(FuturesHolder<Long> futuresHolder) {
        this.futuresHolder = futuresHolder;
    }

    protected ProviderChooser providerChooser;

    @Override
    public ProviderChooser getProviderChooser() {
        return providerChooser;
    }

    @Override
    public void setProviderChooser(ProviderChooser providerChooser) {
        this.providerChooser = providerChooser;
    }

    protected ProviderChooserAdapter providerChooserAdapter;

    @Override
    public ProviderChooserAdapter getProviderChooserAdapter() {
        return providerChooserAdapter;
    }

    @Override
    public void setProviderChooserAdapter(ProviderChooserAdapter providerChooserAdapter) {
        this.providerChooserAdapter = providerChooserAdapter;
    }

    protected IdentifierGenerator identifierGenerator;

    @Override
    public IdentifierGenerator getIdentifierGenerator() {
        return identifierGenerator;
    }

    @Override
    public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }
}