package dv.trubnikov.coolometer.ui.debug

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dv.trubnikov.coolometer.ui.debug.viewholders.DebugButtonViewHolder
import dv.trubnikov.coolometer.ui.debug.viewholders.DebugSpinnerViewHolder
import dv.trubnikov.coolometer.ui.debug.viewholders.DebugSwitchViewHolder

class DebugRecyclerAdapter(
    private val items: List<DebugItem>
) : RecyclerView.Adapter<ViewHolder>() {

    constructor(vararg items: DebugItem) : this(items.toList())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = items.find { it.viewType == viewType }
        return when (requireNotNull(item)) {
            is DebugItem.Button -> DebugButtonViewHolder(parent)
            is DebugItem.Switch -> DebugSwitchViewHolder(parent)
            is DebugItem.Spinner -> DebugSpinnerViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is DebugButtonViewHolder -> holder.onBind(item as DebugItem.Button)
            is DebugSwitchViewHolder -> holder.onBind(item as DebugItem.Switch)
            is DebugSpinnerViewHolder -> holder.onBind(item as DebugItem.Spinner)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
