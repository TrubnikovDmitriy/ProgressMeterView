package dv.trubnikov.coolometer.ui.screens.debug

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.screens.main.MainViewModel

class DebugBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: MainViewModel by unsafeLazy {
        requireActivity().viewModels<MainViewModel>().value
    }
    private val debugRecycler: RecyclerView by unsafeLazy {
        requireView().findViewById(R.id.debug_recycler)
    }

    /**
     * Prevents changing the rounded corners to flatten ones
     *
     * https://github.com/material-components/material-components-android/pull/437#issuecomment-678742683
     */
    @SuppressLint("RestrictedApi", "VisibleForTests")
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.behavior.disableShapeAnimations()
        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_debug_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler(view.context)
    }

    private fun setupRecycler(context: Context) {
        val addMessage = DebugItem.Button(R.string.debug_panel_fake_message, R.drawable.ic_message) {
            viewModel.debugAddFakeMessage()
            dismiss()
        }
        val sendNotification = DebugItem.Button(R.string.debug_panel_fake_notification, R.drawable.ic_notification) {
            viewModel.debugSendFakeNotification(context)
            dismiss()
        }
        val dropConfetti = DebugItem.Button(R.string.debug_panel_confetti, R.drawable.ic_firework) {
            viewModel.debugDropConfetti()
            dismiss()
        }
        val enableButtons = DebugItem.Switch(
            R.string.debug_panel_enable_buttons,
            viewModel.debugButtonEnable,
        ) { isChecked ->
            viewModel.debugToggleCoolButtons(isChecked)
        }
        val bigTickArray = context.resources.getIntArray(R.array.debug_panel_big_ticks_spinner)
        val bigTicksCount = DebugItem.Spinner(
            R.string.debug_panel_big_ticks_count,
            R.array.debug_panel_big_ticks_spinner
        ) { index ->
            val ticks = bigTickArray[index]
            viewModel.debugSetBigTicks(ticks)
        }
        val smallTickArray = context.resources.getIntArray(R.array.debug_panel_small_ticks_spinner)
        val smallTicksCount = DebugItem.Spinner(
            R.string.debug_panel_small_ticks_count,
            R.array.debug_panel_small_ticks_spinner
        ) { index ->
            val ticks = smallTickArray[index]
            viewModel.debugSetSmallTicks(ticks)
        }
        val copyToken = DebugItem.Button(R.string.debug_panel_copy_token, R.drawable.ic_copy) {
            viewModel.debugCopyToken(context)
        }
        val adapter = DebugRecyclerAdapter(
            addMessage,
            sendNotification,
            dropConfetti,
            enableButtons,
            bigTicksCount,
            smallTicksCount,
            copyToken,
        )
        debugRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        debugRecycler.adapter = adapter
    }
}
