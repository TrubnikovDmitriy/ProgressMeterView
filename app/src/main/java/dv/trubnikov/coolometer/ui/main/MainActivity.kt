package dv.trubnikov.coolometer.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.github.jinatonic.confetti.CommonConfetti
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.BuildConfig
import dv.trubnikov.coolometer.databinding.ActivityMainBinding
import dv.trubnikov.coolometer.domain.cloud.CloudMessageQueue
import dv.trubnikov.coolometer.domain.models.CloudMessageParser
import dv.trubnikov.coolometer.domain.models.Message
import dv.trubnikov.coolometer.tools.assertFail
import dv.trubnikov.coolometer.tools.reverse
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.debug.ModalBottomSheet
import dv.trubnikov.coolometer.ui.views.ProgressMeterView.OvershootListener
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CMS_MARKER_KEY = "google.ttl"
    }

    @Inject
    lateinit var messageQueue: CloudMessageQueue

    private val viewModel by viewModels<MainViewModel>()
    private val viewBinding by unsafeLazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        changeSizeOfConfetti()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        observeMessageQueue()
        checkForNewMessage(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForNewMessage(intent)
    }

    private fun setupListeners() {
        viewBinding.progressMeter.overshootListener = OvershootListener { forward ->
            if (forward) showConfetti()
        }
        viewBinding.root.setOnClickListener {
//            handleMessage(FakeMessage())
//            showDebugPanel()
            viewModel.onClick()
         }
        viewBinding.root.setOnLongClickListener {
            viewModel.onLongClick()
            false
         }
    }

    private fun handleMessage(message: Message) {
        with(viewBinding) {
            floatingText.text = message.shortMessage
            floatingText.animateFloating(root.width.toFloat(), progressMeter.bottom.toFloat())
            explainText.text = message.longMessage
            explainText.isVisible = true
            progressMeter.addProgress(message.score, true)
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

    private fun observeMessageQueue() {
        // TODO: replace on repeatOnLifecycle
        lifecycleScope.launchWhenStarted {
            messageQueue.messageFlow.collect {
                handleMessage(it)
            }
        }
    }

    private fun checkForNewMessage(intent: Intent?) {
        if (intent?.hasExtra(CMS_MARKER_KEY) == true) {
            intent.removeExtra(CMS_MARKER_KEY)
            val message = CloudMessageParser.parse(intent)
            if (message != null) {
                handleMessage(message)
            } else {
                val error = IllegalStateException(
                    """
                    Не удалось распарсить интент intent=[$intent], 
                    extras=[${intent.extras?.toString()}]
                    """.trimIndent()
                )
                assertFail(error)
            }
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
}