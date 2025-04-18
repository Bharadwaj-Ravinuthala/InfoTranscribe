package com.infomericainc.infotranscribe.api

import android.content.Context
import androidx.annotation.Keep
import com.infomericainc.infotranscribe.internal.ErrorListener
import com.infomericainc.infotranscribe.internal.LiveTranscribeImpl
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer

@Keep
interface LiveTranscribe {

    /**
     * Used to initialize the [SpeechConfig] and [SpeechRecognizer]
     * from [com.microsoft.cognitiveservices.speech]
     *
     * @param context context of the current scope.
     * @param apiKey ApiKey from the Azure Console.
     * @param region Your Azure serves region.
     * @param onError Callback that handles the initialization error.
     *
     * Make sure to call this [initialize] first before calling any of the
     * functions from this class.
     *
     */

    fun initialize(
        context: Context,
        apiKey: String,
        region: String,
        onError: (String) -> Unit
    ) : LiveTranscribe

    fun startTranscribe(
        onStarted: () -> Unit = { }
    )

    fun observe(
        onSuccess: (String) -> Unit,
    )

    fun pauseTranscribe(
        onPaused: () -> Unit = { }
    )

    fun addOnErrorListener(listener: ErrorListener)

    fun endTranscribe()

    companion object {
        @JvmStatic
        fun getTranscribe(): LiveTranscribe = LiveTranscribeImpl()
        fun test() {
            getTranscribe()
        }
    }

}