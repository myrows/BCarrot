package com.example.bcarrot.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.bcarrot.ui.premium.PremiumFragment
import com.example.bcarrot.R
import com.example.bcarrot.ui.devices.UserOperationsFragment
import com.example.bcarrot.ui.vehicle.VehicleFragment
import kotlinx.android.synthetic.main.bottom_navigation.*

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_navigation)

        setCurrentFragment(UserOperationsFragment())
        chipNavigation.setItemSelected(R.id.home)

        chipNavigation.setOnItemSelectedListener {
            when (it) {
                R.id.home -> {
                    setCurrentFragment(UserOperationsFragment())
                }
                R.id.car -> {
                    setCurrentFragment(VehicleFragment())
                }
                R.id.premium -> {
                    setCurrentFragment(PremiumFragment())
                }
            }

        }

    }
    fun setCurrentFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragment, fragment )
        commit()
    }
}