package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as ExtractorRequest
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OkHttp bridge required by NewPipe Extractor.
 * NewPipe Extractor performs the actual YouTube extraction; this class only
 * supplies its HTTP transport.
 */
class NewPipeDownloader(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(35, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
) : Downloader() {

    override fun execute(request: ExtractorRequest): Response {
        val method = request.httpMethod()
        val body = request.dataToSend()
        val builder = Request.Builder().url(request.url())

        request.headers().forEach { (name, values) ->
            values.forEach { builder.addHeader(name, it) }
        }

        if (!request.headers().keys.any { it.equals("User-Agent", true) }) {
            builder.header("User-Agent", USER_AGENT)
        }

        val requestBody = when {
            body != null -> body.toRequestBody(null)
            method.equals("POST", true) || method.equals("PUT", true) -> ByteArray(0).toRequestBody(null)
            else -> null
        }

        builder.method(method, requestBody)

        try {
            client.newCall(builder.build()).execute().use { response ->
                if (response.code == 429) {
                    throw ReCaptchaException("HTTP 429 rate limit", request.url())
                }

                val text = response.body?.string() ?: ""
                val headers = mutableMapOf<String, MutableList<String>>()
                response.headers.forEach { (name, value) ->
                    headers.getOrPut(name) { mutableListOf() }.add(value)
                }

                return Response(
                    response.code,
                    response.message,
                    headers,
                    text,
                    request.url()
                )
            }
        } catch (e: IOException) {
            throw e
        }
    }

    companion object {
        // Browser UA is intentionally used because current YouTube extraction
        // paths can select client-specific responses based on the UA.
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 16; Mobile) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36"
    }
}
