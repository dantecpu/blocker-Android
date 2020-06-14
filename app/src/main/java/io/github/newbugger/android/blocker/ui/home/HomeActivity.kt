package io.github.newbugger.android.blocker.ui.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import io.github.newbugger.android.blocker.R
import io.github.newbugger.android.blocker.adapter.FragmentAdapter
import io.github.newbugger.android.blocker.base.IActivityView
import io.github.newbugger.android.blocker.ui.settings.SettingsActivity
import io.github.newbugger.android.blocker.util.PreferenceUtil
import io.github.newbugger.android.blocker.shizuku.ShizukuBinder
import io.github.newbugger.android.blocker.util.setupActionBar
import io.github.newbugger.android.libkit.libsu.LibsuCommand
import io.github.newbugger.android.libkit.utils.ConstantUtil
import io.github.newbugger.android.libkit.utils.StatusBarUtil
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity(), IActivityView {
    private lateinit var drawer: Drawer
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupActionBar(R.id.toolbar) {
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
            setDisplayHomeAsUpEnabled(true)
        }
        setupDrawerContent(savedInstanceState)
        setupViewPager(app_viewpager)
        findViewById<TabLayout>(R.id.app_kind_tabs).apply {
            setupWithViewPager(app_viewpager)
            setupTab(this)
        }
    }

    override fun onStart() {
        super.onStart()
        shizukuSetup(this)
        createNotificationChannel()
    }

    override fun onStop() {
        super.onStop()
        LibsuCommand.close()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = FragmentAdapter(supportFragmentManager)
        adapter.addFragment(ApplicationListFragment.newInstance(false), getString(R.string.third_party_app_tab_text))
        adapter.addFragment(ApplicationListFragment.newInstance(true), getString(R.string.system_app_tab_text))
        viewPager.adapter = adapter
    }

    private fun setupDrawerContent(savedInstanceState: Bundle?) {
        val listItem = PrimaryDrawerItem()
            .withIdentifier(1)
            .withName(R.string.app_list_title)
            .withIcon(R.drawable.ic_list)
        val settingItem = SecondaryDrawerItem()
            .withIdentifier(2)
            .withName(R.string.action_settings)
            .withIcon(R.drawable.ic_settings)
        drawer = DrawerBuilder()
            .withActivity(this)
            .withTranslucentStatusBar(true)
            .withToolbar(toolbar)
            .withSavedInstance(savedInstanceState)
            .withActionBarDrawerToggleAnimated(true)
            .addDrawerItems(
                listItem,
                settingItem,
                DividerDrawerItem()
            )
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                when (drawerItem?.identifier) {
                    1L -> startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                    2L -> startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
                }
                false
            }
            .withSelectedItem(1L)
            .withCloseOnClick(true)
            .build()
        drawerLayout = drawer.drawerLayout

    }

    private fun setupTab(tabLayout: TabLayout) {
        changeColor(getBackgroundColor(0))
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.md_white_1000))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                changeBackgroundColor(tabLayout, tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawer.openDrawer()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeColor(color: Int) {
        toolbar.setBackgroundColor(color)
        app_kind_tabs.setBackgroundColor(color)
        StatusBarUtil.setColorForDrawerLayout(this, drawerLayout, color, ConstantUtil.STATUS_BAR_ALPHA)
        findViewById<View>(R.id.statusbarutil_translucent_view).setBackgroundColor(color)
    }

    private fun changeBackgroundColor(tabLayout: TabLayout, tab: TabLayout.Tab) {
        val colorFrom: Int = if (tabLayout.background != null) {
            (tabLayout.background as ColorDrawable).color
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

    override fun getBackgroundColor(tabPosition: Int): Int {
        return when (tabPosition) {
            0 -> ContextCompat.getColor(this, R.color.colorPrimary)
            1 -> ContextCompat.getColor(this, R.color.md_red_500)
            else -> ContextCompat.getColor(this, R.color.md_grey_700)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        }
    }

    private fun shizukuSetup(context: Context) {
        context.let {
            if (!PreferenceUtil.checkShizukuType(it)) return
            if (!ShizukuBinder.shizukuRequestPermission(it)) return
        }
    }

    private fun createNotificationChannel() {
        val channelId = "processing_progress_indicator"
        val channelName = getString(R.string.processing_progress_indicator)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}
