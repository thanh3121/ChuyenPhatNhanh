package com.example.btl_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DonHang_NV : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_employee_orders, container, false)
        
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_orders)
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager_orders)
        
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return DanhSachDonHang.newInstance(position == 0) // 0: Pending, 1: Accepted
            }
        }
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Đơn đang chờ" else "Đơn đã nhận"
        }.attach()
        
        return view
    }
}
