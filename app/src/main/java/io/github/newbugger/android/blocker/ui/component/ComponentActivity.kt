package io.github.newbugger.android.blocker.ui.component

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.adapter.FragmentAdapter
import io.github.newbugger.android.blocker.base.IActivityView
import io.github.newbugger.android.blocker.util.setupActionBar
import io.github.newbugger.android.libkit.entity.Application
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.libkit.utils.StatusBarUtil
import kotlinx.android.synthetic.main.activity_component.*
import kotlinx.android.synthetic.main.application_brief_info_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ComponentActivity : AppCompatActivity(), IActivityView {

    private lateinit var application: Application
    private lateinit var adapter: FragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component)
        setupActionBar(R.id.component_toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        getDataFromIntent()
        setupViewPager()
        setupTab()
        showApplicationBriefInfo(application)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        findViewById<SearchView>(R.id.menu_search)?.let {
            if (!it.isIconified) {
                it.isIconified = true
                it.clearFocus()
                return
            }
        }
        super.onBackPressed()
    }

    override fun getBackgroundColor(tabPosition: Int): Int {
        return when (tabPosition) {
            0 -> ContextCompat.getColor(this, R.color.google_blue)
            1 -> ContextCompat.getColor(this, R.color.google_green)
            2 -> ContextCompat.getColor(this, R.color.google_red)
            3 -> ContextCompat.getColor(this, R.color.md_yellow_800)
            else -> ContextCompat.getColor(this, R.color.md_grey_700)
        }
    }

    private fun setupViewPager() {
        adapter = FragmentAdapter(supportFragmentManager)
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.RECEIVER), getString(R.string.receiver))
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.SERVICE), getString(R.string.service))
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.ACTIVITY), getString(R.string.activity))
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.PROVIDER), getString(R.string.provider))
        component_viewpager.adapter = adapter
    }

    private fun getDataFromIntent() {
        if (intent == null) {
            finish()
        }
        application = intent.getParcelableExtra(ConstantUtil.APPLICATION)!!
    }

    private fun setupTab() {
        component_tabs.setupWithViewPager(component_viewpager)
        changeColor(getBackgroundColor(0))
        component_tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.md_white_1000))
        component_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                changeBackgroundColor(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun showApplicationBriefInfo(application: Application) {
        app_info_app_name.text = getString(R.string.application_label, application.label)
        app_info_app_package_name.text = getString(R.string.package_name, application.packageName)
        app_info_target_sdk_version.text = getString(R.string.target_sdk_version, application.targetSdkVersion.toString())
        app_info_min_sdk_version.text = getString(R.string.min_sdk_version, application.minSdkVersion.toString())
        GlobalScope.launch(Dispatchers.Default) {
            val icon = application.getApplicationIcon(packageManager)
            launch(Dispatchers.Main) {
                app_info_icon.setImageDrawable(icon)
            }
        }
    }

    private fun changeColor(color: Int) {
        component_toolbar.setBackgroundColor(color)
        component_tabs.setBackgroundColor(color)
        component_collapsing_toolbar.setBackgroundColor(color)
        StatusBarUtil.setColor(this, color, ConstantUtil.STATUS_BAR_ALPHA)
    }

    private fun changeBackgroundColor(tab: TabLayout.Tab) {
        val colorFrom = if (component_tabs.background != null) {
            (component_tabs.background as ColorDrawable).color
        } else {
            ContextCompat.getColor(this, android.R.color.darker_gray)
        }
        val colorTo = getBackgroundColor(tab.position)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animation ->
            val color = animation.animatedValue as Int
            changeColor(color)
        }
        colorAnimation.duration = 500
        colorAnimation.start()
    }

}
