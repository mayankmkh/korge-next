package com.soywiz.korau.sound

import com.soywiz.klock.*
import com.soywiz.korau.format.*
import com.soywiz.korau.internal.*
import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.*
import com.soywiz.korio.lang.*
import kotlinx.coroutines.*
import org.w3c.dom.*
import kotlin.coroutines.*

class HtmlNativeSoundProvider : NativeSoundProvider() {
    override fun init() {
        HtmlSimpleSound.ensureUnlockStart()
    }

	override fun createAudioStream(coroutineContext: CoroutineContext, freq: Int): PlatformAudioOutput = JsPlatformAudioOutput(coroutineContext, freq)

	override suspend fun createSound(data: ByteArray, streaming: Boolean, props: AudioDecodingProps, name: String): Sound =
        AudioBufferSound(HtmlSimpleSound.loadSound(data), coroutineContext, name)

	override suspend fun createSound(vfs: Vfs, path: String, streaming: Boolean, props: AudioDecodingProps): Sound = when (vfs) {
		is LocalVfs, is UrlVfs -> {
            //println("createSound[1]")
			val rpath = when (vfs) {
				is LocalVfs -> path
				is UrlVfs -> vfs.getFullUrl(path)
				else -> invalidOp
			}
            if (streaming) {
                MyHTMLAudio(rpath)
            } else {
                AudioBufferSound(HtmlSimpleSound.loadSound(rpath), coroutineContext)
            }
		}
		else -> {
            //println("createSound[2]")
			super.createSound(vfs, path)
		}
	}
}

class MyHTMLAudio(
    val audio: Audio,
    coroutineContext: CoroutineContext,
) : Sound(coroutineContext) {
    override val length: TimeSpan get() = audio.duration.seconds

    override suspend fun decode(): AudioData =
        AudioBufferSound(HtmlSimpleSound.loadSound(audio.src), coroutineContext).decode()

    companion object {
        suspend operator fun invoke(url: String): MyHTMLAudio {
            val audio = Audio(url)
            val promise = CompletableDeferred<Unit>()
            audio.oncanplay = { promise.complete(Unit) }
            audio.oncanplaythrough = { promise.complete(Unit) }
            promise.await()
            //HtmlSimpleSound.waitUnlocked()
            return MyHTMLAudio(audio, coroutineContext)
        }
    }

    override fun play(params: PlaybackParameters): SoundChannel {
        val audioCopy = Audio(audio.src)
        audioCopy.volume = params.volume
        HtmlSimpleSound.callOnUnlocked {
            audioCopy.play()
        }
        return object : SoundChannel(this@MyHTMLAudio) {
            override var volume: Double
                get() = audioCopy.volume
                set(value) {
                    audioCopy.volume = value
                }

            override var pitch: Double
                get() = 1.0
                set(value) {}

            override var panning: Double
                get() = 0.0
                set(value) {}

            override val total: TimeSpan get() = audioCopy.duration.seconds
            override var current: TimeSpan
                get() = audioCopy.currentTime.seconds
                set(value) { audioCopy.currentTime = value.seconds }

            override val state: SoundChannelState get() = when {
                audioCopy.paused -> SoundChannelState.PAUSED
                audioCopy.ended -> SoundChannelState.STOPPED
                else -> SoundChannelState.PLAYING
            }

            override fun pause() {
                audioCopy.pause()
            }

            override fun resume() {
                audioCopy.play()
            }

            override fun stop() {
                audioCopy.pause()
                current = 0.seconds
            }
        }
    }
}

class AudioBufferSound(
    val buffer: AudioBuffer?,
    coroutineContext: CoroutineContext,
    override val name: String = "unknown"
) : Sound(coroutineContext) {
	override val length: TimeSpan = ((buffer?.duration) ?: 0.0).seconds

    override val nchannels: Int get() = buffer?.numberOfChannels ?: 1

    override suspend fun decode(): AudioData = if (buffer == null) {
		AudioData.DUMMY
	} else {
		val nchannels = buffer.numberOfChannels
		val nsamples = buffer.length
		val data = AudioSamples(nchannels, nsamples)
		var m = 0
		for (c in 0 until nchannels) {
			val channelF = buffer.getChannelData(c)
			for (n in 0 until nsamples) {
				data[c][m++] = SampleConvert.floatToShort(channelF[n])
			}
		}
		AudioData(buffer.sampleRate, data)
	}

	override fun play(params: PlaybackParameters): SoundChannel {
        //println("AudioBufferNativeSound.play: $params")
		return object : SoundChannel(this) {
			val channel = if (buffer != null) HtmlSimpleSound.playSound(buffer, params,coroutineContext) else null

			override var volume: Double
				get() = channel?.volume ?: 1.0
				set(value) { channel?.volume = value}
			override var pitch: Double
				get() = channel?.pitch ?: 1.0
				set(value) { channel?.pitch = value }
			override var panning: Double
				get() = channel?.panning ?: 0.0
				set(value) { channel?.panning = value }
			override var current: TimeSpan
                get() = channel?.currentTime ?: 0.seconds
                set(value) = run { channel?.currentTime = value }
			override val total: TimeSpan = buffer?.duration?.seconds ?: 0.seconds
            override val state: SoundChannelState get() = when {
                channel?.pausedAt != null -> SoundChannelState.PAUSED
                channel?.playing ?: (current < total) -> SoundChannelState.PLAYING
                else -> SoundChannelState.STOPPED
            }

            override fun pause() {
                channel?.pause()
            }

            override fun resume() {
                channel?.resume()
            }

            override fun stop(): Unit = run { channel?.stop() }
		}.also {
            //it.current = params.startTime
            it.copySoundPropsFrom(params)
        }
	}
}
