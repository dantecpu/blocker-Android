package io.github.newbugger.android.blocker.core.root.service;

interface IRootService {
    boolean switchComponent(String pkg, String cls, int state);
    int getUid();
    int getPid();
}
