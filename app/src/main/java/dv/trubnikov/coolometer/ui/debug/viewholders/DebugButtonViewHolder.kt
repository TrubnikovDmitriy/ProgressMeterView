package dv.trubnikov.coolometer.ui.debug.viewholders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dv.trubnikov.coolometer.databinding.VhDebugButtonBinding
import dv.trubnikov.coolometer.tools.layoutInflater
import dv.trubnikov.coolometer.ui.debug.DebugItem

class DebugButtonViewHolder private constructor(
    private val item: VhDebugButtonBinding
) : ViewHolder(item.root) {

    constructor(parent: ViewGroup) : this(
        VhDebugButtonBinding.inflate(parent.layoutInflater(), parent, false)
    )

    fun onBind(type: DebugItem.Button) {
        item.debugButtonText.setText(type.text)
        item.debugIcon.setImageResource(type.icon)
        item.root.setOnClickListener {
            type.listener.onClick()
        }
    }
}
