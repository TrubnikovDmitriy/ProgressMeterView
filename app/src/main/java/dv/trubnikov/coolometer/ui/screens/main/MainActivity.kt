package dv.trubnikov.coolometer.ui.screens.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
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
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.databinding.ActivityMainBinding
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.reverse
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.screens.debug.DebugBottomSheet
import dv.trubnikov.coolometer.ui.screens.debug.SecretClickListener
import dv.trubnikov.coolometer.ui.screens.debug.SecretClickListener.Tap
import dv.trubnikov.coolometer.ui.screens.history.HistoryActivity
import dv.trubnikov.coolometer.ui.screens.main.MainViewModel.Action
import dv.trubnikov.coolometer.ui.screens.main.MainViewModel.State
import dv.trubnikov.coolometer.ui.screens.permissions.PermissionActivity
import dv.trubnikov.coolometer.ui.screens.permissions.PermissionActivity.Companion.checkNotificationPermission
import dv.trubnikov.coolometer.ui.views.ProgressMeterView.OvershootListener
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val viewBinding by unsafeLazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        checkForNotificationPermissions()
        observeState()
        changeSizeOfConfetti()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onMessageFromNotification(intent)
        intent = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.onMessageFromNotification(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        with(viewBinding.layoutContent) {
            progressMeter.overshootListener = OvershootListener { forward ->
                if (forward) showConfetti()
            }
            fab.setOnClickListener {
                viewModel.onFabClick()
            }
            historyFab.setOnClickListener {
                val intent = HistoryActivity.intentForActivity(this@MainActivity)
                startActivity(intent)
            }
            debugCoolButton.setOnClickListener {
                val score = debugCoolNumber.text.toString().toIntOrNull()
                if (score != null) {
                    progressMeter.addProgress(score, animate = true)
                }
            }
            val debugTaps = listOf(Tap.TOP, Tap.BOTTOM, Tap.LEFT, Tap.RIGHT)
            val debugClickListener = SecretClickListener(
                debugTaps, { showHintForDebugPanel() }, { openDebugPanel() }
            )
            debugListener.setOnTouchListener(debugClickListener)
        }
    }

    private fun handleMessage(message: Message) {
        val progressMeter = viewBinding.layoutContent.progressMeter
        val isReceived = progressMeter.addProgress(message.score, true)
        if (isReceived) {
            viewModel.markAsReceived(this, message)
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
        viewBinding.layoutError.root.isVisible = state === State.Error
        viewBinding.layoutContent.root.isVisible = state !== State.Error
        when (state) {
            is State.Success -> with(viewBinding.layoutContent) {
                progressMeter.progress = state.progress
                progressMeter.totalProgress = state.totalProgress
                progressMeter.bigTickCount = state.bigTicks
                progressMeter.smallTickCount = state.smallTicks
                progressMeter.isVisible = true
                debugCoolEdit.isVisible = state.debugButtonEnable
                debugCoolButton.isVisible = state.debugButtonEnable
                fab.isVisible = state.unreceivedMessages.isNotEmpty()
                fab.setIconResource(state.getIconForFab())
                fab.setText(state.getTextForFab())
                historyFab.isVisible = true
            }
            is State.Error -> with(viewBinding.layoutContent) {
                debugCoolButton.isVisible = false
                progressMeter.isVisible = false
                debugCoolEdit.isVisible = false
                historyFab.isVisible = false
                fab.isVisible = false
            }
            is State.Loading -> {
                Timber.e("На главном экране $state")
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
            Action.DebugConfetti -> showConfetti()
        }
    }

    private fun openDebugPanel() {
        viewModel.onDebugPanelOpen()
        val debugPanel = DebugBottomSheet()
        debugPanel.show(supportFragmentManager, null)
    }

    private fun showHintForDebugPanel() {
        val startAlpha = 0.5f
        val hint = viewBinding.layoutContent.debugPanelHint
        val alpha = ObjectAnimator.ofFloat(hint, View.ALPHA, startAlpha, 0.0f)
        val scaleX = ObjectAnimator.ofFloat(hint, View.SCALE_X, 1.0f, 1.2f)
        val scaleY = ObjectAnimator.ofFloat(hint, View.SCALE_Y, 1.0f, 1.2f)

        alpha.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
                hint.alpha = startAlpha
                hint.isVisible = true
            }
            override fun onAnimationEnd(animation: Animator) {
                hint.isVisible = false
            }
        })

        val animators = listOf(alpha, scaleX, scaleY)
        for (animator in animators) {
            animator.interpolator = DecelerateInterpolator()
            animator.duration = HINT_DURATION_MS
            animator.start()
        }
    }

    private fun checkForNotificationPermissions() {
        if (!checkNotificationPermission(this)) {
            val permissionActivity = PermissionActivity.intentForActivity(this)
            startActivity(permissionActivity)
            finish()
        }
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

    companion object {
        private const val HINT_DURATION_MS = 750L

        fun intentForActivity(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}