package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CustomMusicTrack(
  val id: String,
  val title: String,
  val filePath: String,
)

enum class RepeatMode(val id: String, val label: String) {
  OFF("off", "No Repeat"),
  ALL("all", "Loop All"),
  ONE("one", "Repeat One");

  fun next(): RepeatMode =
    when (this) {
      OFF -> ALL
      ALL -> ONE
      ONE -> OFF
    }

  companion object {
    fun fromId(id: String): RepeatMode = entries.find { it.id == id } ?: ALL
  }
}

data class MusicPlaybackStatus(
  val isPlaying: Boolean = false,
  val currentTrackId: String = MusicTrack.SUNLIT_WHISKERS.id,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 20_000L,
  val repeatMode: RepeatMode = RepeatMode.ALL,
  val shuffleEnabled: Boolean = false,
  val playbackSpeed: Float = 1.0f,
  val sleepTimerRemainingMs: Long = 0L,
  val waveformBars: List<Float> = listOf(0.28f, 0.52f, 0.74f, 0.44f, 0.86f, 0.60f, 0.40f, 0.68f),
)

enum class MusicTrack(
  val id: String,
  val title: String,
  val subtitle: String,
  val bpm: Int,
  val baseNotesHz: List<Double>,
  val bassNotesHz: List<Double>,
) {
  SUNLIT_WHISKERS(
    id = "sunlit_whiskers",
    title = "Sunlit Whiskers",
    subtitle = "Warm music-box arpeggio",
    bpm = 96,
    baseNotesHz = listOf(523.25, 659.25, 783.99, 987.77, 783.99, 659.25, 587.33, 698.46),
    bassNotesHz = listOf(130.81, 130.81, 164.81, 164.81, 174.61, 174.61, 196.00, 146.83),
  ),
  MIDNIGHT_CATNIP(
    id = "midnight_catnip",
    title = "Midnight Catnip",
    subtitle = "Dreamy lo-fi night bells",
    bpm = 78,
    baseNotesHz = listOf(440.00, 523.25, 659.25, 587.33, 392.00, 493.88, 587.33, 523.25),
    bassNotesHz = listOf(110.00, 110.00, 146.83, 146.83, 98.00, 98.00, 130.81, 164.81),
  ),
  RAINY_WALTZ(
    id = "rainy_waltz",
    title = "Rainy Window Waltz",
    subtitle = "Gentle 3/4 sanctuary lullaby",
    bpm = 84,
    baseNotesHz = listOf(392.00, 493.88, 587.33, 659.25, 587.33, 493.88, 440.00, 523.25),
    bassNotesHz = listOf(98.00, 146.83, 146.83, 130.81, 196.00, 196.00, 110.00, 164.81),
  ),
  STARLIGHT_BISCUIT(
    id = "starlight_biscuit",
    title = "Starlight Biscuit",
    subtitle = "Playful celesta & soft pluck",
    bpm = 112,
    baseNotesHz = listOf(587.33, 739.99, 880.00, 1174.66, 880.00, 739.99, 659.25, 783.99),
    bassNotesHz = listOf(146.83, 146.83, 196.00, 196.00, 220.00, 220.00, 164.81, 196.00),
  );

  fun phraseStepDurationMs(playbackSpeed: Float = 1.0f): Long {
    val speed = playbackSpeed.coerceIn(0.5f, 2.0f)
    return ((60_000.0 / (bpm * 2.0)) / speed).roundToLongSafe()
  }

  fun totalPhraseSteps(): Int = baseNotesHz.size * 6 // 48 steps (~12-20 seconds per full phrase cycle)

  fun cycleDurationMs(playbackSpeed: Float = 1.0f): Long {
    return (phraseStepDurationMs(playbackSpeed) * totalPhraseSteps()).coerceAtLeast(4_000L)
  }

  companion object {
    fun fromId(id: String): MusicTrack = entries.find { it.id == id } ?: SUNLIT_WHISKERS

    private fun Double.roundToLongSafe(): Long = this.roundToInt().toLong().coerceAtLeast(1L)
  }
}

enum class CatSoundEffect {
  COZY_PURR,
  DEEP_RUMBLE_PURR,
  HAPPY_TRILL_PURR,
  GENTLE_MEW,
  PLAYFUL_CHIRP,
  SWEET_DOUBLE_MEW,
  FRENZY_EXCITED_MEOW,
  UI_BUTTON_TAP,
  UI_BUTTON_CONFIRM,
  UI_PILL_SLIDE_TICK,
}

class SoundManager(context: Context? = null) {
  private val appContext: Context? = context?.applicationContext
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val activeSfxCount = AtomicInteger(0)

