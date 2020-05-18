package io.github.newbugger.android.ifw.entity;

import ifw.entity.Activity;
import ifw.entity.Broadcast;
import ifw.entity.Service;
import io.github.newbugger.android.ifw.entity.Activity;
import io.github.newbugger.android.ifw.entity.Broadcast;
import io.github.newbugger.android.ifw.entity.Service;
import io.github.newbugger.android.merxury.ifw.entity.Activity;
import io.github.newbugger.android.merxury.ifw.entity.Broadcast;
import io.github.newbugger.android.merxury.ifw.entity.Service;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "rules")
public class Rules {
    @Element(required = false)
    private Activity activity;
    @Element(required = false)
    private Broadcast broadcast;
    @Element(required = false)
    private Service service;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Broadcast getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
