package com.tnmlicitacoes.app.search

import android.os.Bundle

import com.tnmlicitacoes.app.R
import android.app.SearchManager
import android.content.Context
import android.support.v7.widget.SearchView
import com.tnmlicitacoes.app.ui.base.BaseAuthenticatedActivity
import android.text.TextUtils
import android.view.View
import android.support.v4.graphics.drawable.DrawableCompat
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.transition.Slide
import android.view.Gravity
import android.widget.ListView
import com.tnmlicitacoes.app.model.realm.Notice
import com.tnmlicitacoes.app.ui.adapter.NoticeSearchResultAdapter
import com.tnmlicitacoes.app.utils.NoticeUtils
import io.realm.Case
import io.realm.Realm


class SearchActivity : BaseAuthenticatedActivity() {
    override fun getLogTag(): String {
        return TAG
    }

    private var mSearchView : SearchView? = null

    private var mSearchResults : ListView? = null
    private var mSearchResultsAdapter : NoticeSearchResultAdapter? = null

    private var mQuery : String? = null

    // Loading from local database for now
    private var mRealm : Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRealm = Realm.getDefaultInstance()
        setContentView(R.layout.activity_search)

        mSearchView = findViewById(R.id.search_view) as SearchView
        initSearchView()

        mSearchResults = findViewById(R.id.search_results) as ListView
        mSearchResults?.setOnItemClickListener { _, _, position, _ ->
            NoticeUtils.seeDetails(this, mSearchResultsAdapter?.getItem(position))
        }

        mSearchResultsAdapter = NoticeSearchResultAdapter(this)
        mSearchResults?.adapter = mSearchResultsAdapter

        val up = DrawableCompat.wrap(resources.getDrawable(R.drawable.ic_arrow_back))
        DrawableCompat.setTint(up, resources.getColor(R.color.colorGrey))
        toolbar.navigationIcon = up
        toolbar.setNavigationOnClickListener { ActivityCompat.finishAfterTransition(this) }

        // Get the intent, verify the action and get the query
        var query: String? = intent.getStringExtra(SearchManager.QUERY)
        query = if (query == null) "" else query
        mQuery = query

        mSearchView?.setQuery(query, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val slide = Slide(Gravity.RIGHT);
            slide.excludeTarget(R.id.search_results, true);
            window.exitTransition = slide
        };
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm?.close()
    }

    fun initSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        mSearchView?.isIconified = false
        // Set the query hint.
        mSearchView?.queryHint = getString(R.string.buscarHint)
        mSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                mSearchView?.clearFocus()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                searchFor(s)
                return true
            }
        })

        mSearchView?.setOnCloseListener {
            ActivityCompat.finishAfterTransition(this)
            true
        }

        if (!TextUtils.isEmpty(mQuery)) {
            mSearchView?.setQuery(mQuery, false)
        }
    }

    fun searchFor(query: String) {
        if (query.length < 2) {
            return
        }

        mSearchResultsAdapter?.setQuery(query)

        val results = mRealm?.where(Notice::class.java)?.contains("object", query, Case.INSENSITIVE)?.findAll()
        mSearchResultsAdapter?.setItems(ArrayList<Notice>(results))

        mSearchResults?.visibility = if (results?.size != 0) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        ActivityCompat.finishAfterTransition(this)
    }

    companion object {

        val TAG = "SearchActivity"
    }
}
