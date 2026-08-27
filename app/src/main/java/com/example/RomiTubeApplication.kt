package com.example

import android.app.Application
import com.example.data.remote.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class RomiTubeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NewPipe.init(
            NewPipeDownloader(),
            Localization("IN", "en"),
            ContentCountry("IN")
        )
    }
}
