package com.openmusic.app.domain.usecase

import com.openmusic.app.domain.repository.MusicRepository

class GetAudioStreamUseCase(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(videoId: String): Result<String> {
        return repository.getStreamUrl(videoId)
    }
}