  @Volatile private var sfxEnabled: Boolean = true
  @Volatile private var sfxVolume: Float = 0.8f
  @Volatile private var musicEnabled: Boolean = false
  @Volatile private var musicVolume: Float = 0.55f
  @Volatile private var selectedTrackId: String = MusicTrack.SUNLIT_WHISKERS.id
  @Volatile private var customTracks: List<CustomMusicTrack> = emptyList()
  @Volatile private var isForegrounded: Boolean = true
  @Volatile private var repeatMode: RepeatMode = RepeatMode.ALL
  @Volatile private var shuffleEnabled: Boolean = false
  @Volatile private var playbackSpeed: Float = 1.0f
  @Volatile private var sleepTimerEndMillis: Long = 0L

  @Volatile private var builtInCurrentStep: Int = 0
  @Volatile private var pendingBuiltInStep: Int? = null
  @Volatile private var onTrackCompletedCallback: (() -> Unit)? = null
  @Volatile private var onSleepTimerExpiredCallback: (() -> Unit)? = null

  private val _playbackStatus = MutableStateFlow(MusicPlaybackStatus())
  val playbackStatus: StateFlow<MusicPlaybackStatus> = _playbackStatus.asStateFlow()

  private var lastSfxIndex = -1
  private var musicJob: Job? = null
  private var monitorJob: Job? = null
  private var customMediaPlayer: MediaPlayer? = null
  private var activeCustomFilePath: String? = null
  private val mediaLock = Any()

  private var soundPool: SoundPool? = null
  private val soundIds = ConcurrentHashMap<CatSoundEffect, Int>()
  private val loadedSoundIds = ConcurrentHashMap<Int, Boolean>()

  private val prebuiltSfx: Map<CatSoundEffect, ShortArray> by lazy {
    mapOf(
      CatSoundEffect.COZY_PURR to
        synthesizeCatPurr(baseRumbleHz = 52.0, pulseHz = 25.5, durationMs = 640),
      CatSoundEffect.DEEP_RUMBLE_PURR to
        synthesizeCatPurr(baseRumbleHz = 46.0, pulseHz = 23.5, durationMs = 720),
      CatSoundEffect.HAPPY_TRILL_PURR to
        synthesizeTrillPurr(purrHz = 56.0, chirpStartHz = 480.0, durationMs = 460),
      CatSoundEffect.GENTLE_MEW to
        synthesizeCatMeow(startHz = 440.0, peakHz = 760.0, endHz = 410.0, durationMs = 340),
      CatSoundEffect.PLAYFUL_CHIRP to
        synthesizeChirp(startHz = 450.0, endHz = 920.0, durationMs = 240),
      CatSoundEffect.SWEET_DOUBLE_MEW to synthesizeDoubleMew(durationMs = 390),
      CatSoundEffect.FRENZY_EXCITED_MEOW to
        synthesizeCatMeow(startHz = 520.0, peakHz = 980.0, endHz = 540.0, durationMs = 360),
      CatSoundEffect.UI_BUTTON_TAP to
        synthesizeUiPop(startHz = 620.0, endHz = 880.0, durationMs = 42, gain = 0.34),
      CatSoundEffect.UI_BUTTON_CONFIRM to
        synthesizeUiConfirm(durationMs = 88),
      CatSoundEffect.UI_PILL_SLIDE_TICK to
        synthesizeUiPop(startHz = 1180.0, endHz = 760.0, durationMs = 24, gain = 0.28),
    )
  }

  init {
    if (appContext != null) {
      initSoundPool(appContext)
    }
    startPlaybackMonitor()
  }

  private fun initSoundPool(ctx: Context) {
    runCatching {
      val pool =
        SoundPool.Builder()
          .setMaxStreams(6)
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .build()
      pool.setOnLoadCompleteListener { _, sampleId, status ->
        if (status == 0) {
          loadedSoundIds[sampleId] = true
        }
      }
      soundPool = pool

      scope.launch(Dispatchers.IO) {
        val dir = File(ctx.cacheDir, "purr_sfx_v5").apply { mkdirs() }
        prebuiltSfx.forEach { (effect, samples) ->
          runCatching {
            val wavFile = File(dir, "${effect.name.lowercase()}.wav")
            if (!wavFile.exists() || wavFile.length() <= 44L) {
              writeWavFile(wavFile, samples, SAMPLE_RATE)
            }
            val sampleId = pool.load(wavFile.absolutePath, 1)
            if (sampleId != 0) {
              soundIds[effect] = sampleId
            }
          }.onFailure { err ->
            Log.w(TAG, "Failed preparing SFX ${effect.name}: ${err.message}")
          }
        }
      }
    }.onFailure { err ->
      Log.w(TAG, "SoundPool init unavailable, falling back to AudioTrack: ${err.message}")
    }
  }

  fun setPlaybackCallbacks(
    onTrackEnded: () -> Unit,
    onSleepTimerExpired: () -> Unit,
  ) {
    onTrackCompletedCallback = onTrackEnded
    onSleepTimerExpiredCallback = onSleepTimerExpired
  }

