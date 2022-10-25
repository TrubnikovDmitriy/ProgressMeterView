package dv.trubnikov.coolometer.ui.screens.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.jinatonic.confetti.CommonConfetti
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.BuildConfig
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.databinding.ActivityMainBinding
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.reverse
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.screens.debug.ModalBottomSheet
import dv.trubnikov.coolometer.ui.screens.main.MainViewModel.Action
import dv.trubnikov.coolometer.ui.screens.main.MainViewModel.State
import dv.trubnikov.coolometer.ui.views.ProgressMeterView.OvershootListener
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val viewBinding by unsafeLazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        observeState()
        changeSizeOfConfetti()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onMessageFromNotification(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onMessageFromNotification(intent)
    }

    private fun setupListeners() {
        viewBinding.progressMeter.overshootListener = OvershootListener { forward ->
            if (forward) showConfetti()
        }
        viewBinding.fab.setOnClickListener {
            viewModel.onFabClick()
        }
    }

    private fun handleMessage(message: Message) {
        with(viewBinding) {
            val isReceived = progressMeter.addProgress(message.score, true)
            if (isReceived) {
                explainText.text = message.text
                explainText.isVisible = true
                viewModel.markAsReceived(message)
            }
        }
    }

    private fun showConfetti() {
        val colors = intArrayOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW
        )
        val confetti = CommonConfetti.rainingConfetti(viewBinding.root, colors)

        confetti.confettiManager
            .setNumInitialCount(0)
            .setEmissionDuration(5_000)
            .setEmissionRate(100f)
            .enableFadeOut(DecelerateInterpolator().reverse())
            .setTouchEnabled(true)
            .setTTL(-1)
            .animate()
    }

    private fun observeState() {
        // TODO: replace on normal repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { state ->
                    handleState(state)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionFlow.collect { state ->
                    handleAction(state)
                }
            }
        }
    }

    private fun handleState(state: State) {
        when (state) {
            is State.Success -> with(viewBinding) {
                progressMeter.progress = state.progress
                progressMeter.totalProgress = state.totalProgress
                progressMeter.bigTickCount = state.bigTicks
                progressMeter.smallTickCount = state.smallTicks
                progressMeter.isVisible = true
                fab.isVisible = state.unreceivedMessages.isNotEmpty()
                fab.setIconResource(state.getIconForFab())
                fab.setText(state.getTextForFab())
            }
            is State.Error -> with(viewBinding) {
                progressMeter.isVisible = false
            }
            is State.Loading -> with(viewBinding) {
                progressMeter.isVisible = false
            }
        }
    }

    private fun handleAction(action: Action) {
        when (action) {
            is Action.AcceptDialog -> showAcceptMessage(action.message) { msg ->
                handleMessage(msg)
            }
            is Action.NotificationDialog -> showNewMessageDialog(action.message) { msg ->
                handleMessage(msg)
            }
            is Action.ListDialog -> showChoiceDialog(action.messages) { msg ->
                showAcceptMessage(msg) {
                    handleMessage(msg)
                }
            }
            Action.PityDialog -> showPityDialog()
        }
    }

    private fun showDebugPanel() {
        if (!BuildConfig.DEBUG) return
        val modalBottomSheet = ModalBottomSheet()
        modalBottomSheet.show(supportFragmentManager, null)
    }

    private fun checkForNotificationPermissions() {
        // TODO: Permissions
    }

    private fun changeSizeOfConfetti() {
        CommonConfetti.rainingConfetti(viewBinding.root, intArrayOf())
        val clazz = CommonConfetti::class.java
        val field = clazz.getDeclaredField("defaultConfettiSize")
        field.isAccessible = true
        field.setInt(null, 40)
    }

    @DrawableRes
    private fun State.Success.getIconForFab(): Int {
        return when (unreceivedMessages.size) {
            1 -> R.drawable.ic_msg_1
            2 -> R.drawable.ic_msg_2
            3 -> R.drawable.ic_msg_3
            4 -> R.drawable.ic_msg_4
            5 -> R.drawable.ic_msg_5
            6 -> R.drawable.ic_msg_6
            7 -> R.drawable.ic_msg_7
            8 -> R.drawable.ic_msg_8
            9 -> R.drawable.ic_msg_9
            else -> R.drawable.ic_msg_many
        }
    }

    @StringRes
    private fun State.Success.getTextForFab(): Int {
        return when (unreceivedMessages.size) {
            1 -> R.string.main_single_achievement
            else -> R.string.main_many_achievements
        }
    }
}