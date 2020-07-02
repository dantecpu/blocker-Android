package io.github.newbugger.android.ifw;

import io.github.newbugger.android.ifw.entity.ComponentType;

public interface IntentFirewall {
    void save() throws Exception;

    boolean add(String packageName, String componentName, ComponentType type) throws RuntimeException;

    boolean remove(String packageName, String componentName, ComponentType type) throws RuntimeException;

    boolean getComponentEnableState(String packageName, String componentName) throws RuntimeException;

    void clear() throws RuntimeException;

    void clear(String name) throws RuntimeException;

    void reload() throws RuntimeException;

    String getPackageName() throws RuntimeException;

    boolean removeCache() throws RuntimeException;
}
