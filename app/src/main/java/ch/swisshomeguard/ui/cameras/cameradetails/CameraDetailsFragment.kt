package ch.swisshomeguard.ui.cameras.cameradetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import ch.swisshomeguard.BASE_URL
import ch.swisshomeguard.R
import ch.swisshomeguard.ServiceLocator
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.player.PlayerActivity
import ch.swisshomeguard.utils.EventObserver
import ch.swisshomeguard.utils.Headers
import com.aminography.redirectglide.GlideApp
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.camera_details_fragment.*
import kotlinx.android.synthetic.main.loading_or_error.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraDetailsFragment : Fragment() {

    private lateinit var cameraDetailsViewModel: CameraDetailsViewModel
    private lateinit var cameraDetailsAdapter: CameraDetailsAdapter
    private var fetchEventsJob: Job? = null

    private val args: CameraDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_details_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val repository = ServiceLocator.provideRepository()
        cameraDetailsViewModel = ViewModelProvider(
            this,
            CameraDetailsViewModel.Factory(repository, requireActivity().application)
        ).get(CameraDetailsViewModel::class.java)

        val systemId = args.systemId
        val systemDeviceId = args.systemDeviceId
        val streamChannelUrl = args.streamChannelUrl
        val cameraImageUrl = args.cameraImageUrl

        activity?.actionBar?.title = streamChannelUrl.toString()

        showPreviewImage(cameraImageUrl)

        cameraDetailImage.setOnClickListener {
            if (streamChannelUrl != null) {
                findNavController().navigate(
                    CameraDetailsFragmentDirections.actionCameraDetailsFragmentToPlayerActivity(
                        videoType = PlayerActivity.VideoType.LIVE,
                        liveStream = streamChannelUrl
                    )
                )
            } else {
                Toast.makeText(activity, "Live stream not available", Toast.LENGTH_SHORT).show()
            }
        }

        cameraDetailsList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        cameraDetailsAdapter = CameraDetailsAdapter(cameraDetailsViewModel)

        cameraDetailsList.adapter = cameraDetailsAdapter.withLoadStateHeaderAndFooter(
            header = CameraDetailsAdapter.EventLoadStateAdapter { cameraDetailsAdapter.retry() },
            footer = CameraDetailsAdapter.EventLoadStateAdapter { cameraDetailsAdapter.retry() }
        )

        cameraDetailsAdapter.addLoadStateListener { loadState ->
            loadState.refresh.let {
                when (it) {
                    is LoadState.NotLoading -> {
                        showDataViews()
                    }
                    is LoadState.Loading -> {
                        showLoading()
                    }
                    is LoadState.Error -> {
                        showError(Result.Error(Exception(it.error.localizedMessage)))
                    }
                }
            }
        }

        cameraDetailsViewModel.navigateToEventPlayer.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(
                CameraDetailsFragmentDirections.actionCameraDetailsFragmentToPlayerActivity(
                    videoType = PlayerActivity.VideoType.EVENT,
                    eventStreams = it.first.toTypedArray(),
                    eventPosition = it.second,
                    liveStream = streamChannelUrl
                )
            )
        })

        fetchEvents(systemId, systemDeviceId)

        retryButton.setOnClickListener {
            cameraDetailsAdapter.retry()
        }
    }

    private fun fetchEvents(systemId: Int, systemDeviceId: Int) {
        fetchEventsJob?.cancel()
        fetchEventsJob = viewLifecycleOwner.lifecycleScope.launch {
            cameraDetailsViewModel.fetchEvents(systemId, listOf(systemDeviceId))
                .collectLatest {
                    cameraDetailsAdapter.submitData(it)
                }
        }
    }

    private fun showPreviewImage(cameraImageUrl: String?) {
        GlideApp.with(this)
            .load(Headers.getUrlWithHeaders("$BASE_URL$cameraImageUrl"))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .centerCrop()
            .placeholder(R.drawable.ic_outline_image)
            .error(R.drawable.ic_broken_image)
            .into(cameraDetailImage)
    }

    private fun showDataViews() {
        cameraDetailsList.visibility = View.VISIBLE
        loadingOrErrorLayout.visibility = View.GONE
    }

    private fun showLoading() {
        cameraDetailsList.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        retryButton.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showError(it: Result.Error) {
        cameraDetailsList.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        retryButton.visibility = View.VISIBLE
        errorMessage.visibility = View.VISIBLE
        errorMessage.text = it.exception.message
    }
}