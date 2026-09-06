package com.kaleido.app.data.local

/**
 * Tiny cross-platform key/value persistence — the whole offline layer sits on
 * this. Concrete implementations per platform:
 *
 *  - Android  → SharedPreferences   (`SharedPrefsKeyValueStore`)
 *  - iOS      → NSUserDefaults       (`UserDefaultsKeyValueStore`)
 *  - Web      → window.localStorage  (`LocalStorageKeyValueStore`)
 *
 * Deliberately string-only: callers serialize their own JSON, which keeps the
 * platform code to a handful of lines each and makes it trivial to fake in tests
 * ([InMemoryKeyValueStore]).
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

/** In-memory implementation for tests and previews. */
class InMemoryKeyValueStore(
    initial: Map<String, String> = emptyMap(),
) : KeyValueStore {
    private val map = initial.toMutableMap()
    override fun getString(key: String): String? = map[key]
    override fun putString(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
}
