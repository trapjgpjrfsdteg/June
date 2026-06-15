package com.denser.june.core.domain.repository

import com.denser.june.core.domain.model.SongDetails

interface SongRepository {
    suspend fun fetchSongDetails(url: String): Result<SongDetails>
    suspend fun searchSongs(query: String): Result<List<SongDetails>>
}
