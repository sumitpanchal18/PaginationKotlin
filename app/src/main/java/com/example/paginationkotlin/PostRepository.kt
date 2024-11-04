package com.example.paginationkotlin

import retrofit2.Response


class PostRepository(private val apiService: ApiService) {

    suspend fun getPosts(page: Int, limit: Int): Response<List<Post>> {
        return apiService.getPosts(page, limit)
    }
}
