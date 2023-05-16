package com.example.android.fluentspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.android.fluentspeak.database.RedditDatabase
import com.example.android.fluentspeak.databinding.FragmentTopicsBinding
import com.example.android.fluentspeak.network.TranscriptionRequestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TopicsFragment : Fragment() {

    private val sharedViewModel: MainViewModel by activityViewModels()

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
                this, viewModelFactory).get(TopicsViewModel::class.java)

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            R.layout.item,
            resources.getStringArray(R.array.topics)
        )

        val topicsAutoCompleteTextView = binding.topicsField.editText as AutoCompleteTextView
        topicsAutoCompleteTextView.setAdapter(adapter)

        binding.startButton.setOnClickListener {
            lifecycleScope.launch {
                val conversationsWithUtterances = withContext(Dispatchers.IO) {
                    viewModel.getConversationsWithUtterances(topicsAutoCompleteTextView.text.toString())
                }

                sharedViewModel.conversationsWithUtterances = conversationsWithUtterances
            }
        }

        binding.setLifecycleOwner(this)

        binding.viewModel = viewModel

        return binding.root
    }
}