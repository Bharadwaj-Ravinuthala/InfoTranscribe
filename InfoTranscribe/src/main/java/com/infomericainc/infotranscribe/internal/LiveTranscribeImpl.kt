package com.infomericainc.infotranscribe.internal

import android.content.Context
import android.util.Log
import com.infomericainc.infotranscribe.api.LiveTranscribe
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig

internal typealias ErrorListener = (String) -> Unit

internal class LiveTranscribeImpl : LiveTranscribe {

    private var microphoneService: MicrophoneService? = null
    private var speechConfig: SpeechConfig? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val errorListeners = mutableListOf<ErrorListener>()

    override fun initialize(
        context: Context,
        apiKey: String,
        region: String,
        onError: (String) -> Unit
    ): LiveTranscribe {
        try {
            microphoneService = MicrophoneService(
                context = context
            ).create()
        } catch (e: Exception) {
            onError(e.message.toString())
            return this
        }

        speechConfig = SpeechConfig.fromSubscription(
            apiKey, region
        )

        speechRecognizer = SpeechRecognizer(
            speechConfig,
            AudioConfig.fromStreamInput(microphoneService)
        )
        Log.i(LIVE_TRANSCRIBE, "Initialized")
        return this
    }

    override fun startTranscribe(onStarted: () -> Unit) {
        if (speechRecognizer == null) {
            notifyError("Please call initialize, Before starting the transcribe.")
            return
        }
        speechRecognizer?.startContinuousRecognitionAsync().also { it?.get() }
        onStarted()
        Log.i(LIVE_TRANSCRIBE, "Starting the live transcription.")
    }

    override fun observe(onSuccess: (String) -> Unit) {
        if (speechRecognizer == null) {
            notifyError("Please call initialize, Before observing the transcribe.")
            return
        }
        speechRecognizer?.recognized?.addEventListener { _, e ->
            onSuccess(e.result.text.plus("\n"))
        }
    }

    override fun addOnErrorListener(listener: ErrorListener) {
        errorListeners.add(listener)
    }

    private fun notifyError(message: String) {
        errorListeners.forEach { it(message) }
    }


    override fun pauseTranscribe(onPaused: () -> Unit) {
        if (speechRecognizer == null) {
            notifyError("Please call initialize, Before pausing the transcribe.")
        }
        speechRecognizer?.stopContinuousRecognitionAsync().also { it?.get() }
        onPaused()
        Log.i(LIVE_TRANSCRIBE, "Pausing the live transcription.")
    }

    override fun endTranscribe() {
        microphoneService?.close()
        speechRecognizer?.recognized?.removeEventListener { sender, e -> }
        speechRecognizer?.close()
        speechConfig?.close()
        speechConfig = null
        Log.i(LIVE_TRANSCRIBE, "Live transcription Ended.")
    }

    private companion object {
        private const val LIVE_TRANSCRIBE = "LiveTranscribe"
    }
}