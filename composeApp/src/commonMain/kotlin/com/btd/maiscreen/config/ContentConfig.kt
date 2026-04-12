package com.btd.maiscreen.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum for special category display types
 */
@Serializable
enum class CategoryType {
    @SerialName("standard")
    STANDARD,       // Normal category display

    @SerialName("top10")
    TOP_10,         // Shows numbered badges 1-10

    @SerialName("featured")
    FEATURED,       // Larger cards, special highlight

    @SerialName("themed")
    THEMED          // Special visual indicator (e.g., Black History Month)
}

/**
 * Configuration for a single category
 */
@Serializable
data class CategoryConfig(
    val id: String,
    val name: String,
    val type: CategoryType = CategoryType.STANDARD,
    val order: Int,
    val enabled: Boolean = true,
    val isTestOnly: Boolean = false,  // Hidden unless useTestData is true
    val contentIds: List<String> = emptyList(),  // Specific content to include
    val themeColor: String? = null,   // Optional hex color for themed categories
    val themeIcon: String? = null,    // Optional emoji/icon for themed categories
    val maxItems: Int? = null         // Limit items shown (null = show all)
)

/**
 * Configuration for hero/featured card
 */
@Serializable
data class HeroConfig(
    val contentId: String,            // ID of content to show in hero
    val fallbackContentId: String? = null  // Backup if primary not available
)

/**
 * Root configuration object
 */
@Serializable
data class ContentConfiguration(
    val version: String = "1.0",
    val hero: HeroConfig,
    val categories: List<CategoryConfig>,
    val testCategories: List<CategoryConfig> = emptyList()  // Categories only shown in test mode
)
