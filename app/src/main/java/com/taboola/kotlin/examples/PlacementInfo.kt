package com.taboola.kotlin.examples

class PlacementInfo {
    // Definitions common for placement properties
    abstract class Properties {
        open val placementName = "Mid Article"
        val sourceType = "text"
        val pageType = "article"
        val pageUrl = "https://blog.taboola.com"
        val targetType = "mix"
        val mode = "thumbs-feed-01"
    }

    class WidgetProperties : Properties() {
        override val placementName = "Widget without video"
    }

    class ClassicFeedProperties : Properties() {
        override val placementName = "Feed with video"
    }

    class NativeFeedProperties : Properties() {
        override val placementName = "list_item"
    }

    class WebFeedProperties : Properties() {
        override val placementName = "Feed without video"
    }

    // Static access
    companion object  {
        fun widgetProperties() = WidgetProperties()
        fun classicFeedProperties() = ClassicFeedProperties()
        fun nativeFeedProperties() = NativeFeedProperties()
        fun webFeedProperties() = WebFeedProperties()
    }


}