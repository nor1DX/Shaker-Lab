package com.shakerlab.app.data.repository

import android.content.Context
import com.shakerlab.app.domain.repository.RecentSearchRepository

private const val PREFS_NAME = "search_prefs"
private const val KEY_RECENT = "recent_searches"
private const val MAX_RECENT = 8

class RecentSearchRepositoryImpl(context: Context) : RecentSearchRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getAll(): List<String> {
        val raw = prefs.getString(KEY_RECENT, "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split("|||")
    }

    override fun save(query: String) {
        val current = getAll().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > MAX_RECENT) current.removeAt(current.lastIndex)
        prefs.edit().putString(KEY_RECENT, current.joinToString("|||")).apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_RECENT).apply()
    }
}
