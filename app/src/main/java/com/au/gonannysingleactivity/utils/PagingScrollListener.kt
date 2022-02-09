package com.au.gonannysingleactivity.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PagingScrollListener(private val layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    abstract fun isLoading(): Boolean
    abstract fun notHaveData(): Boolean
    abstract fun loadMore()

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        layoutManager.apply {
            if (!isLoading() && !notHaveData()) {
                if (findLastCompletelyVisibleItemPosition()+1>=itemCount){
                    loadMore()
                }
            }
        }
    }
}