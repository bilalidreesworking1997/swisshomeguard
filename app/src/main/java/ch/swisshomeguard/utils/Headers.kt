package ch.swisshomeguard.utils

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

// TODO improve authentication method
// https://medium.com/@PaulinaSadowska/adding-headers-to-image-request-in-glide-dc9640ca9b12
object Headers {
    fun getUrlWithHeaders(url: String?): GlideUrl {
        return GlideUrl(
            url, LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer ${HomeguardTokenUtils.readHomeguardToken()}")
                .build()
        )
    }
}