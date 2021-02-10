package com.taboola.kotlin.examples.screens.native

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taboola.android.Taboola
import com.taboola.android.listeners.TBLNativeListener
import com.taboola.android.tblnative.*
import com.taboola.kotlin.examples.PlacementInfo
import com.taboola.kotlin.examples.R
import java.util.*


/**
 * To implement a Taboola Feed in "Native Integration" we use a RecyclerView to layout incoming items.
 */
class FeedFragment : Fragment() {
    private var mRecyclerView: RecyclerView? = null
    private var mData: MutableList<Any> = ArrayList()

    private var mNativeUnit: TBLNativeUnit? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_native_feed, container, false)
        mRecyclerView = root.findViewById(R.id.endless_feed_recycler_view)

        // Setup RecyclerView
        setupRecyclerView()

        // Define Placement properties
        val placementProperties = PlacementInfo.nativeFeedProperties()

        // Create and return a Taboola Unit
        getTaboolaUnit(placementProperties)

        // Fetch content for Unit
        fetchInitialContent()

        return root
    }

    /**
     * Basic setup for the RecyclerView with an emphasis on pulling additional items when user scrolls to bottom of View.
     */
    private fun setupRecyclerView() {
        mRecyclerView?.layoutManager = LinearLayoutManager(activity)

        // Load more Taboola content when reaching scroll bottom
        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadNextRecommendationsBatch()
                }
            }
        })

        mRecyclerView?.adapter = NativeFeedAdapter(mData)
    }

    /**
     * Taboola "Native Integration" currently differentiates between first and subsequent content fetch calls.
     * This method fetches the initial few items for this Feed implementation.
     */
    private fun fetchInitialContent() {
        // Define a fetch request (with desired number of content items in setRecCount())
        val requestData = TBLRequestData().setRecCount(10)

        mNativeUnit?.fetchRecommendations(requestData, object : TBLRecommendationRequestCallback {
            override fun onRecommendationsFetched(recommendationsResponse: TBLRecommendationsResponse?) {
                println("Taboola | fetchInitialContent | onRecommendationsFetched")

                // Send content to RecyclerView Adapter
                addRecommendationToFeed(recommendationsResponse)
            }

            override fun onRecommendationsFailed(throwable: Throwable?) {
                println("Taboola | onRecommendationsFailed: ${throwable?.message}")
            }
        })
    }


    /**
     * Taboola "Native Integration" currently differentiates between first and subsequent content fetch calls.
     * This method fetches additional items for this Feed implementation.
     */
    private fun loadNextRecommendationsBatch() {
        mNativeUnit?.getNextRecommendationsBatch(null, object : TBLRecommendationRequestCallback {
            override fun onRecommendationsFetched(recommendationsResponse: TBLRecommendationsResponse) {
                println("Taboola | loadNextRecommendationsBatch | onRecommendationsFetched")
                addRecommendationToFeed(recommendationsResponse)
            }

            override fun onRecommendationsFailed(throwable: Throwable) {
                println("Taboola | onRecommendationsFailed: ${throwable?.message}")
            }
        })
    }

    /**
     * This method parses the response with its items and adds them to the RecyclerView Adapter.
     */
    private fun addRecommendationToFeed(recommendationsResponse: TBLRecommendationsResponse?) {
        val placement = recommendationsResponse?.placementsMap?.values?.iterator()?.next()

        if (placement != null) {
            // Update data structure
            mData.addAll(placement.items)

            // Update data in RecyclerView adapter
            val itemCount = mRecyclerView?.adapter?.itemCount
            if (itemCount != null) {
                mRecyclerView?.adapter?.notifyItemRangeInserted(itemCount, placement.items.size)
            }
        }
    }

    /**
     * Define a Page that represents this screen, get a Unit from it, add it to screen and fetch its content
     * Notice: A Unit of unlimited items, called "Feed" in Taboola, can be set in TBL_PLACEMENT_TYPE.PAGE_BOTTOM only.
     */
    private fun getTaboolaUnit(properties: PlacementInfo.Properties) {
        // Define a page to control all Unit placements on this screen
        val nativePage: TBLNativePage = Taboola.getNativePage(properties.sourceType, properties.pageUrl)

        // Define a single Unit to display
        mNativeUnit = nativePage.build(properties.placementName, object : TBLNativeListener() {
            override fun onItemClick(placementName: String?, itemId: String?, clickUrl: String?, isOrganic: Boolean, customData: String?): Boolean {
                println("Taboola | onItemClick | isOrganic = $isOrganic")
                return super.onItemClick(placementName, itemId, clickUrl, isOrganic, customData)
            }
        })
    }
}