package conglin.clrpc.bootstrap.option;

import java.util.function.Supplier;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.RandomIdentifierGenerator;
import conglin.clrpc.transport.component.ProviderChooserAdapter;

public enum RpcOptionEnum {
    IDENTIFIER_GENERATOR(IdentifierGenerator.class, RandomIdentifierGenerator::new),
    PROVIDER_CHOOSER_ADAPTER(ProviderChooserAdapter.class, ProviderChooserAdapter::defaultAdapter);

    private final Supplier<?> defaultSupplier;

    private final Class<?> clazz;

    RpcOptionEnum(Class<?> clazz, Supplier<?> defaultSupplier) {
        this.clazz = clazz;
        this.defaultSupplier = defaultSupplier;
    }

    /**
     * 生成对象
     * 
     * @return
     */
    public Object generate() {
        return defaultSupplier.get();
    }

    /**
     * 是否接受对象
     * 
     * @param obj
     * @return
     */
    public boolean accept(Object obj) {
        return clazz.isAssignableFrom(obj.getClass());
    }

    /**
     * 接收的类
     * 
     * @return
     */
    public Class<?> acceptClass() {
        return clazz;
    } 
    
}
