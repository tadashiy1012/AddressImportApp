package jp.yama.addressimportapp

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.internal.http.promisesBody
import java.io.IOException
import java.lang.Exception

class HttpClient {

    companion object {


        private fun _get(url: String): Response? {
            val client = OkHttpClient()
            val req = Request.Builder()
                .url(url).build()
            val resp = client.newCall(req).execute()
            Log.i("HttpClient","status: ${resp.code} ${url}")
            return resp
        }

        suspend fun get(url: String): Deferred<Response> = coroutineScope {
            async(Dispatchers.Default) {
                _get(url).let {
                    when (it == null) {
                        true -> throw  Exception("error for $url")
                        else -> it
                    }
                }
            }
        }

        suspend fun get(url: String, key: AppKeys?): Deferred<Pair<AppKeys?, Response>> = coroutineScope {
            async(Dispatchers.Default) {
                _get(url).let {
                    when (it == null) {
                        true -> throw Exception("error for $url")
                        else -> Pair(key ?: null, it)
                    }
                }
            }
        }

        suspend fun get(url: String, key: SectionKeys?): Deferred<Pair<SectionKeys?, Response>> = coroutineScope {
            async(Dispatchers.Default) {
                _get(url).let {
                    when (it == null) {
                        true -> throw Exception("error for $url")
                        else -> Pair(key ?: null, it)
                    }
                }
            }
        }

    }



}