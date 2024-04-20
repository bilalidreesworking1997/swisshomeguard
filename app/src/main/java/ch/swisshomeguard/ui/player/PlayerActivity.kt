package ch.swisshomeguard.ui.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navArgs
import ch.swisshomeguard.BASE_URL
import ch.swisshomeguard.PLAYER_TAG
import ch.swisshomeguard.R
import ch.swisshomeguard.ServiceLocator
import ch.swisshomeguard.data.Result
import ch.swisshomeguard.utils.HomeguardTokenUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource.BaseFactory
import com.google.android.exoplayer2.upstream.HttpDataSource.RequestProperties
import com.otaliastudios.zoom.ZoomSurfaceView
import com.otaliastudios.zoom.ZoomSurfaceView.Callback
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.custom_playback_control_event.*
import kotlinx.android.synthetic.main.custom_playback_control_live.*
import kotlinx.android.synthetic.main.exo_layout_view.*
import kotlinx.android.synthetic.main.exo_layout_view_live.*


class PlayerActivity : AppCompatActivity() {
    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var playbackStateListener: PlaybackStateListener? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var eventStreams: Array<String>? = null
    private var liveStream: String? = null
    private lateinit var videoType: VideoType
    private lateinit var playerViewModel: PlayerViewModel
    private val args: PlayerActivityArgs by navArgs()
    private var currentlyPlayingMediaItem: MediaItem? = null

    //region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("VIDEO_TAG", "Activity: onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        videoType = args.videoType
        eventStreams = args.eventStreams
        currentWindow = args.eventPosition
        liveStream = args.liveStream

        playbackStateListener = PlaybackStateListener()

        liveCloseIcon.setOnClickListener {
            finish()
        }

        eventCloseIcon.setOnClickListener {
            finish()
        }

