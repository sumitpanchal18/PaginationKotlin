package com.example.paginationkotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var postViewModel: PostViewModel
    private lateinit var adapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val repository = PostRepository(apiService)

        postViewModel =
            ViewModelProvider(this, PostViewModelFactory(repository))[PostViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.MainProgressBar)

        adapter = PostAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        progressBar.visibility = View.VISIBLE

        postViewModel.posts.observe(this) { posts ->

            CoroutineScope(Dispatchers.Main).launch {
                if (posts.isEmpty()) {
                    Log.d("MainActivity", "No posts available.")
                } else {
                    delay(5000)
                    progressBar.visibility = View.GONE
                    Log.d("MainActivity", "Posts updated: ${posts.size} items")
                    adapter.submitList(posts)
                }
            }
        }

        postViewModel.loadPosts(postViewModel.currentPage, 10)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                    postViewModel.loadPosts(postViewModel.currentPage, 10)
                }
            }
        })
    }
}
