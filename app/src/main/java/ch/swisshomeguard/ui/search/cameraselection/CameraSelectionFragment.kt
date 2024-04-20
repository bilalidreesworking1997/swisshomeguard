package ch.swisshomeguard.ui.search.cameraselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ch.swisshomeguard.R
import ch.swisshomeguard.data.DummyDataRepository
import ch.swisshomeguard.data.Result
import kotlinx.android.synthetic.main.fragment_camera_selection.*

class CameraSelectionFragment : DialogFragment() {

    private lateinit var cameraSelectionViewModel: CameraSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val camerasRepositoryImpl = DummyDataRepository()
        cameraSelectionViewModel = ViewModelProvider(
            this,
            CameraSelectionViewModel.Factory(camerasRepositoryImpl)
        ).get(CameraSelectionViewModel::class.java)

        return inflater.inflate(R.layout.fragment_camera_selection, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val cameraSelectionAdapter = CameraSelectionAdapter(cameraSelectionViewModel)
        cameraSelectionList.adapter = cameraSelectionAdapter
        cameraSelectionList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        cameraSelectionViewModel.cameras.observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> {
                    //TODO hide loading and error message
//                    progress_bar_now_playing.visibility = View.GONE
//                    retry_button_now_playing.visibility = View.GONE
                    cameraSelectionAdapter.submitList(it.data)
                }
                is Result.Loading -> {
                    //TODO Show loading
//                    progress_bar_now_playing.visibility = View.VISIBLE
//                    retry_button_now_playing.visibility = View.GONE
                }
                is Result.Error -> {
                    //TODO show error message
                    //TODO show retry button
                    //TODO hide loading
                }
            }
        })
    }
}