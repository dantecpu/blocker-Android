package io.github.newbugger.android.ifw.entity;

import ifw.entity.ComponentFilter;
import ifw.entity.IntentFilter;
import io.github.newbugger.android.ifw.entity.ComponentFilter;
import io.github.newbugger.android.ifw.entity.IntentFilter;
import io.github.newbugger.android.merxury.ifw.entity.ComponentFilter;
import io.github.newbugger.android.merxury.ifw.entity.IntentFilter;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.List;

public class Component {
    @Attribute
    private boolean block = true;

    @Attribute
    private boolean log = true;

    @ElementList(entry = "component-filter", inline = true, empty = false, required = false)
    private List<ComponentFilter> componentFilters;

    @Element(name = "intent-filter", required = false)
    private IntentFilter intentFilter;

    public List<ComponentFilter> getComponentFilters() {
        return componentFilters;
    }

    public void setComponentFilters(List<ComponentFilter> componentFilters) {
        this.componentFilters = componentFilters;
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    public void setIntentFilter(IntentFilter intentFilter) {
        this.intentFilter = intentFilter;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }
}
