/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings

import android.content.ComponentName
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import org.mozilla.fenix.R
import org.mozilla.fenix.components.PrivateShortcutCreateManager
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.checkAndUpdateScreenshotPermission
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.metrics
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.ext.showToolbar
import org.mozilla.fenix.widget.PrivateTileService

/**
 * Lets the user customize Private browsing options.
 */
class PrivateBrowsingFragment : PreferenceFragmentCompat() {
    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.preferences_private_browsing_options))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.private_browsing_preferences, rootKey)
        updatePreferences()
    }

    private fun updatePreferences() {
        findPreference<Preference>(getPreferenceKey(R.string.pref_key_add_private_browsing_shortcut))?.apply {
            setOnPreferenceClickListener {
                requireContext().metrics.track(Event.PrivateBrowsingCreateShortcut)
                PrivateShortcutCreateManager.createPrivateShortcut(requireContext())
                true
            }
        }

        findPreference<SwitchPreference>(getPreferenceKey(R.string.pref_key_open_links_in_a_private_tab))?.apply {
            onPreferenceChangeListener = SharedPreferenceUpdater()
            isChecked = context.settings().openLinksInAPrivateTab
            if (SDK_INT >= Build.VERSION_CODES.N) {
                TileService.requestListeningState(
                    context,
                    ComponentName(context, PrivateTileService::class.java)
                )
            }
        }

        findPreference<SwitchPreference>(getPreferenceKey
            (R.string.pref_key_allow_screenshots_in_private_mode))?.apply {
            onPreferenceChangeListener = object : SharedPreferenceUpdater() {
                override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
                    return super.onPreferenceChange(preference, newValue).also {
                        requireActivity().checkAndUpdateScreenshotPermission(requireActivity().settings()) }
                }
            }
        }
    }
}
