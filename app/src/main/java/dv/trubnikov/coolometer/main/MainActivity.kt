package dv.trubnikov.coolometer.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.cloud.CloudMessageQueue
import dv.trubnikov.coolometer.models.CloudMessage
import dv.trubnikov.coolometer.models.CloudMessageParser
import dv.trubnikov.coolometer.tools.assertFail
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val CMS_MARKER_KEY = "google.ttl"
    }

    @Inject
    lateinit var messageQueue: CloudMessageQueue

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeMessageQueue()
    }

    override fun onStart() {
        super.onStart()
        checkForNewMessage()
    }

    private fun showConfetti(message: CloudMessage) {
        Toast.makeText(this, "Конфетти! (${message.score})", Toast.LENGTH_LONG).show()
    }

    private fun checkForNewMessage() {
        if (intent.hasExtra(CMS_MARKER_KEY)) {
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