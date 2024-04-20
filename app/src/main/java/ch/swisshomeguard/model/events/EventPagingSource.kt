package ch.swisshomeguard.model.events

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ch.swisshomeguard.data.WebService
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1

class EventPagingSource(
    private val systemId: Int,
    private val startDateMillis: String?,
    private val endDateMillis: String?,
    private val systemDeviceIds: String?,
    private val service: WebService
) : PagingSource<Int, Event>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Event> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = service.fetchEvents(
                systemId,
                position,
                startDateMillis,
                endDateMillis,
                systemDeviceIds
            )
            val events = response.events
            LoadResult.Page(
                data = events,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (events.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    // The refresh key is used for subsequent refresh calls to PagingSource.load after the initial load
    override fun getRefreshKey(state: PagingState<Int, Event>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}