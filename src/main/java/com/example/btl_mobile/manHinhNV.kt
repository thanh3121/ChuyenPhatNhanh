package com.example.btl_mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class manHinhNV : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee)

        val bottomNav = findViewById<BottomNavigationView>(R.id.employee_bottom_navigation)
        
        if (savedInstanceState == null) {
            loadFragment(DonHang_NV())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_emp_orders -> loadFragment(DonHang_NV())
                R.id.nav_emp_stats -> loadFragment(EmployeeStatsFragment())
                R.id.nav_emp_profile -> loadFragment(HoSo_NV())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.employee_fragment_container, fragment)
            .commit()
    }
}