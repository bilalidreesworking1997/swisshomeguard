package ch.swisshomeguard.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.swisshomeguard.BASE_URL
import ch.swisshomeguard.R
import ch.swisshomeguard.model.events.Event
import ch.swisshomeguard.utils.Headers
import ch.swisshomeguard.utils.convertTimeDateSingleLine
import com.aminography.redirectglide.GlideApp
import kotlinx.android.synthetic.main.loading_or_error_item.view.*
import kotlinx.android.synthetic.main.search_item.view.*

class SearchAdapter(private val viewModel: SearchViewModel) :
    PagingDataAdapter<Event, SearchAdapter.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.search_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                val event = getItem(layoutPosition)
                if (event != null) {
                    viewModel.selectEvent(event)
                }
            }
        }

        fun bind(item: Event) = with(itemView) {
            searchEventDateAndTime.text = convertTimeDateSingleLine(context, item.eventCreatedAt)
            item.eventRecordings.let { eventRecordings ->
                if (eventRecordings.isNotEmpty()) {
                    showPreviewImage(eventRecordings[0].urls.preview)
                } else {
                    showPreviewImage(null)
                }
            }
        }

        private fun showPreviewImage(preview: String?) {
            with(itemView) {
                GlideApp.with(context)
                    .load(Headers.getUrlWithHeaders("$BASE_URL$preview"))
                    .centerCrop()
                    .placeholder(R.drawable.ic_outline_image)
                    .error(R.drawable.ic_broken_image)
                    .into(eventImage)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}

class SearchLoadStateViewHolder(
    itemView: View,
    retry: () -> Unit
) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Loading) {
            showLoading(itemView)
        } else if (loadState is LoadState.Error) {
            showError(loadState, itemView)
        }
    }

    private fun showLoading(view: View) {
        with(view) {
            progressBar.visibility = View.VISIBLE
            retryButton.visibility = View.GONE
            errorMessage.visibility = View.GONE
        }
    }

    private fun showError(loadState: LoadState.Error, view: View) {
        with(view) {
            progressBar.visibility = View.GONE
            retryButton.visibility = View.VISIBLE
            errorMessage.visibility = View.VISIBLE
            errorMessage.text = loadState.error.localizedMessage
        }
    }
}

class SearchLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<SearchLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: SearchLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): SearchLoadStateViewHolder {
        return SearchLoadStateViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.loading_or_error_item,
                parent,
                false
            ),
            retry
        )
    }
}