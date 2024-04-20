package ch.swisshomeguard.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.swisshomeguard.R
import ch.swisshomeguard.model.status.SystemNotes
import kotlinx.android.synthetic.main.system_notes_item.view.*

class SystemNotesAdapter(private val viewModel: HomeViewModel) :
    ListAdapter<SystemNotes, SystemNotesAdapter.ViewHolder>(EventDiffCallback()) {

    init {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.system_notes_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val infoOK = ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_ok, null)
        private val infoWarn =
            ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_warning, null)
        private val infoError =
            ResourcesCompat.getDrawable(itemView.resources, R.drawable.ic_error, null)

        fun bind(item: SystemNotes) = with(itemView) {
            when (item.type) {
                "info" -> systemNotesImage.setImageDrawable(infoOK)
                "warn" -> systemNotesImage.setImageDrawable(infoWarn)
                "error" -> systemNotesImage.setImageDrawable(infoError)
            }
            systemNotesItem.text = item.text
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<SystemNotes>() {
    override fun areItemsTheSame(oldItem: SystemNotes, newItem: SystemNotes): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SystemNotes, newItem: SystemNotes): Boolean {
        return oldItem == newItem
    }
}