  fun configureAudio(
    sfxEnabled: Boolean,
    sfxVolume: Float,
    musicEnabled: Boolean,
    musicVolume: Float,
    trackId: String,
    customTracks: List<CustomMusicTrack> = emptyList(),
    repeatMode: RepeatMode = this.repeatMode,
    shuffleEnabled: Boolean = this.shuffleEnabled,
    playbackSpeed: Float = this.playbackSpeed,
  ) {
    this.sfxEnabled = sfxEnabled
    this.sfxVolume = sfxVolume.coerceIn(0f, 1f)
    val trackChanged = this.selectedTrackId != trackId
    val speedChanged = abs(this.playbackSpeed - playbackSpeed.coerceIn(0.75f, 1.5f)) > 0.02f
    this.selectedTrackId = trackId
    this.customTracks = customTracks
    this.musicVolume = musicVolume.coerceIn(0f, 1f)
    this.musicEnabled = musicEnabled
    this.repeatMode = repeatMode
    this.shuffleEnabled = shuffleEnabled
    this.playbackSpeed = playbackSpeed.coerceIn(0.75f, 1.5f)
    if (trackChanged) {
      builtInCurrentStep = 0
      pendingBuiltInStep = null
    }

    syncMusicPlaybackState(forceRestart = trackChanged && musicEnabled, speedChanged = speedChanged)
    refreshStatusSnapshot()
  }

  fun seekTo(positionMs: Long) {
    val safePos = positionMs.coerceAtLeast(0L)
    val customTrack =
      customTracks.find { it.id == selectedTrackId }?.takeIf { File(it.filePath).exists() }
    if (customTrack != null) {
      synchronized(mediaLock) {
        runCatching {
          val mp = customMediaPlayer
          if (mp != null) {
            val dur = mp.duration.toLong().coerceAtLeast(1L)
            val clamped = safePos.coerceIn(0L, dur).toInt()
            mp.seekTo(clamped)
          }
        }
      }
    } else {
      val track = MusicTrack.fromId(selectedTrackId)
      val cycleMs = track.cycleDurationMs(playbackSpeed).coerceAtLeast(1L)
      val stepCount = track.totalPhraseSteps()
      val targetStep = (((safePos.coerceIn(0L, cycleMs)).toDouble() / cycleMs) * stepCount).roundToInt()
        .coerceIn(0, stepCount - 1)
      builtInCurrentStep = targetStep
      pendingBuiltInStep = targetStep
    }
    refreshStatusSnapshot()
  }

  fun setSleepTimer(minutes: Int) {
    sleepTimerEndMillis =
      if (minutes <= 0) 0L
      else System.currentTimeMillis() + minutes * 60_000L
    refreshStatusSnapshot()
  }

  fun setForegroundState(inForeground: Boolean) {
    isForegrounded = inForeground
    syncMusicPlaybackState(forceRestart = false)
  }

  fun playCatTapSound(isComboCrit: Boolean = false) {
    if (!sfxEnabled || sfxVolume <= 0.01f) return

    val pool =
      if (isComboCrit) {
        listOf(
          CatSoundEffect.FRENZY_EXCITED_MEOW,
          CatSoundEffect.HAPPY_TRILL_PURR,
          CatSoundEffect.SWEET_DOUBLE_MEW,
          CatSoundEffect.PLAYFUL_CHIRP,
        )
      } else {
        listOf(
          CatSoundEffect.COZY_PURR,
          CatSoundEffect.DEEP_RUMBLE_PURR,
          CatSoundEffect.HAPPY_TRILL_PURR,
          CatSoundEffect.GENTLE_MEW,
          CatSoundEffect.PLAYFUL_CHIRP,
          CatSoundEffect.SWEET_DOUBLE_MEW,
        )
      }

    var pickIndex = Random.nextInt(pool.size)
    if (pool.size > 1 && pickIndex == lastSfxIndex) {
      pickIndex = (pickIndex + 1 + Random.nextInt(pool.size - 1)) % pool.size
    }
    lastSfxIndex = pickIndex
    playEffectInternal(pool[pickIndex])
  }

  fun playSpecificSfx(effect: CatSoundEffect) {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    playEffectInternal(effect)
  }

  fun playUiTap() {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    playEffectInternal(CatSoundEffect.UI_BUTTON_TAP)
  }

  fun playUiConfirm() {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    playEffectInternal(CatSoundEffect.UI_BUTTON_CONFIRM)
  }

  fun playPillSlideTick() {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    playEffectInternal(CatSoundEffect.UI_PILL_SLIDE_TICK)
  }

  fun playWindowGatoStartupChime() {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    if (activeSfxCount.get() >= 3) return
    playPcmStreamFallback(synthesizeWindowGatoStartupChime(), sfxVolume.coerceAtLeast(0.45f))
  }

  fun playWindowGatoErrorBeep() {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    if (activeSfxCount.get() >= 3) return
    playPcmStreamFallback(synthesizeWindowGatoErrorBeep(), sfxVolume.coerceAtLeast(0.45f))
  }

