package com.example.android.fluentspeak

import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.page_1 -> {
                    // Respond to navigation item 1 click
                    val fragment = ConversationFragment()

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                R.id.page_2 -> {
                    // Respond to navigation item 2 click
                    val fragment = TopicsFragment()

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.page_1
    }
}