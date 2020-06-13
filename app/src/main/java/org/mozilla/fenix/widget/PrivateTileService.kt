/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.widget

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import org.mozilla.fenix.ext.settings

/**
 * Service to update a Quick Settings tile for opening third-party links in private mode.
 * The tile will be active if the private browsing preference is enabled, and disabled otherwise.
 *
 * Whenever the preference is toggled in app (instead of by this service),
 * the tile should be updated by calling [TileService.requestListeningState].
 */
@RequiresApi(Build.VERSION_CODES.N)
class PrivateTileService : TileService() {

    /**
     * Called when the Quick Settings tile is first created.
     * Updates the tile state.
     */
    override fun onTileAdded() = updateTile()

    /**
     * Called whenever [TileService.requestListeningState] is used.
     * Updates the tile state once.
     */
    override fun onStartListening() = updateTile()

    /**
     * Toggles the "open links in a private tab" preference.
     */
    override fun onClick() {
        val settings = settings()
        val newState = !settings.openLinksInAPrivateTab
        settings.openLinksInAPrivateTab = newState
        updateTile(newState)
    }

    private fun updateTile(openLinksInAPrivateTab: Boolean = settings().openLinksInAPrivateTab) {
        qsTile.apply {
            state = if (openLinksInAPrivateTab) {
                Tile.STATE_ACTIVE
            } else {
                Tile.STATE_INACTIVE
            }
            updateTile()
        }
    }
}