  fun playCorruptedAudioSample(clipId: String) {
    if (!sfxEnabled || sfxVolume <= 0.01f) return
    if (activeSfxCount.get() >= 3) return
    playPcmStreamFallback(synthesizeCorruptedAudio(clipId), sfxVolume.coerceAtLeast(0.55f))
  }

  private fun synthesizeUiPop(
    startHz: Double,
    endHz: Double,
    durationMs: Int,
    gain: Double,
  ): ShortArray {
    val count = ((SAMPLE_RATE * durationMs) / 1000).coerceAtLeast(1)
    val out = ShortArray(count)
    var phase = 0.0
    for (i in 0 until count) {
      val progress = i.toDouble() / count.toDouble()
      val freq = startHz + (endHz - startHz) * progress
      phase += 2.0 * PI * freq / SAMPLE_RATE
      val attack = (progress * 10.0).coerceAtMost(1.0)
      val decay = exp(-progress * 6.5)
      val sample = sin(phase) * attack * decay * gain
      out[i] = (sample * Short.MAX_VALUE).roundToInt().coerceIn(-32767, 32767).toShort()
    }
    return out
  }

  private fun synthesizeUiConfirm(durationMs: Int): ShortArray {
    val count = ((SAMPLE_RATE * durationMs) / 1000).coerceAtLeast(1)
    val out = ShortArray(count)
    var phase = 0.0
    val split = count / 2
    for (i in 0 until count) {
      val freq = if (i < split) 587.33 else 880.0
      phase += 2.0 * PI * freq / SAMPLE_RATE
      val localProgress =
        if (i < split) i.toDouble() / split.coerceAtLeast(1)
        else (i - split).toDouble() / (count - split).coerceAtLeast(1)
      val env = (localProgress * 12.0).coerceAtMost(1.0) * exp(-localProgress * 5.2)
      val sample = (sin(phase) * 0.8 + sin(phase * 2.0) * 0.2) * env * 0.36
      out[i] = (sample * Short.MAX_VALUE).roundToInt().coerceIn(-32767, 32767).toShort()
    }
    return out
  }

  private fun playEffectInternal(effect: CatSoundEffect) {
    val sp = soundPool
    val sampleId = soundIds[effect]
    if (sp != null && sampleId != null && loadedSoundIds[sampleId] == true) {
      val streamId = sp.play(sampleId, sfxVolume, sfxVolume, 1, 0, 1.0f)
      if (streamId != 0) return
    }

    if (activeSfxCount.get() >= 3) return
    val raw = prebuiltSfx[effect] ?: return
    playPcmStreamFallback(raw, sfxVolume)
  }

