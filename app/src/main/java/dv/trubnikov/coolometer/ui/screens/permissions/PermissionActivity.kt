package dv.trubnikov.coolometer.ui.screens.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.databinding.ActivityPermissionBinding
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.notifications.Channel
import dv.trubnikov.coolometer.ui.screens.main.MainActivity


class PermissionActivity : AppCompatActivity() {

    private val viewBinding by unsafeLazy { ActivityPermissionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        showRationText()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        setupViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUTO_PERMISSION_REQUEST_CODE) {
            if ((grantResults.getOrNull(0) == PERMISSION_GRANTED)) {
                openMainActivity()
            } else {
                @SuppressLint("InlinedApi")
                val isTheFirstRequest = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                if (isTheFirstRequest) {
                    showAngryRationText()
                } else {
                    showVeryAngryRationText()
                }
            }
        }
    }

    private fun setupViews() {
        when {
            checkNotificationPermission(this) -> {
                openMainActivity()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showAngryRationText()
            }
            else -> {
                showRationText()
            }
        }
    }

    private fun setupListeners() {
        with(viewBinding) {
            buttonSettings.setOnClickListener {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            buttonYes.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), AUTO_PERMISSION_REQUEST_CODE)
                } else {
                    openMainActivity()
                }
            }
            buttonNo.setOnClickListener {
                showAngryRationText()
            }
        }
    }

    private fun showRationText() {
        with(viewBinding) {
            buttonSettings.isVisible = false
            buttonYes.isVisible = true
            buttonNo.isVisible = true
            icon.setImageResource(R.drawable.ic_notification_on)
            rationalTitle.setText(R.string.permissions_rational_title)
            rationalText.setText(R.string.permissions_rational_text)
        }
    }

    private fun showAngryRationText() {
        with(viewBinding) {
            buttonSettings.isVisible = false
            buttonYes.isVisible = true
            buttonNo.isVisible = false
            icon.setImageResource(R.drawable.ic_notification_on)
            rationalTitle.setText(R.string.permissions_rational_angry_title)
            rationalText.setText(R.string.permissions_rational_angry_text)
        }
    }

    private fun showVeryAngryRationText() {
        with(viewBinding) {
            buttonSettings.isVisible = true
            buttonYes.isVisible = true
            buttonNo.isVisible = false
            icon.setImageResource(R.drawable.ic_notification_off)
            rationalTitle.setText(R.string.permissions_rational_very_angry_title)
            rationalText.setText(R.string.permissions_rational_very_angry_text)
        }
    }

    private fun openMainActivity() {
        for (channel in Channel.values()) {
            channel.init(this)
        }
        val intent = MainActivity.intentForActivity(this)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val AUTO_PERMISSION_REQUEST_CODE = 100

        fun intentForActivity(context: Context): Intent {
            return Intent(context, PermissionActivity::class.java)
        }

        fun checkNotificationPermission(context: Context): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
        }
    }
}