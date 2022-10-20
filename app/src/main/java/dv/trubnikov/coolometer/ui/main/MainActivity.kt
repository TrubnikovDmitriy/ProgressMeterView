package dv.trubnikov.coolometer.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.jinatonic.confetti.CommonConfetti
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.databinding.ActivityMainBinding
import dv.trubnikov.coolometer.domain.cloud.CloudMessageQueue
import dv.trubnikov.coolometer.domain.models.CloudMessage
import dv.trubnikov.coolometer.domain.models.CloudMessageParser
import dv.trubnikov.coolometer.tools.assertFail
import dv.trubnikov.coolometer.tools.reverse
import dv.trubnikov.coolometer.tools.unsafeLazy
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CMS_MARKER_KEY = "google.ttl"
    }

    @Inject
    lateinit var messageQueue: CloudMessageQueue

    private val viewModel by viewModels<MainViewModel>()
    private val viewBinding by unsafeLazy { ActivityMainBinding.inflate(layoutInflater) }

    private var sign = +1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        viewBinding.root.post {
            viewBinding.floatingText.setOnClickListener {
                viewBinding.floatingText.animateFloating(
                    viewBinding.root.width.toFloat(),
                    viewBinding.root.height.toFloat(),
                )
            }
            val random = Random(viewBinding.coolometer.hashCode())
            viewBinding.coolometer.setOnClickListener {
                val progress = viewBinding.coolometer.progress
                if (progress <= 0f || progress >= 1f) {
                    sign *= -1
                }
                viewBinding.coolometer.setProgress(progress + sign * random.nextFloat(), true)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        observeMessageQueue()
        checkForNewMessage()
    }

    private fun showConfetti(message: CloudMessage) {
        changeSizeOfConfetti()
        viewBinding.root.post {
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
                .setEmissionRate(150f)
                .enableFadeOut(DecelerateInterpolator().reverse())
                .setTouchEnabled(true)
                .setTTL(-1)
                .animate()
        }
    }

    private fun changeSizeOfConfetti() {
        CommonConfetti.rainingConfetti(viewBinding.root, intArrayOf())
        val clazz = CommonConfetti::class.java
        val field = clazz.getDeclaredField("defaultConfettiSize")
        field.isAccessible = true
        field.setInt(null, 40)
    }

    private fun checkForNewMessage() {
        if (intent.hasExtra(CMS_MARKER_KEY)) {
            intent.removeExtra(CMS_MARKER_KEY)
            val message = CloudMessageParser.parse(intent)
            if (message != null) {
                showConfetti(message)
            } else {
                val error = IllegalStateException(
                    """
                    Не удалось распарсить интент intent=[$intent], 
                    extras=[${intent?.extras?.toString()}]
                    """.trimIndent()
                )
                assertFail(error)
            }
        }
    }

    private fun observeMessageQueue() {
        lifecycleScope.launchWhenStarted {
            messageQueue.messageFlow.collect {
                showConfetti(it)
            }
        }
    }

    private fun checkForNotificationPermissions() {
        // TODO: Permissions
    }
}