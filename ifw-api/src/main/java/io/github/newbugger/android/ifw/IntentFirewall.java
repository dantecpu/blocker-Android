package io.github.newbugger.android.ifw;

import io.github.newbugger.android.ifw.entity.ComponentType;

public interface IntentFirewall {
    void save() throws Exception;

    boolean add(String packageName, String componentName, ComponentType type);

    boolean remove(String packageName, String componentName, ComponentType type);

    boolean getComponentEnableState(String packageName, String componentName);

    boolean getPackageEnableState(String packageName);

    void clear();

    void clear(String name);

    void reload();

    String getPackageName();

    boolean removeCache();
}
