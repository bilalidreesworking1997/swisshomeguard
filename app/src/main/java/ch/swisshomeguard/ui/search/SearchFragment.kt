package ch.swisshomeguard.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import ch.swisshomeguard.R
import ch.swisshomeguard.ServiceLocator
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.ui.cameras.CamerasViewModel
import ch.swisshomeguard.ui.player.PlayerActivity
import ch.swisshomeguard.ui.search.SearchViewModel.ListLayout
import ch.swisshomeguard.utils.EventObserver
import ch.swisshomeguard.utils.convertDateInMillisToLocal
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.loading_or_error.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import kotlin.properties.Delegates
import android.widget.Toast

import android.content.DialogInterface


class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var camerasViewModel: CamerasViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var newArr: Array<String>

    private var systemId by Delegates.notNull<Int>()
    private var startDateInMillis: Long? = null
    private var endDateInMillis: Long? = null
    private var selectedCamerasIds = emptyList<Int>().toMutableList()
    private var fetchEventsJob: Job? = null
    private var selectedIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val repository = ServiceLocator.provideRepository()
        searchViewModel = ViewModelProvider(
            this,
            SearchViewModel.Factory(repository)
        ).get(SearchViewModel::class.java)
        camerasViewModel = ViewModelProvider(
            requireActivity(),
            CamerasViewModel.Factory(repository)
        ).get(CamerasViewModel::class.java)

        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val singleColumnIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_view_single_columns, null)
        val multipleColumnsIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_view_columns, null)

        searchViewIcon.setOnClickListener {
            searchViewModel.changeListLayout()
        }

        searchAdapter = SearchAdapter(searchViewModel)

        searchList.adapter = searchAdapter.withLoadStateHeaderAndFooter(
            header = SearchLoadStateAdapter { searchAdapter.retry() },
            footer = SearchLoadStateAdapter { searchAdapter.retry() }
        )

        searchAdapter.addLoadStateListener { loadState ->
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

        searchViewModel.listLayout.observe(viewLifecycleOwner, {
            when (it) {
                ListLayout.SINGLE_COLUMN -> {
                    searchViewIcon.setImageDrawable(multipleColumnsIcon)
                    searchList.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }
                ListLayout.TWO_COLUMN -> {
                    searchViewIcon.setImageDrawable(singleColumnIcon)
                    searchList.layoutManager = GridLayoutManager(requireContext(), 2)
                }
            }
        })

        searchViewModel.navigateToPlayer.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                findNavController().navigate(
                    SearchFragmentDirections.actionNavigationSearchToPlayerActivity(
                        videoType = PlayerActivity.VideoType.EVENT,
                        liveStream = it.data.first,
                        eventStreams = it.data.second.toTypedArray()
                    )
                )
            }
        })

        searchViewModel.camerasFilterResult.observe(viewLifecycleOwner, { tripleResult ->
            when (tripleResult) {
                is Result.Success -> {
                    val ids = emptyList<Int>().toMutableList()
                    val names = emptyList<String>().toMutableList()
                    val checks = emptyList<Boolean>().toMutableList()
                    val systemId = tripleResult.data.first
                    tripleResult.data.second.forEach {
                        ids.add(it.id)
                        names.add(it.name)
                    }
                    tripleResult.data.third.forEach {
                        checks.add(it)
                    }
                    cameraCount.text = checks.count { it }.toString()
                    cameraFilterIcon.setOnClickListener {
                        setUpCameraFilterDialog(
                            systemId,
                            names.toTypedArray(),
                            ids.toTypedArray(),
                            checks.toBooleanArray()
                        )
                    }
                }
                is Result.Loading -> {
                    // TODO
                }
                is Result.Error -> {
                    // TODO
                }
            }
        })

        searchViewModel.datesFilterResult.observe(viewLifecycleOwner, { tripleResult ->
            when (tripleResult) {
                is Result.Success -> {
                    systemId = tripleResult.data.first
                    startDateInMillis = tripleResult.data.second
                    endDateInMillis = tripleResult.data.third
                    setUpDateFilterDialog(systemId, startDateInMillis, endDateInMillis)
                }
                is Result.Loading -> {
                    // TODO
                }
                is Result.Error -> {
                    // TODO
                }
            }
        })

        camerasViewModel.selectedSystemResult.observe(viewLifecycleOwner, {
            when (it) {
                is Result.Success -> {
                    searchViewModel.selectSystem(it)
                    fetchEvents(it.data.id)
                }
                is Result.Loading -> {
                    showLoading()
                }
                is Result.Error -> {
                    showError(it)
                }
            }
        })

        retryButton.setOnClickListener {
            camerasViewModel.fetchSystems()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDateFilterDialog(
        systemId: Int,
        startDateInMillis: Long?,
        endDateInMillis: Long?
    ) {
        // Toolbar icons and info
        if (startDateInMillis != null && endDateInMillis != null) {
            val start = convertDateInMillisToLocal(startDateInMillis)
            val end = convertDateInMillisToLocal(endDateInMillis)
            dateFilterInfo.text = "$start\n-\n$end"
            dateFilterInfo.visibility = View.VISIBLE
            clearCalendarIcon.visibility = View.VISIBLE
            clearCalendarIcon.setOnClickListener {
                searchViewModel.setDateFilter(null, null)
                fetchEvents(systemId, null, null, selectedCamerasIds)
            }
        } else {
            dateFilterInfo.visibility = View.GONE
            clearCalendarIcon.visibility = View.GONE
        }

        // Date picker dialog
        // https://medium.com/@maithilijoshi94/new-material-date-range-picker-in-android-abd050bfc86d
        val picker = MaterialDatePicker.Builder
            .dateRangePicker()
            .build()
        picker.addOnPositiveButtonClickListener {
            val newStartDateInMillis = it.first
            val newEndDateInMillis = it.second
            Log.d(
                "DATE_TAG",
                "start timestamp: $newStartDateInMillis, end timestamp: $newEndDateInMillis"
            )
            searchViewModel.setDateFilter(newStartDateInMillis, newEndDateInMillis)
            fetchEvents(systemId, newStartDateInMillis, newEndDateInMillis, selectedCamerasIds)
        }
        calendarIcon.setOnClickListener {
            picker.show(parentFragmentManager, picker.toString())
        }
    }

    private fun setUpCameraFilterDialog(
        systemId: Int,
        cameraNames: Array<String>,
        cameraIds: Array<Int>,
        checkedItems: BooleanArray
    ) {
        val checkedItemsResult = checkedItems.copyOf()
        newArr = Arrays.copyOf(cameraNames, cameraNames.size + 1)
        newArr[cameraNames.size] = "All"
        var currentSelectedItem: Int
        if (selectedIndex == -1)
            currentSelectedItem = cameraNames.size
        else
            currentSelectedItem = selectedIndex
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.search_camera_filter_title))
            .setNegativeButton(getString(R.string.search_camera_filter_cancel)) { _, _ ->
            }
            .setPositiveButton(getString(R.string.search_camera_filter_select)) { _, _ ->
                // Do not update filter if no cameras are selected
                if (!checkedItemsResult.contains(true)) return@setPositiveButton

                searchViewModel.setCameraFilter(checkedItemsResult)
                selectedCamerasIds.clear()

                for (i in cameraNames.indices) {
                    if (checkedItemsResult[i]) {
                        selectedCamerasIds.add(cameraIds[i])
                    }
                }
                fetchEvents(systemId, startDateInMillis, endDateInMillis, selectedCamerasIds)
            }.setSingleChoiceItems(
                newArr, currentSelectedItem
            ) { dialog, item ->
                selectedIndex = item
                if (item == cameraNames.size) {
                    newArr.forEachIndexed { index, element ->
                        if (index <= cameraNames.size - 1)
                            checkedItemsResult[index] = true
                    }
                } else {
                    newArr.forEachIndexed { index, element ->
                        if (index <= cameraNames.size - 1)
                            checkedItemsResult[index] = false
                    }
                    checkedItemsResult[item] = true
                }
                searchViewModel.setCameraFilter(checkedItemsResult)
            }.show()
    }

    private fun fetchEvents(
        systemId: Int,
        startDate: Long? = null,
        endDate: Long? = null,
        systemDeviceIds: List<Int>? = null
    ) {
        fetchEventsJob?.cancel()
        fetchEventsJob = viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.fetchEvents(systemId, startDate, endDate, systemDeviceIds)
                .collectLatest {
                    searchAdapter.submitData(it)
                }
        }
    }

    private fun showDataViews() {
        searchList.visibility = View.VISIBLE
        loadingOrErrorLayout.visibility = View.GONE
    }

    private fun showLoading() {
        searchList.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        retryButton.visibility = View.GONE
        errorMessage.visibility = View.GONE
    }

    private fun showError(it: Result.Error) {
        searchList.visibility = View.GONE
        loadingOrErrorLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        retryButton.visibility = View.VISIBLE
        errorMessage.visibility = View.VISIBLE
        errorMessage.text = it.exception.message
    }
}
