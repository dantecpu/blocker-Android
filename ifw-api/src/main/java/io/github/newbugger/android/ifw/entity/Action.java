package io.github.newbugger.android.ifw.entity;

import org.simpleframework.xml.Attribute;

class Action {
    @Attribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
