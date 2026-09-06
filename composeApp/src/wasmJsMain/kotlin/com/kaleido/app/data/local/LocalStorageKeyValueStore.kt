package com.kaleido.app.data.local

import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

class LocalStorageKeyValueStore : KeyValueStore {
    override fun getString(key: String): String? = localStorage[key]
    override fun putString(key: String, value: String) { localStorage[key] = value }
    override fun remove(key: String) { localStorage.removeItem(key) }
}
