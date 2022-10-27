package dv.trubnikov.coolometer.ui.screens.history

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dv.trubnikov.coolometer.databinding.VhHistoryTableItemBinding
import dv.trubnikov.coolometer.tools.layoutInflater
import dv.trubnikov.coolometer.tools.toSignString
import java.text.SimpleDateFormat

class HistoryViewHolder private constructor(
    private val binding: VhHistoryTableItemBinding
) : ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        VhHistoryTableItemBinding.inflate(parent.layoutInflater(), parent, false)
    )

    private val dateFormatter = SimpleDateFormat("HH:mm dd.MM.yyyy", itemView.resources.configuration.locales[0])

    fun bind(item: HistoryItem) {
        binding.score.text = item.score.toSignString()
        binding.text.text = item.text
        binding.date.text = dateFormatter.format(item.date)
        val color = ContextCompat.getColor(itemView.context, item.color)
        binding.root.setBackgroundColor(color)
    }
}