        snapshotButton.setOnClickListener {
            eventPlayerView.videoSurfaceView.let {
                if (it is SurfaceView) {
                    makeVideoScreenshot(it) { bitmap ->
                        if (bitmap != null) {
                            shareImage(this@PlayerActivity, bitmap)
                        } else {
                            Toast.makeText(
                                this,
                                "Error while creating snapshot",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        saveButton.setOnClickListener {
            saveVideoWithPermissions(this, currentlyPlayingMediaItem?.mediaId)
        }

        val repository = ServiceLocator.provideRepository()
        playerViewModel = ViewModelProvider(this, PlayerViewModel.Factory(repository))[PlayerViewModel::class.java]

        playerViewModel.video.observe(this) {
            Log.d("VIDEO_TAG", "Activity: $it")
            when (it) {
                is Result.Success -> {
                    Log.d("VIDEO_LOAD_TAG", "Playing live video")
                    progressBar.visibility = View.GONE
                    val updatedLink = "${it.data.streamUrl}?token=${HomeguardTokenUtils.readHomeguardToken()}&channel=1"
                    prepareSingleHLSMediaItem(updatedLink)
                    playerViewModel.startKeepAlive(
                        it.data.keepAliveUrl,
                        it.data.keepAliveInterval
                    )
                }

                is Result.Loading -> {
                    Log.d("VIDEO_LOAD_TAG", "Loading live video")
                    progressBar.visibility = View.VISIBLE
                }

                is Result.Error -> {
                    Log.d("VIDEO_LOAD_TAG", "Error loading live video")
                    Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        return true
    }

    override fun onStart() {
        Log.d("VIDEO_TAG", "Activity: onStart")
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun onStop() {
        Log.d("VIDEO_TAG", "Activity: onStop")
        super.onStop()
        releasePlayer()
    }
    //endregion

    //region Player
    private fun initializePlayer() {
        createSimplePlayer()
        playWhenReady()

        when (videoType) {
            VideoType.LIVE -> {
                showLiveControls()
                liveStream?.let { liveStream ->
                    Log.d("VIDEO_TAG", "Activity: Fetch video")
                    playerViewModel.fetchVideo(liveStream)
                } ?: Toast.makeText(this, "Stream not available", Toast.LENGTH_SHORT).show()
            }
            VideoType.EVENT -> {
                showEventControls()
                eventStreams?.let { streams ->
                    val authenticatedMediaSources = emptyList<MediaSource>().toMutableList()
                    streams.forEach {
                        val completeMP4Url = "$BASE_URL$it"
                        val authenticatedMediaSource =
                            buildAuthenticatedMediaSource(Uri.parse(completeMP4Url))
                        authenticatedMediaSources.add(authenticatedMediaSource)
                    }
                    prepareMultipleMP4MediaSources(authenticatedMediaSources)
                } ?: Toast.makeText(this, "Stream not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLiveControls() {
        eventPlayerView.visibility = View.GONE

        livePlayerView.player = player
        livePlayerView.visibility = View.VISIBLE
        scaleGestureDetector =
            ScaleGestureDetector(this, CustomOnScaleGestureListener())
    }

    private fun showEventControls() {
        livePlayerView.visibility = View.GONE

        eventPlayerView.player = player
        eventPlayerView.visibility = View.VISIBLE
        scaleGestureDetector =
            ScaleGestureDetector(this, CustomOnScaleGestureListener())
    }

    private fun playWhenReady() {
        Log.d("VIDEO_TAG", "Activity: playWhenReady")
        player!!.prepare()
        player!!.addListener(playbackStateListener!!)
        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow, playbackPosition)
    }

    private fun createSimplePlayer() {
        Log.d("VIDEO_TAG", "Activity: Create player")
        player = SimpleExoPlayer.Builder(this).build()
    }

    private fun createSimplePlayerWithFlags() {
        // SimpleExoPlayer with specific flags for HLS playback with TS
        // To be used only if createSimplePlayer() does not work
        val extractorsFactory = DefaultExtractorsFactory()
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS)
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
        player = SimpleExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this, extractorsFactory)
            )
            .build()
    }

    private fun prepareSingleHLSMediaItem(videoStreamUrl: String) {
        val mediaItem = MediaItem.fromUri(videoStreamUrl)
        player!!.setMediaItem(mediaItem)

    }

    private fun prepareSingleMP4MediaItem(streamUrl: String) {
        val mediaItem = MediaItem.fromUri(streamUrl)
        player!!.setMediaItem(mediaItem)
    }

    private fun prepareSingleMP4MediaSource(mediaSource: MediaSource) {
        player!!.setMediaSource(mediaSource)
    }

    private fun prepareMultipleMP4MediaSources(mediaSources: List<MediaSource>) {
        player!!.clearMediaItems()
        mediaSources.forEach {
            player!!.addMediaSource(it)
        }
    }

    private fun prepareMultipleMP4MediaItems() {
        val mediaItem1 = MediaItem.fromUri(getString(R.string.dummy_mp4))
        val mediaItem2 = MediaItem.fromUri(getString(R.string.dummy_mp4))
        val mediaItem3 = MediaItem.fromUri(getString(R.string.dummy_mp4))
        player!!.setMediaItem(mediaItem1)
        player!!.addMediaItem(mediaItem2)
        player!!.addMediaItem(mediaItem3)
    }

    private fun releasePlayer() {
        Log.d("VIDEO_TAG", "Activity: Release player")
        if (videoType == VideoType.LIVE) playerViewModel.stopKeepAlive()

        playWhenReady = player!!.playWhenReady
        playbackPosition = player!!.currentPosition
        currentWindow = player!!.currentWindowIndex

        player!!.removeListener(playbackStateListener!!)
        player!!.release()
        player = null
    }

    // TODO update code to replace deprecated items
    // https://stackoverflow.com/a/55334822/5987516
    private fun buildAuthenticatedMediaSource(uri: Uri): MediaSource {
        val baseFactory: BaseFactory = object : BaseFactory() {
            override fun createDataSourceInternal(defaultRequestProperties: RequestProperties): HttpDataSource {
                val dataSource = DefaultHttpDataSourceFactory().createDataSource()
                dataSource.setRequestProperty(
                    "Authorization",
                    "Bearer ${HomeguardTokenUtils.readHomeguardToken()}"
                )
                return dataSource
            }
        }
        val emf = ExtractorMediaSource.Factory(baseFactory)
        return emf.createMediaSource(uri)
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    //endregion

    //region Save Video
    // https://developer.android.com/training/permissions/requesting
    private fun saveVideoWithPermissions(context: Context, url: String?) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // https://developer.android.com/reference/android/app/DownloadManager.Request.html#setDestinationInExternalPublicDir(java.lang.String,%20java.lang.String)
                // For applications targeting Build.VERSION_CODES.Q or above,
                // WRITE_EXTERNAL_STORAGE permission is not needed and the dirType must be one of
                // the known public directories like Environment#DIRECTORY_DOWNLOADS, Environment#DIRECTORY_PICTURES, Environment#DIRECTORY_MOVIES, etc.
                saveVideo(context, url)
            }
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission had already been granted
                saveVideo(context, url)
            }
            else -> {
                // Ask for the permission
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSIONS_CODE_WRITE_STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                saveVideo(this, currentlyPlayingMediaItem?.mediaId)
            } else {
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                Toast.makeText(
                    this,
                    "Video cannot be saved without required permission",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO open app settings screen
            }
            return
        }
    }

    private fun saveVideo(context: Context, url: String?) {
        if (url != null) {
            Toast.makeText(this, getString(R.string.player_saving_video), Toast.LENGTH_SHORT).show()
            saveVideoToDownloads(context, url)
        } else {
            Toast.makeText(this, "Error saving video", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    //region Debugging
    private inner class PlaybackStateListener : Player.EventListener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            currentlyPlayingMediaItem = mediaItem
        }

        override fun onPlaybackStateChanged(state: Int) {
            val stateString: String = when (state) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(PLAYER_TAG, "changed state to $stateString")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                Log.d(PLAYER_TAG, "Is playing: Yes")
                logTrackInfo()
            } else {
                Log.d(PLAYER_TAG, "Is playing: No")
            }
        }

        override fun onPlayerError(e: ExoPlaybackException) {
            if (isBehindLiveWindow(e)) {
                // Re-initialize player at the live edge.
                Log.d(PLAYER_TAG, "Player behind live window")
                Log.d(PLAYER_TAG, e.toString())
            } else {
                // Handle other errors
                Log.d(PLAYER_TAG, "Player error")
                Log.d(PLAYER_TAG, e.toString())
            }
        }

        private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
            if (e.type != ExoPlaybackException.TYPE_SOURCE) {
                return false
            }
            var cause: Throwable? = e.sourceException
            while (cause != null) {
                if (cause is BehindLiveWindowException) {
                    return true
                }
                cause = cause.cause
            }
            return false
        }
    }

    private fun logTrackInfo() {
        val currentTrackGroups = player!!.currentTrackGroups
        Log.d(PLAYER_TAG, currentTrackGroups.toString())
        val currentTrackSelections = player!!.currentTrackSelections
        Log.d(PLAYER_TAG, currentTrackSelections.toString())
    }
    //endregion

    enum class VideoType { LIVE, EVENT }

    companion object {
        const val REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 123
    }
}