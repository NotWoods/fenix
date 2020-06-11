/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.intent

import android.content.Intent
import androidx.navigation.NavController
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.helpers.FenixRobolectricTestRunner

@RunWith(FenixRobolectricTestRunner::class)
class StartSearchIntentProcessorTest {

    private val metrics: MetricController = mockk(relaxed = true)
    private val navController: NavController = mockk(relaxed = true)
    private val out: Intent = mockk(relaxed = true)

    @Test
    fun `do not process blank intents`() {
        StartSearchIntentProcessor(metrics).process(Intent(), navController, out)

        verify { metrics wasNot Called }
        verify { navController wasNot Called }
        verify { out wasNot Called }
    }

    @Test
    fun `do not process when search extra is false`() {
        val intent = Intent().apply {
            removeExtra(HomeActivity.OPEN_TO_SEARCH)
        }
        StartSearchIntentProcessor(metrics).process(intent, navController, out)

        verify { metrics wasNot Called }
        verify { navController wasNot Called }
        verify { out wasNot Called }
    }

    @Test
    fun `process search intents from widget`() {
        val intent = Intent().apply {
            putExtra(HomeActivity.OPEN_TO_SEARCH, StartSearchIntentProcessor.SEARCH_WIDGET)
        }
        StartSearchIntentProcessor(metrics).process(intent, navController, out)

        verify { metrics.track(Event.SearchWidgetNewTabPressed) }
        verify {
            navController.navigate(
                NavGraphDirections.actionGlobalSearch(
                    sessionId = null,
                    searchAccessPoint = Event.PerformedSearch.SearchAccessPoint.WIDGET
                ),
                null
            )
        }
        verify { out.removeExtra(HomeActivity.OPEN_TO_SEARCH) }
    }

    fun `process search intents from new tab shortcut`() {
        val intent = Intent().apply {
            putExtra(HomeActivity.OPEN_TO_SEARCH, StartSearchIntentProcessor.STATIC_SHORTCUT_NEW_TAB)
        }
        StartSearchIntentProcessor(metrics).process(intent, navController, out)

        verify { metrics.track(Event.PrivateBrowsingStaticShortcutTab) }
        verify {
            navController.navigate(
                NavGraphDirections.actionGlobalSearch(
                    sessionId = null,
                    searchAccessPoint = Event.PerformedSearch.SearchAccessPoint.SHORTCUT
                ),
                null
            )
        }
        verify { out.removeExtra(HomeActivity.OPEN_TO_SEARCH) }
    }

    fun `process search intents from new private tab shortcut`() {
        val intent = Intent().apply {
            putExtra(HomeActivity.OPEN_TO_SEARCH, StartSearchIntentProcessor.STATIC_SHORTCUT_NEW_PRIVATE_TAB)
        }
        StartSearchIntentProcessor(metrics).process(intent, navController, out)

        verify { metrics.track(Event.PrivateBrowsingStaticShortcutPrivateTab) }
        verify {
            navController.navigate(
                NavGraphDirections.actionGlobalSearch(
                    sessionId = null,
                    searchAccessPoint = Event.PerformedSearch.SearchAccessPoint.SHORTCUT
                ),
                null
            )
        }
        verify { out.removeExtra(HomeActivity.OPEN_TO_SEARCH) }
    }

    fun `process search intents from private browsing shortcut`() {
        val intent = Intent().apply {
            putExtra(HomeActivity.OPEN_TO_SEARCH, StartSearchIntentProcessor.PRIVATE_BROWSING_PINNED_SHORTCUT)
        }
        StartSearchIntentProcessor(metrics).process(intent, navController, out)

        verify { metrics.track(Event.PrivateBrowsingPinnedShortcutPrivateTab) }
        verify {
            navController.navigate(
                NavGraphDirections.actionGlobalSearch(
                    sessionId = null,
                    searchAccessPoint = Event.PerformedSearch.SearchAccessPoint.SHORTCUT
                ),
                null
            )
        }
        verify { out.removeExtra(HomeActivity.OPEN_TO_SEARCH) }
    }
}
