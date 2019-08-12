package jp.yama.addressimportapp

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.internal.http.promisesBody
import java.io.IOException
import java.lang.Exception

class HttpClient {

    companion object {

        private fun _get(url: String): ResponseBody? {
            val client = OkHttpClient()
            val req = Request.Builder()
                .url(url).build()
            val resp = client.newCall(req).execute()
            Log.i("yama", "status " + resp.code)
            return resp.body
        }

        fun get(url: String): Deferred<ResponseBody> {
            return GlobalScope.async {
                _get(url).let {
                    when (it == null) {
                        true -> throw Exception("error for $url")
                        else -> it
                    }
                }
            }
        }

    }



}