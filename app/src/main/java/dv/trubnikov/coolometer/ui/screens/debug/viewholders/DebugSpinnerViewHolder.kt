package dv.trubnikov.coolometer.ui.screens.debug.viewholders

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dv.trubnikov.coolometer.databinding.VhDebugSpinnerBinding
import dv.trubnikov.coolometer.tools.layoutInflater
import dv.trubnikov.coolometer.ui.screens.debug.DebugItem

class DebugSpinnerViewHolder private constructor(
    private val item: VhDebugSpinnerBinding
) : ViewHolder(item.root) {

    constructor(parent: ViewGroup) : this(
        VhDebugSpinnerBinding.inflate(parent.layoutInflater(), parent, false)
    )

    init {
        item.root.setOnClickListener {
            item.debugSpinner.performClick()
        }
    }

    fun onBind(type: DebugItem.Spinner) {
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            itemView.context, type.array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        item.debugButtonText.setText(type.text)
        item.debugSpinner.adapter = adapter
        adapter.notifyDataSetChanged()
        item.debugSpinner.setSelection(0, false)
        item.debugSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(a: AdapterView<*>?, b: View?, position: Int, c: Long) {
                type.listener.onItemClick(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }
}
