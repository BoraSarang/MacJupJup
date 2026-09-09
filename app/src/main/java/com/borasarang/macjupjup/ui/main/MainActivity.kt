package com.borasarang.macjupjup.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import com.borasarang.macjupjup.R
import com.borasarang.macjupjup.databinding.ActivityMainBinding
import com.borasarang.macjupjup.ui.home.HomeFragment
import com.borasarang.macjupjup.ui.notif.NotificationFragment
import com.borasarang.macjupjup.ui.settings.SettingsFragment
import com.borasarang.macjupjup.ui.source.SourceManageFragment
import com.borasarang.macjupjup.util.DebugLogger

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLogger.i("네비게이션", "메인 화면 진입")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()

        if (savedInstanceState == null) {
            showTab(R.id.nav_home)
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
    }

    /** 수집 실패 알림용 (Android 13+ 런타임 권한) */
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < 33) return
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 2001)
    }

    private fun showTab(itemId: Int) {        val fragment = when (itemId) {
            R.id.nav_source -> SourceManageFragment()
            R.id.nav_notif -> NotificationFragment()
            R.id.nav_settings -> SettingsFragment()
            else -> HomeFragment()
        }
        // 백스택 미사용 → 동기 커밋으로 Espresso 타이밍 이슈 방지
        supportFragmentManager.commitNow {
            replace(R.id.fragment_container, fragment)
        }
    }
}
