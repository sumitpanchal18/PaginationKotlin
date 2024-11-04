package com.example.paginationkotlin

sealed class ListItem {
    data class PostItem(val post: Post) : ListItem()
    object LoadingItem : ListItem()
}