  private fun playPcmStreamFallback(rawSamples: ShortArray, volume: Float) {
    activeSfxCount.incrementAndGet()
    scope.launch {
      var track: AudioTrack? = null
      try {
        val tailPaddingSamples = (SAMPLE_RATE * 220) / 1000
        val padded =
          ShortArray(rawSamples.size + tailPaddingSamples) { i ->
            if (i < rawSamples.size) {
              (rawSamples[i] * volume)
                .roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
            } else {
              0
            }
          }
        val minBytes =
          AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        if (minBytes <= 0) return@launch

        track =
          AudioTrack.Builder()
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            )
            .setAudioFormat(
              AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBytes * 2, 4096))
            .build()

        if (track.state != AudioTrack.STATE_INITIALIZED) return@launch
        track.play()
        track.write(padded, 0, padded.size)
        val totalDurationMs = (padded.size * 1000L) / SAMPLE_RATE + 40L
        delay(totalDurationMs)
      } catch (e: Throwable) {
        Log.d(TAG, "SFX audio skipped: ${e.message}")
      } finally {
        runCatching {
          track?.pause()
          track?.flush()
          track?.stop()
          track?.release()
        }
        activeSfxCount.decrementAndGet()
      }
    }
  }

  private fun stopCustomPlayerLocked() {
    runCatching {
      customMediaPlayer?.let { mp ->
        if (mp.isPlaying) mp.stop()
        mp.release()
      }
    }
    customMediaPlayer = null
    activeCustomFilePath = null
  }

  private fun syncMusicPlaybackState(forceRestart: Boolean, speedChanged: Boolean = false) {
    val shouldPlay = musicEnabled && isForegrounded && musicVolume > 0.01f
    if (!shouldPlay) {
      musicJob?.cancel()
      musicJob = null
      synchronized(mediaLock) { stopCustomPlayerLocked() }
      return
    }

    val customTrack =
      customTracks.find { it.id == selectedTrackId }?.takeIf { File(it.filePath).exists() }
    if (customTrack != null) {
      musicJob?.cancel()
      musicJob = null
      synchronized(mediaLock) {
        val existingMp = customMediaPlayer
        if (!forceRestart && activeCustomFilePath == customTrack.filePath && existingMp != null) {
          runCatching {
            existingMp.setVolume(musicVolume, musicVolume)
            existingMp.isLooping = repeatMode == RepeatMode.ONE
            if (speedChanged && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              existingMp.playbackParams = existingMp.playbackParams.setSpeed(playbackSpeed)
            }
          }
          return
        }
        stopCustomPlayerLocked()
        runCatching {
          val mp =
            MediaPlayer().apply {
              setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_GAME)
                  .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                  .build()
              )
              setDataSource(customTrack.filePath)
              isLooping = repeatMode == RepeatMode.ONE
              prepare()
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && abs(playbackSpeed - 1.0f) > 0.02f) {
                runCatching { playbackParams = playbackParams.setSpeed(playbackSpeed) }
              }
              setOnCompletionListener {
                if (this@SoundManager.repeatMode != RepeatMode.ONE && this@SoundManager.musicEnabled) {
                  onTrackCompletedCallback?.invoke()
                }
              }
              setVolume(musicVolume, musicVolume)
              start()
            }
          customMediaPlayer = mp
          activeCustomFilePath = customTrack.filePath
        }.onFailure { e ->
          Log.w(TAG, "Failed to play custom audio track ${customTrack.title}: ${e.message}")
          stopCustomPlayerLocked()
        }
      }
      return
    }

    synchronized(mediaLock) { stopCustomPlayerLocked() }
    if (forceRestart || speedChanged) {
      musicJob?.cancel()
      musicJob = null
    }
    if (musicJob?.isActive == true) return

    val trackToPlay = MusicTrack.fromId(selectedTrackId)
    val activeSpeed = playbackSpeed
    musicJob =
      scope.launch {
        var audioTrack: AudioTrack? = null
        try {
          val minBytes =
            AudioTrack.getMinBufferSize(
              SAMPLE_RATE,
              AudioFormat.CHANNEL_OUT_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
            )
          if (minBytes <= 0) return@launch

          val stepSamples =
            ((SAMPLE_RATE * 60.0 / (trackToPlay.bpm * 2.0)) / activeSpeed).roundToInt()
              .coerceAtLeast(512)
          val chunkBytes = maxOf(minBytes, stepSamples * 2)

          audioTrack =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_GAME)
                  .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(SAMPLE_RATE)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setTransferMode(AudioTrack.MODE_STREAM)
              .setBufferSizeInBytes(chunkBytes * 2)
              .build()

          if (audioTrack.state != AudioTrack.STATE_INITIALIZED) return@launch
          audioTrack.play()

          var step = builtInCurrentStep.coerceIn(0, trackToPlay.totalPhraseSteps() - 1)
          val stepBuffer = ShortArray(stepSamples)
          var leadPhase = 0.0
          var bassPhase = 0.0

          while (isActive && musicEnabled && isForegrounded && selectedTrackId == trackToPlay.id) {
            pendingBuiltInStep?.let { jumped ->
              step = jumped.coerceIn(0, trackToPlay.totalPhraseSteps() - 1)
              pendingBuiltInStep = null
            }
            val vol = musicVolume
            if (vol <= 0.01f) {
              delay(120L)
              continue
            }
            val leadHz = trackToPlay.baseNotesHz[step % trackToPlay.baseNotesHz.size]
            val bassHz = trackToPlay.bassNotesHz[step % trackToPlay.bassNotesHz.size]
            val leadInc = 2.0 * PI * leadHz / SAMPLE_RATE
            val bassInc = 2.0 * PI * bassHz / SAMPLE_RATE

            for (i in 0 until stepSamples) {
              val t = i.toDouble() / stepSamples.toDouble()
              val attack = (t / 0.08).coerceIn(0.0, 1.0)
              val decay = exp(-3.4 * t)
              val envLead = attack * decay
              val envBass = (t / 0.06).coerceIn(0.0, 1.0) * exp(-2.2 * t)

              val leadWave =
                sin(leadPhase) * 0.72 + sin(leadPhase * 2.0) * 0.20 + sin(leadPhase * 3.0) * 0.08
              val bassWave = sin(bassPhase) * 0.85 + sin(bassPhase * 2.0) * 0.15
              val sample = (leadWave * envLead * 0.42 + bassWave * envBass * 0.32) * vol * 16000.0
              stepBuffer[i] =
                sample.roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

              leadPhase += leadInc
              bassPhase += bassInc
            }
            if (leadPhase > 2.0 * PI) leadPhase %= (2.0 * PI)
            if (bassPhase > 2.0 * PI) bassPhase %= (2.0 * PI)

            builtInCurrentStep = step
            audioTrack.write(stepBuffer, 0, stepBuffer.size)
            step++

            if (step >= trackToPlay.totalPhraseSteps()) {
              step = 0
              builtInCurrentStep = 0
              if (repeatMode != RepeatMode.ONE) {
                onTrackCompletedCallback?.invoke()
              }
            }
          }
        } catch (e: Throwable) {
          Log.d(TAG, "Music stream stopped: ${e.message}")
        } finally {
          runCatching {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
          }
        }
      }
  }

  private fun startPlaybackMonitor() {
    monitorJob?.cancel()
    monitorJob =
      scope.launch {
        while (isActive) {
          val now = System.currentTimeMillis()
          if (sleepTimerEndMillis > 0L && now >= sleepTimerEndMillis) {
            sleepTimerEndMillis = 0L
            if (musicEnabled) {
              onSleepTimerExpiredCallback?.invoke()
            }
          }
          refreshStatusSnapshot(now)
          delay(350L)
        }
      }
  }

  private fun refreshStatusSnapshot(nowMillis: Long = System.currentTimeMillis()) {
    val sleepRem =
      if (sleepTimerEndMillis > nowMillis) (sleepTimerEndMillis - nowMillis) else 0L
    val customTrack =
      customTracks.find { it.id == selectedTrackId }?.takeIf { File(it.filePath).exists() }

    val (posMs, durMs) =
      if (customTrack != null) {
        synchronized(mediaLock) {
          val mp = customMediaPlayer
          if (mp != null) {
            runCatching {
              val dur = mp.duration.toLong().coerceAtLeast(1_000L)
              val pos = mp.currentPosition.toLong().coerceIn(0L, dur)
              pos to dur
            }.getOrDefault(0L to 180_000L)
          } else {
            0L to 180_000L
          }
        }
      } else {
        val track = MusicTrack.fromId(selectedTrackId)
        val totalDur = track.cycleDurationMs(playbackSpeed)
        val totalSteps = track.totalPhraseSteps().coerceAtLeast(1)
        val currentStep = builtInCurrentStep.coerceIn(0, totalSteps - 1)
        val pos = ((currentStep.toDouble() / totalSteps) * totalDur).roundToInt().toLong()
        pos to totalDur
      }

    val playing = musicEnabled && isForegrounded && musicVolume > 0.01f
    val phaseSeed = if (playing) (nowMillis / 180L).toInt() else 0
    val bars =
      List(10) { idx ->
        if (!playing) {
          0.16f + ((idx % 3) * 0.08f)
        } else {
          val wave = abs(sin((phaseSeed + idx * 1.35) * 0.65)).toFloat()
          (0.22f + wave * 0.78f).coerceIn(0.2f, 1.0f)
        }
      }

    _playbackStatus.update {
      MusicPlaybackStatus(
        isPlaying = playing,
        currentTrackId = selectedTrackId,
        currentPositionMs = posMs,
        durationMs = durMs,
        repeatMode = repeatMode,
        shuffleEnabled = shuffleEnabled,
        playbackSpeed = playbackSpeed,
        sleepTimerRemainingMs = sleepRem,
        waveformBars = bars,
      )
    }
  }

  fun release() {
    monitorJob?.cancel()
    musicJob?.cancel()
    synchronized(mediaLock) { stopCustomPlayerLocked() }
    runCatching { soundPool?.release() }
    soundPool = null
    soundIds.clear()
    loadedSoundIds.clear()
    scope.cancel()
  }

  private fun synthesizeCatMeow(
    startHz: Double,
    peakHz: Double,
    endHz: Double,
    durationMs: Int,
  ): ShortArray {
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    var phase = 0.0
    var lpBreath = 0.0
    val rng = java.util.Random(durationMs.toLong())
    for (i in 0 until count) {
      val t = i.toDouble() / count
      val freq =
        when {
          t < 0.28 -> {
            val u = t / 0.28
            startHz + (peakHz - startHz) * sin(u * PI * 0.5)
          }
          else -> {
            val u = (t - 0.28) / 0.72
            peakHz + (endHz - peakHz) * u.pow(1.15)
          }
        } + sin(2.0 * PI * 6.5 * t) * 10.0
      phase += 2.0 * PI * freq / SAMPLE_RATE

      val mouthOpen = sin(t * PI).pow(0.85)
      val vocal =
        sin(phase) * 0.48 +
          sin(phase * 2.0) * (0.28 + 0.14 * mouthOpen) +
          sin(phase * 3.0) * (0.14 + 0.10 * mouthOpen) +
          sin(phase * 4.0) * 0.08 * mouthOpen +
          sin(phase * 5.0) * 0.04 * mouthOpen
      val rawNoise = (rng.nextDouble() * 2.0 - 1.0) * 0.04
      lpBreath = lpBreath * 0.85 + rawNoise * 0.15

      val env = sin(t * PI).pow(0.65) * exp(-0.75 * t)
      val sample = ((vocal + lpBreath) * env * 25500.0).roundToInt()
      out[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun synthesizeChirp(startHz: Double, endHz: Double, durationMs: Int): ShortArray {
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    var phase = 0.0
    for (i in 0 until count) {
      val t = i.toDouble() / count
      val freq = startHz + (endHz - startHz) * t.pow(0.75) + sin(2.0 * PI * 28.0 * t) * 22.0
      phase += 2.0 * PI * freq / SAMPLE_RATE
      val env = sin(t * PI).pow(0.55)
      val wave = sin(phase) * 0.56 + sin(phase * 2.0) * 0.30 + sin(phase * 3.0) * 0.14
      out[i] = (wave * env * 24000.0).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun synthesizeCatPurr(
    baseRumbleHz: Double,
    pulseHz: Double,
    durationMs: Int,
  ): ShortArray {
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    var phase = 0.0
    var subPhase = 0.0
    var filteredBreath = 0.0
    val rng = java.util.Random(baseRumbleHz.toLong())
    for (i in 0 until count) {
      val t = i.toDouble() / count
      val sec = i.toDouble() / SAMPLE_RATE
      val breathCycle = 0.72 + 0.28 * sin(2.0 * PI * 1.45 * sec)
      val freq = baseRumbleHz + sin(2.0 * PI * 2.2 * sec) * 3.5
      phase += 2.0 * PI * freq / SAMPLE_RATE
      subPhase += 2.0 * PI * (freq * 0.5) / SAMPLE_RATE

      val pulseRaw = 0.5 + 0.5 * sin(2.0 * PI * pulseHz * sec)
      val purrThrob = 0.25 + 0.75 * pulseRaw.pow(1.6)

      val chest =
        sin(subPhase) * 0.28 +
          sin(phase) * 0.44 +
          sin(phase * 2.0) * 0.24 +
          sin(phase * 3.0) * 0.14 +
          sin(phase * 4.0) * 0.08
      val white = (rng.nextDouble() * 2.0 - 1.0) * 0.16
      filteredBreath = filteredBreath * 0.90 + white * 0.10

      val env = sin(t * PI).pow(0.45) * breathCycle
      val sample = ((chest + filteredBreath) * purrThrob * env * 26500.0).roundToInt()
      out[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun synthesizeTrillPurr(
    purrHz: Double,
    chirpStartHz: Double,
    durationMs: Int,
  ): ShortArray {
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    var pPhase = 0.0
    var cPhase = 0.0
    var breath = 0.0
    val rng = java.util.Random(99L)
    for (i in 0 until count) {
      val t = i.toDouble() / count
      val sec = i.toDouble() / SAMPLE_RATE
      pPhase += 2.0 * PI * purrHz / SAMPLE_RATE
      val chirpFreq = chirpStartHz + 260.0 * t.pow(0.8) + sin(2.0 * PI * 26.0 * sec) * 18.0
      cPhase += 2.0 * PI * chirpFreq / SAMPLE_RATE
      val mod = 0.35 + 0.65 * (0.5 + 0.5 * sin(2.0 * PI * 25.0 * sec)).pow(1.4)
      val env = sin(t * PI).pow(0.5)
      val purr = (sin(pPhase) * 0.55 + sin(pPhase * 2.0) * 0.30 + sin(pPhase * 3.0) * 0.15) * mod
      val chirp = (sin(cPhase) * 0.72 + sin(cPhase * 2.0) * 0.28) * exp(-2.2 * t) * 0.55
      breath = breath * 0.88 + (rng.nextDouble() * 2.0 - 1.0) * 0.015
      val sample = ((purr * 0.7 + chirp + breath) * env * 25500.0).roundToInt()
      out[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun synthesizeDoubleMew(durationMs: Int): ShortArray {
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    var phase = 0.0
    for (i in 0 until count) {
      val t = i.toDouble() / count
      val env: Double
      val freq: Double
      when {
        t < 0.42 -> {
          val u = t / 0.42
          freq = 510.0 + 230.0 * sin(u * PI * 0.6)
          env = sin(u * PI).pow(0.7)
        }
        t < 0.52 -> {
          freq = 540.0
          env = 0.0
        }
        else -> {
          val u = (t - 0.52) / 0.48
          freq = 560.0 + 280.0 * sin(u * PI * 0.65)
          env = sin(u * PI).pow(0.7)
        }
      }
      phase += 2.0 * PI * freq / SAMPLE_RATE
      val wave = sin(phase) * 0.56 + sin(phase * 2.0) * 0.30 + sin(phase * 3.0) * 0.14
      out[i] = (wave * env * 24500.0).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return out
  }

  private fun synthesizeWindowGatoStartupChime(): ShortArray {
    // Classic 90s desktop boot arpeggio: G4 -> D5 -> G5 -> B5 + warm sustain pad
    val durationMs = 1150
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    val notes = listOf(392.0, 587.33, 783.99, 987.77)
    val phases = DoubleArray(notes.size)
    for (i in 0 until count) {
      val t = i.toDouble() / SAMPLE_RATE
      var mix = 0.0
      notes.forEachIndexed { idx, hz ->
        val onset = idx * 0.14
        if (t >= onset) {
          val local = t - onset
          phases[idx] += 2.0 * PI * hz / SAMPLE_RATE
          val attack = (local / 0.018).coerceAtMost(1.0)
          val decay = exp(-local * (2.6 - idx * 0.35))
          val tone = sin(phases[idx]) * 0.72 + sin(phases[idx] * 2.0) * 0.22 + sin(phases[idx] * 3.0) * 0.06
          mix += tone * attack * decay
        }
      }
      val normalized = (mix * 0.42).coerceIn(-0.95, 0.95)
      out[i] = (normalized * Short.MAX_VALUE).roundToInt().toShort()
    }
    return out
  }

  private fun synthesizeWindowGatoErrorBeep(): ShortArray {
    val durationMs = 310
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    var p1 = 0.0
    var p2 = 0.0
    for (i in 0 until count) {
      val t = i.toDouble() / count
      p1 += 2.0 * PI * 220.0 / SAMPLE_RATE
      p2 += 2.0 * PI * 277.18 / SAMPLE_RATE
      val square = (if (sin(p1) >= 0) 0.5 else -0.5) + (if (sin(p2) >= 0) 0.35 else -0.35)
      val env = (1.0 - t).pow(0.7) * (t * 25.0).coerceAtMost(1.0)
      out[i] = (square * env * 12000.0).roundToInt().coerceIn(-32767, 32767).toShort()
    }
    return out
  }

  private fun synthesizeCorruptedAudio(clipId: String): ShortArray {
    val durationMs = 2100
    val count = (SAMPLE_RATE * durationMs) / 1000
    val out = ShortArray(count)
    val rng = java.util.Random(clipId.hashCode().toLong())
    var phaseA = 0.0
    var phaseB = 0.0
    var heldSample = 0.0
    val bitCrushHold =
      when (clipId) {
        "miga_last_purr.corrupt" -> 10
        "red_eyes_port999.corrupt" -> 18
        else -> 14
      }
    for (i in 0 until count) {
      val t = i.toDouble() / count
      val sec = i.toDouble() / SAMPLE_RATE
      // Eerie detuned voice/purr warble + sudden static dropouts + square bit-crush
      val baseHz =
        when (clipId) {
          "miga_last_purr.corrupt" -> 310.0 + sin(sec * 7.5) * 85.0 - t * 140.0
          "red_eyes_port999.corrupt" -> 115.0 + sin(sec * 23.0) * 55.0 + (if ((i / 2200) % 3 == 0) 390.0 else 0.0)
          else -> 185.0 + (if ((i / 3100) % 2 == 0) 240.0 else -45.0)
        }
      phaseA += 2.0 * PI * baseHz.coerceAtLeast(55.0) / SAMPLE_RATE
      phaseB += 2.0 * PI * (baseHz * 1.498) / SAMPLE_RATE

      val inGlitchBurst = ((i / 1850) % 5 == 2) || (t > 0.72 && t < 0.81)
      if (i % bitCrushHold == 0) {
        val raw =
          if (inGlitchBurst) {
            (rng.nextDouble() * 2.0 - 1.0) * 0.75 + (if (sin(phaseB) > 0) 0.25 else -0.25)
          } else {
            sin(phaseA) * 0.55 + sin(phaseB) * 0.28 + (rng.nextDouble() * 2.0 - 1.0) * 0.14
          }
        // Quantize amplitude for creepy 4-bit corruption texture
        heldSample = (raw * 8.0).roundToInt() / 8.0
      }
      val stutterGate = if ((i / 900) % 7 == 4) 0.15 else 1.0
      val env = sin(t * PI).pow(0.25) * stutterGate
      out[i] = (heldSample * env * 19500.0).roundToInt().coerceIn(-32767, 32767).toShort()
    }
    return out
  }

  private fun writeWavFile(file: File, samples: ShortArray, sampleRate: Int) {
    val dataBytes = samples.size * 2
    val totalChunkSize = 36 + dataBytes
    val header =
      ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
        put('R'.code.toByte())
        put('I'.code.toByte())
        put('F'.code.toByte())
        put('F'.code.toByte())
        putInt(totalChunkSize)
        put('W'.code.toByte())
        put('A'.code.toByte())
        put('V'.code.toByte())
        put('E'.code.toByte())
        put('f'.code.toByte())
        put('m'.code.toByte())
        put('t'.code.toByte())
        put(' '.code.toByte())
        putInt(16)
        putShort(1.toShort())
        putShort(1.toShort())
        putInt(sampleRate)
        putInt(sampleRate * 2)
        putShort(2.toShort())
        putShort(16.toShort())
        put('d'.code.toByte())
        put('a'.code.toByte())
        put('t'.code.toByte())
        put('a'.code.toByte())
        putInt(dataBytes)
      }.array()

    val pcmBuffer = ByteBuffer.allocate(dataBytes).order(ByteOrder.LITTLE_ENDIAN)
    for (s in samples) {
      pcmBuffer.putShort(s)
    }
    FileOutputStream(file).use { out ->
      out.write(header)
      out.write(pcmBuffer.array())
    }
  }

  companion object {
    private const val TAG = "PurrSoundManager"
    private const val SAMPLE_RATE = 44100
  }
}
