package com.denser.june.core.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request as OkHttpRequest
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException

class NewPipeDownloader(private val client: OkHttpClient) : Downloader() {
    
    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val url = request.url()
        val method = request.httpMethod()
        val headers = request.headers()
        val body = request.dataToSend()

        val builder = OkHttpRequest.Builder().url(url)

        // Headers
        headers?.forEach { (key, values) ->
            values.forEach { value ->
                builder.addHeader(key, value)
            }
        }

        // Method
        if (method.equals("POST", ignoreCase = true)) {
            val requestBody = (body ?: ByteArray(0)).toRequestBody()
            builder.post(requestBody)
        } else {
            builder.get()
        }

        client.newCall(builder.build()).execute().use { response ->
            val responseCode = response.code
            val responseMessage = response.message
            val responseBody = response.body?.string()
            val latestUrl = response.request.url.toString()
            
            // Map headers to Multimap
            val responseHeaders = response.headers.toMultimap()

            return Response(responseCode, responseMessage, responseHeaders, responseBody, latestUrl)
        }
    }
}
