package dv.trubnikov.coolometer.ui.screens.history

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.databinding.ActivityHistoryBinding
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.screens.history.HistoryViewModel.State
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {

    private val viewModel by viewModels<HistoryViewModel>()
    private val viewBinding by unsafeLazy { ActivityHistoryBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        setupTableHeader()
        setupObservers()
        setupRecycler()
    }

    private fun setupRecycler() {
        val recycler = viewBinding.layoutContent.historyTable
        val adapter = HistoryAdapter()

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.filterIsInstance<State.Success>().collect { state ->
                    adapter.setItems(state.items)
                }
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun handleState(state: State) {
        val isError = (state === State.Error) || (state === State.Empty)
        viewBinding.layoutError.root.isVisible = isError
        viewBinding.layoutContent.root.isVisible = !isError
        when (state) {
            is State.Success -> with(viewBinding.layoutContent) {
                root.isVisible = false
                root.isVisible = true
                loader.isVisible = false
                historyTable.isVisible = true
            }
            State.Loading -> with(viewBinding.layoutContent) {
                root.isVisible = false
                root.isVisible = true
                loader.isVisible = true
                historyTable.isVisible = false
            }
            State.Empty -> with(viewBinding.layoutError) {
                errorText.setText(R.string.history_table_header_empty)
                errorImage.setImageResource(R.drawable.image_no_data)
            }
            State.Error -> with(viewBinding.layoutError) {
                errorText.setText(R.string.general_error_text)
                errorImage.setImageResource(R.drawable.image_error_robot)
            }
        }
    }

    private fun setupTableHeader() {
        with(viewBinding.layoutContent.tableHeader) {
            score.setText(R.string.history_table_header_score)
            text.setText(R.string.history_table_header_text)
            date.setText(R.string.history_table_header_date)
            val textViews = listOf(score, text, date)
            val color = ContextCompat.getColor(this@HistoryActivity, R.color.primaryColor)
            for (textView in textViews) {
                textView.setTypeface(null, Typeface.BOLD)
                textView.setBackgroundColor(color)
            }
        }
    }

    companion object {
        fun intentForActivity(context: Context): Intent {
            return Intent(context, HistoryActivity::class.java)
        }
    }
}
