package io.github.newbugger.android.ifw.entity;

import io.github.newbugger.android.ifw.entity.Action;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class IntentFilter {
    @ElementList(inline = true)
    private
    List<Action> actions;

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
}
