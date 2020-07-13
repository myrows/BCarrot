package com.example.bcarrot

import android.content.ClipData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import kotlinx.android.synthetic.main.bottom_navigation.*

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_navigation)

        setCurrentFragment( UserOperationsFragment() )
        chipNavigation.setItemSelected( R.id.home )

        chipNavigation.setOnItemSelectedListener {
            when (it) {
                R.id.home -> {
                    setCurrentFragment( UserOperationsFragment() )
                }
                R.id.car -> {
                    setCurrentFragment( VehicleFragment() )
                }
                R.id.premium -> {
                    setCurrentFragment( PremiumFragment() )
                }
            }

        }

    }
    fun setCurrentFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace( R.id.fragment, fragment )
        commit()
    }
}