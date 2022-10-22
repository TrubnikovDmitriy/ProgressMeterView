package dv.trubnikov.coolometer.ui.debug.viewholders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dv.trubnikov.coolometer.databinding.VhDebugSwitchBinding
import dv.trubnikov.coolometer.tools.layoutInflater
import dv.trubnikov.coolometer.ui.debug.DebugItem

class DebugSwitchViewHolder private constructor(
    private val item: VhDebugSwitchBinding
) : ViewHolder(item.root) {

    constructor(parent: ViewGroup) : this(
        VhDebugSwitchBinding.inflate(parent.layoutInflater(), parent, false)
    )

    init {
        item.root.setOnClickListener {
            item.debugSwitch.performClick()
        }
    }

    fun onBind(type: DebugItem.Switch) {
        item.debugButtonText.setText(type.text)
        item.debugSwitch.setOnClickListener {
            type.listener.onSwitch(item.debugSwitch.isChecked)
        }
    }
}
