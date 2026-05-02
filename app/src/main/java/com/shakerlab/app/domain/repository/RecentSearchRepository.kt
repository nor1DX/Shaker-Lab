package com.shakerlab.app.domain.repository

interface RecentSearchRepository {
    fun getAll(): List<String>
    fun save(query: String)
    fun clear()
}
