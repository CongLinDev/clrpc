package conglin.clrpc.service.context;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.registry.ServiceRegistry;
import conglin.clrpc.service.fallback.FallbackHolder;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transport.component.ProviderChooser;
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

    private FallbackHolder fallbackHolder;

    @Override
    public FallbackHolder getFallbackHolder() {
        return fallbackHolder;
    }

    @Override
    public void setFallbackHolder(FallbackHolder fallbackHolder) {
        this.fallbackHolder = fallbackHolder;
    }

    @Override
    public void setFuturesHolder(FuturesHolder<Long> futuresHolder) {
        this.futuresHolder = futuresHolder;
    }

    private ProviderChooser providerChooser;

    @Override
    public ProviderChooser getProviderChooser() {
        return providerChooser;
    }

    @Override
    public void setProviderChooser(ProviderChooser providerChooser) {
        this.providerChooser = providerChooser;
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

    private ServiceRegistry serviceRegister;

    @Override
    public ServiceRegistry getServiceRegister() {
        return serviceRegister;
    }

    @Override
    public void setServiceRegister(ServiceRegistry serviceRegister) {
        this.serviceRegister = serviceRegister;
    }
}