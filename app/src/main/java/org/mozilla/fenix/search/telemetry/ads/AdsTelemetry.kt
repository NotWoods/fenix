/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.search.telemetry.ads

import androidx.annotation.VisibleForTesting
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import org.json.JSONObject
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.search.telemetry.BaseSearchTelemetry
import org.mozilla.fenix.search.telemetry.ExtensionInfo
import org.mozilla.fenix.search.telemetry.SearchProviderModel

class AdsTelemetry(private val metrics: MetricController) : BaseSearchTelemetry() {

    override fun install(engine: Engine, store: BrowserStore) {
        val info = ExtensionInfo(
            id = ADS_EXTENSION_ID,
            resourceUrl = ADS_EXTENSION_RESOURCE_URL,
            messageId = ADS_MESSAGE_ID
        )
        installWebExtension(engine, store, info)
    }

    fun trackAdClickedMetric(sessionUrl: String?, urlPath: List<String>) {
        val provider = getProviderForUrl(sessionUrl ?: return)
        provider?.apply {
            if (containsAds(urlPath)) {
                metrics.track(Event.SearchAdClicked(name))
            }
        }
    }

    override fun processMessage(message: JSONObject) {
        val urls = getMessageList<String>(message, ADS_MESSAGE_DOCUMENT_URLS_KEY)
        val provider = getProviderForUrl(message.getString(ADS_MESSAGE_SESSION_URL_KEY))
        provider?.apply {
            if (containsAds(urls)) {
                metrics.track(Event.SearchWithAds(name))
            }
        }
    }

    companion object {
        @VisibleForTesting
        internal const val ADS_EXTENSION_ID = "mozacBrowserAds"
        @VisibleForTesting
        internal const val ADS_EXTENSION_RESOURCE_URL = "resource://android/assets/extensions/ads/"
        @VisibleForTesting
        internal const val ADS_MESSAGE_SESSION_URL_KEY = "url"
        @VisibleForTesting
        internal const val ADS_MESSAGE_DOCUMENT_URLS_KEY = "urls"
        @VisibleForTesting
        internal const val ADS_MESSAGE_ID = "MozacBrowserAds"
    }
}

@VisibleForTesting
internal fun SearchProviderModel.containsAds(urlList: List<String>) =
    urlList.containsAds(extraAdServersRegexps)

private fun List<String>.containsAds(adRegexps: List<String>) =
    this.any { url -> url.isAd(adRegexps) }

private fun String.isAd(adRegexps: List<String>) =
    adRegexps.any { adsRegex -> adsRegex.toRegex().containsMatchIn(this) }
