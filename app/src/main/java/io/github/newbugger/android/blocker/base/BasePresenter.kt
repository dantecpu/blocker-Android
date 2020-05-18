package io.github.newbugger.android.blocker.base

import android.content.Context

interface BasePresenter {
    fun start(context: Context)
    fun destroy()
}