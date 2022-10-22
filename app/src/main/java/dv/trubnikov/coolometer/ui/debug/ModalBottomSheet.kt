package dv.trubnikov.coolometer.ui.debug

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.unsafeLazy

class ModalBottomSheet : BottomSheetDialogFragment() {

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
        setupRecycler()
    }

    private fun setupRecycler() {
        val sendNotification = DebugItem.Button(R.string.debug_panel_fake_notification, R.drawable.ic_notification) {
            Toast.makeText(requireContext(), "sendNotification", Toast.LENGTH_SHORT).show()
        }
        val enableButtons = DebugItem.Switch(R.string.debug_panel_enable_buttons) { isChecked ->
            Toast.makeText(requireContext(), "enableButtons-$isChecked", Toast.LENGTH_SHORT).show()

        }
        val bigTicksCount = DebugItem.Spinner(R.string.debug_panel_big_ticks_count, R.array.debug_panel_big_ticks_spinner) { value ->
            Toast.makeText(requireContext(), "bigTicksCount-$value", Toast.LENGTH_SHORT).show()
        }
        val smallTicksCount = DebugItem.Spinner(R.string.debug_panel_small_ticks_count, R.array.debug_panel_small_ticks_spinner) { value ->
            Toast.makeText(requireContext(), "smallTicksCount-$value", Toast.LENGTH_SHORT).show()
        }
        val copyToken = DebugItem.Button(R.string.debug_panel_copy_token, R.drawable.ic_copy) {
            Toast.makeText(requireContext(), "sendNotification", Toast.LENGTH_SHORT).show()
        }
        val adapter = DebugRecyclerAdapter(
            sendNotification,
            enableButtons,
            bigTicksCount,
            smallTicksCount,
            copyToken,
        )
        debugRecycler.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        debugRecycler.adapter = adapter
    }
}
