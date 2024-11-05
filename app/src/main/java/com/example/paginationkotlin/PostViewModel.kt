package com.example.paginationkotlin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _posts = MutableLiveData<List<ListItem>>()
    val posts: LiveData<List<ListItem>> get() = _posts

    var currentPage = 1
    private val limit = 10
    private var isLoading = false

    init {
        loadPosts(currentPage, limit)
    }

    fun loadPosts(page: Int, limit: Int) {
        if (isLoading) {
            Log.d("PostViewModel", "Load posts already in progress...")
            return
        }

        isLoading = true
        Log.d("PostViewModel", "Fetching posts: page $page, limit $limit")

        viewModelScope.launch {
            if (page > 1) {
                _posts.value = (_posts.value ?: emptyList()) + ListItem.LoadingItem
            }

            delay(1000)

            val response: Response<List<Post>> = repository.getPosts(page, limit)
            isLoading = false

            if (response.isSuccessful) {
                response.body()?.let { newPosts ->
                    Log.d("PostViewModel", "Fetched ${newPosts.size} posts successfully.")

                    val currentPosts =
                        _posts.value?.filterNot { it is ListItem.LoadingItem } ?: emptyList()

                    val updatedPosts = currentPosts + newPosts.map { ListItem.PostItem(it) }
                    _posts.value = updatedPosts

                    currentPage++
                } ?: run {
                    Log.d("PostViewModel", "Response body is null.")
                }
            } else {
                Log.e(
                    "PostViewModel",
                    "Error fetching posts: ${response.code()} - ${response.message()}"
                )
            }

            _posts.value = _posts.value?.filterNot { it is ListItem.LoadingItem }
        }
    }

}
