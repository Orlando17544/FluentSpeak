package com.example.android.fluentspeak

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.example.android.fluentspeak.database.RedditDatabase
import com.example.android.fluentspeak.databinding.FragmentTopicsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TopicsFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()
    private var topicsAutoCompleteTextView: AutoCompleteTextView? = null

    private lateinit var viewModel: TopicsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentTopicsBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application

        val dataSource = RedditDatabase.getInstance(application).redditDatabaseDao

        val viewModelFactory = TopicsViewModelFactory(dataSource)

        viewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(TopicsViewModel::class.java)

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            R.layout.item,
            resources.getStringArray(R.array.topics)
        )

        topicsAutoCompleteTextView = binding.topicsField.editText as AutoCompleteTextView
        topicsAutoCompleteTextView?.setAdapter(adapter)

        binding.startButton.setOnClickListener {
            lifecycleScope.launch {
                val conversations = withContext(Dispatchers.IO) {
                    viewModel.getConversationsWithUtterances(topicsAutoCompleteTextView?.text.toString())
                }

                sharedViewModel.setConversations(conversations)
/*
                val controller = activity?.findNavController(R.id.nav_host_fragment)
                val menu: Menu? =
                    activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.menu

                val item = menu?.findItem(R.id.conversationFragment)

                NavigationUI.onNavDestinationSelected(item!!, controller!!)*/
            }
        }

        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStop() {
        super.onStop()

        topicsAutoCompleteTextView?.setText("")
    }

    override fun onDestroy() {
        super.onDestroy()

        topicsAutoCompleteTextView = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater: MenuInflater = inflater
        inflater.inflate(R.menu.topic_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settingsFragment -> {
                val navController = requireActivity().findNavController(R.id.nav_host_fragment)

                return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}