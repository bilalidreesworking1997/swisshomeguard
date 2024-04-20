package ch.swisshomeguard.ui.cameras

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import ch.swisshomeguard.R
import ch.swisshomeguard.ServiceLocator
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.cameras.CamerasViewModel.ListLayout
import ch.swisshomeguard.utils.EventObserver
import kotlinx.android.synthetic.main.fragment_cameras.*
import kotlinx.android.synthetic.main.loading_or_error.*

class CamerasFragment : Fragment() {

    private lateinit var camerasViewModel: CamerasViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val repository = ServiceLocator.provideRepository()
        camerasViewModel = ViewModelProvider(
            requireActivity(),
            CamerasViewModel.Factory(repository)
        ).get(CamerasViewModel::class.java)

        return inflater.inflate(R.layout.fragment_cameras, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val singleColumnIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_view_single_columns, null)
        val multipleColumnsIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_view_columns, null)

        viewIcon.setOnClickListener {
            camerasViewModel.toggleListLayout()
        }

        val camerasAdapter = CamerasAdapter(camerasViewModel)
        camerasList.adapter = camerasAdapter

        camerasViewModel.listLayout.observe(viewLifecycleOwner, {
            when (it) {
                ListLayout.SINGLE_COLUMN -> {
                    viewIcon.setImageDrawable(multipleColumnsIcon)
                    camerasList.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }
                ListLayout.TWO_COLUMN -> {
                    viewIcon.setImageDrawable(singleColumnIcon)
                    camerasList.layoutManager = GridLayoutManager(requireContext(), 2)
                }
            }
        })

        camerasViewModel.selectedSystemResult.observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> {
                    showDataViews()
                    val selectedSystem = it.data
                    camerasViewModel.navigateToCameraDetailsViewModelEvent.observe(
                        viewLifecycleOwner,
                        EventObserver { systemDevice ->
                            findNavController().navigate(
                                // TODO allow or restrict null values
                                CamerasFragmentDirections.actionNavigationCamerasToCameraDetailsFragment(
                                    systemDevice.urls.stream2,
                                    systemDevice.urls.preview,
                                    systemDevice.id,
                                    selectedSystem.id
                                )
                            )
                        })
                }
                is Result.Loading -> {
                    showLoading()
                }
                is Result.Error -> {
                    showError(it)
                }
            }
        })

        camerasViewModel.camerasResult.observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> {
                    Log.d("CAMERAS_TAG", it.toString())
                    camerasAdapter.submitList(it.data)
                }
                is Result.Loading -> {}
                is Result.Error -> {}
            }
        })

        retryButton.setOnClickListener {
            camerasViewModel.fetchSystems()
        }
    }

    private fun showDataViews() {
        camerasList.visibility = View.VISIBLE
        loadingOrErrorLayout.visibility = View.GONE
    }

    private fun showLoading() {
        camerasList.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        retryButton.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showError(it: Result.Error) {
        camerasList.visibility = View.GONE
        progressBar.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        errorMessage.visibility = View.VISIBLE
        errorMessage.text = it.exception.message
    }
}