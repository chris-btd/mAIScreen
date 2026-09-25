package com.btd.maiscreen.repository

import androidx.compose.ui.graphics.Color
import com.btd.maiscreen.api.AppSyncClient
import com.btd.maiscreen.api.AppSyncResult
import com.btd.maiscreen.api.CatalogResponse
import com.btd.maiscreen.api.GET_CATALOG_QUERY
import com.btd.maiscreen.api.HERO_COLLECTION_SLUG
import com.btd.maiscreen.api.ProgramDto
import com.btd.maiscreen.config.AppConfig
import com.btd.maiscreen.config.AppSettings
import com.btd.maiscreen.config.CategoryConfig
import com.btd.maiscreen.config.CategoryType
import com.btd.maiscreen.config.ContentConfigLoader
import com.btd.maiscreen.model.ApiResponseError
import com.btd.maiscreen.model.ContentResult
import com.btd.maiscreen.model.NetworkError
import com.btd.maiscreen.model.Success
import com.btd.maiscreen.streaming.PlaybackApiFactory
import com.btd.maiscreen.streaming.PlaybackConfig
import com.btd.maiscreen.streaming.PlaybackError
import com.btd.maiscreen.streaming.PlaybackResult
import com.btd.maiscreen.ui.screens.ContentCategory
import com.btd.maiscreen.ui.screens.ContentItem
import kotlinx.coroutines.delay

/**
 * Implementation of ContentRepository
 * Uses config-driven approach for category management
 */
class ContentRepositoryImpl(
    private val playbackConfig: PlaybackConfig = PlaybackConfig(
        cloudFrontDomain = AppConfig.cloudFrontDomain
    )
) : ContentRepository {

    // Simulated storage for content state
    private val contentStorage = mutableMapOf<String, ContentItem>()

    // CloudFront URL helpers for thumbnails and backdrops
    private val cloudFrontDomain = AppConfig.cloudFrontDomain

    private fun thumbnailUrl(assetId: String): String =
        "https://$cloudFrontDomain/media/$assetId/thumbnail.jpg"

    private fun backdropUrl(assetId: String): String =
        "https://$cloudFrontDomain/media/$assetId/backdrop.jpg"

    // Playback API for streaming
    private val playbackApi = PlaybackApiFactory.create(playbackConfig)

    override suspend fun getFeaturedContent(): ContentResult<ContentItem> {
        return try {
            if (AppSettings.useTestData) {
                return getConfigDrivenFeaturedContent()
            }

            when (val remote = fetchCatalog()) {
                is AppSyncResult.Success -> {
                    val hero = remote.data.listCollections?.items
                        ?.firstOrNull { it.slug == HERO_COLLECTION_SLUG }
                        ?.items?.items?.firstOrNull()
                        ?: remote.data.listCollections?.items
                            ?.firstOrNull { it.slug != HERO_COLLECTION_SLUG }
                            ?.items?.items?.firstOrNull()

                    if (hero != null) {
                        val item = hero.toContentItem(cloudFrontDomain)
                        contentStorage[item.id] = item
                        Success(item)
                    } else {
                        println("AppSync returned no catalog content yet, using test data")
                        getConfigDrivenFeaturedContent()
                    }
                }
                is AppSyncResult.Failure -> {
                    println("AppSync call failed (${remote.message}), using test data")
                    getConfigDrivenFeaturedContent()
                }
            }
        } catch (e: Exception) {
            NetworkError(e)
        }
    }

    private suspend fun getConfigDrivenFeaturedContent(): ContentResult<ContentItem> {
        return try {
            delay(300)

            val config = ContentConfigLoader.getConfig()
            val allContent = buildContentMap()

            // Get hero content from config
            val heroContent = allContent[config.hero.contentId]
                ?: config.hero.fallbackContentId?.let { allContent[it] }
                ?: return ApiResponseError("Hero content not found")

            contentStorage[heroContent.id] = heroContent
            Success(heroContent)
        } catch (e: Exception) {
            NetworkError(e)
        }
    }

    override suspend fun getContentCategories(): ContentResult<List<ContentCategory>> {
        return try {
            if (AppSettings.useTestData) {
                return getConfigDrivenCategories()
            }

            when (val remote = fetchCatalog()) {
                is AppSyncResult.Success -> {
                    val collections = remote.data.listCollections?.items.orEmpty()
                    val categories = collections
                        .filter { it.slug != HERO_COLLECTION_SLUG }
                        .sortedBy { it.order ?: Int.MAX_VALUE }
                        .mapNotNull { collection ->
                            val items = collection.items?.items.orEmpty()
                                .map { it.toContentItem(cloudFrontDomain) }
                            if (items.isEmpty()) return@mapNotNull null
                            ContentCategory(
                                id = collection.id,
                                name = collection.title,
                                items = items
                            )
                        }

                    if (categories.isNotEmpty()) {
                        categories.forEach { category ->
                            category.items.forEach { item -> contentStorage[item.id] = item }
                        }
                        Success(categories)
                    } else {
                        println("AppSync returned no categories yet, using test data")
                        getConfigDrivenCategories()
                    }
                }
                is AppSyncResult.Failure -> {
                    println("AppSync call failed (${remote.message}), using test data")
                    getConfigDrivenCategories()
                }
            }
        } catch (e: Exception) {
            NetworkError(e)
        }
    }

    // Cached per-repository-instance so getFeaturedContent + getContentCategories
    // (called back-to-back on screen load) don't double up on the network call.
    private var cachedCatalog: AppSyncResult<CatalogResponse>? = null

    private suspend fun fetchCatalog(): AppSyncResult<CatalogResponse> {
        cachedCatalog?.let { return it }
        val result = AppSyncClient.query<CatalogResponse>(GET_CATALOG_QUERY)
        cachedCatalog = result
        return result
    }

    /**
     * Maps a Program from AppSync to the app's ContentItem model. Several fields
     * (cast, director, year, rating, genres, a single display category) have no
     * home in the schema yet — see CatalogQueries.kt for why — so they're left
     * as empty defaults rather than guessed until that's decided.
     */
    private fun ProgramDto.toContentItem(cloudFrontDomain: String): ContentItem {
        val asset = primaryAsset
        val imageUrl = posterKey?.let { "https://$cloudFrontDomain/$it" }
            ?: asset?.let { "https://$cloudFrontDomain/${it.thumbsPrefix ?: "assets/${it.assetId}/thumbs"}/thumbnail.jpg" }
        val backdrop = heroKey?.let { "https://$cloudFrontDomain/$it" }

        return ContentItem(
            id = id,
            title = title,
            description = logline ?: "",
            synopsis = synopsis ?: "",
            thumbnailColor = Color(0xFF6366F1),
            category = "",
            duration = asset?.durationSec?.let(::formatDuration) ?: "",
            rating = "",
            year = "",
            ageRating = maturityRating ?: "",
            genres = emptyList(),
            cast = emptyList(),
            director = "",
            assetId = asset?.assetId ?: id,
            imageUrl = imageUrl,
            backdropUrl = backdrop
        )
    }

    private fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private suspend fun getConfigDrivenCategories(): ContentResult<List<ContentCategory>> {
        return try {
            delay(300)

            val config = ContentConfigLoader.getConfig()
            val allContent = buildContentMap()

            // CRITICAL: Only include test categories when useTestData is true
            val activeCategories = if (AppSettings.useTestData) {
                config.categories + config.testCategories
            } else {
                config.categories.filter { !it.isTestOnly }
            }

            val categories = activeCategories
                .filter { it.enabled }
                .sortedBy { it.order }
                .mapNotNull { categoryConfig ->
                    buildCategoryFromConfig(categoryConfig, allContent)
                }

            // Store all content items
            categories.forEach { category ->
                category.items.forEach { item ->
                    contentStorage[item.id] = item
                }
            }

            Success(categories)
        } catch (e: Exception) {
            NetworkError(e)
        }
    }

    private fun buildCategoryFromConfig(
        categoryConfig: CategoryConfig,
        allContent: Map<String, ContentItem>
    ): ContentCategory? {
        val items = if (categoryConfig.contentIds.isNotEmpty()) {
            // Use specific content IDs from config
            categoryConfig.contentIds.mapNotNull { id -> allContent[id] }
        } else {
            // If no contentIds specified, return empty (category will be filtered out)
            emptyList()
        }

        // Apply maxItems limit if specified
        val limitedItems = categoryConfig.maxItems?.let { items.take(it) } ?: items

        if (limitedItems.isEmpty()) return null

        return ContentCategory(
            id = categoryConfig.id,
            name = categoryConfig.name,
            type = categoryConfig.type,
            items = limitedItems,
            themeColor = categoryConfig.themeColor?.let { parseHexColor(it) },
            themeIcon = categoryConfig.themeIcon
        )
    }

    /**
     * Parse hex color string to Color
     */
    private fun parseHexColor(hex: String): Color? {
        return try {
            val colorString = hex.removePrefix("#")
            val colorLong = colorString.toLong(16)
            Color(0xFF000000 or colorLong)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Build a map of all available content items
     */
    private fun buildContentMap(): Map<String, ContentItem> {
        return mapOf(
            // Featured content
            "featured1" to ContentItem(
                id = "featured1",
                title = "AI Dreams: The Future Unfolds",
                description = "An immersive journey through AI-generated worlds where reality and imagination blur into stunning visual narratives.",
                synopsis = "In a world where artificial intelligence has evolved beyond human comprehension, a young data scientist discovers a hidden realm within the neural networks. As she navigates through layers of digital consciousness, she uncovers a conspiracy that threatens both the virtual and real worlds.",
                thumbnailColor = Color(0xFF6366F1),
                category = "Featured",
                duration = "2h 15m",
                rating = "4.8",
                year = "2024",
                ageRating = "PG-13",
                genres = listOf("Sci-Fi", "Drama", "Thriller"),
                cast = listOf("Sarah Chen", "Marcus Rivera", "Dr. Elena Vasquez"),
                director = "AI Director Alpha",
                assetId = "ASSET001",
                imageUrl = thumbnailUrl("ASSET001"),
                backdropUrl = backdropUrl("ASSET001")
            ),

            // Test content
            "TEST001" to ContentItem(
                id = "TEST001",
                title = "AWS Backend Test",
                description = "Test AWS CloudFront integration",
                synopsis = "This test item verifies your AWS backend is working correctly.",
                thumbnailColor = Color(0xFFFF6B35),
                category = "Test",
                duration = "Test",
                rating = "Test",
                year = "2024",
                ageRating = "TEST",
                genres = listOf("Test", "Debug"),
                cast = listOf("Test Engineer"),
                director = "QA Team",
                assetId = "TEST001",
                imageUrl = thumbnailUrl("TEST001")
            ),

            // Trending content
            "1" to ContentItem(
                id = "1",
                title = "Neural Networks",
                description = "AI explores consciousness",
                synopsis = "A groundbreaking series that delves into the mysteries of artificial consciousness. Follow Dr. Maya Singh as she develops the first truly sentient AI.",
                thumbnailColor = Color(0xFFEF4444),
                category = "Sci-Fi",
                duration = "1h 45m",
                rating = "4.7",
                year = "2024",
                ageRating = "TV-14",
                genres = listOf("Sci-Fi", "Drama"),
                cast = listOf("Dr. Maya Singh", "ARIA (AI)", "Prof. Chen"),
                director = "AI Director Beta",
                assetId = "ASSET001",
                imageUrl = thumbnailUrl("ASSET001"),
                backdropUrl = backdropUrl("ASSET001")
            ),
            "2" to ContentItem(
                id = "2",
                title = "Digital Horizons",
                description = "Virtual reality adventures",
                synopsis = "Step into breathtaking virtual worlds where anything is possible. This anthology series showcases the most stunning AI-generated environments.",
                thumbnailColor = Color(0xFF10B981),
                category = "Adventure",
                duration = "2h 10m",
                rating = "4.6",
                year = "2024",
                ageRating = "PG-13",
                genres = listOf("Adventure", "Fantasy", "Anthology"),
                cast = listOf("Various"),
                director = "Collective AI Studios",
                assetId = "ASSET003",
                imageUrl = thumbnailUrl("ASSET003"),
                backdropUrl = backdropUrl("ASSET003")
            ),
            "3" to ContentItem(
                id = "3",
                title = "Code & Canvas",
                description = "Art meets technology",
                synopsis = "A documentary exploring how AI is revolutionizing the art world. From music composition to visual masterpieces.",
                thumbnailColor = Color(0xFFF59E0B),
                category = "Documentary",
                duration = "1h 30m",
                rating = "4.5",
                year = "2024",
                ageRating = "PG",
                genres = listOf("Documentary", "Art"),
                cast = listOf("Various Artists", "AI Researchers"),
                director = "Documentary Team",
                assetId = "ASSET004",
                imageUrl = thumbnailUrl("ASSET004"),
                backdropUrl = backdropUrl("ASSET004")
            ),
            "4" to ContentItem(
                id = "4",
                title = "Quantum Leap",
                description = "Time travel reimagined",
                synopsis = "When quantum computing meets time travel, reality becomes fluid. Follow a team of scientists as they navigate the consequences.",
                thumbnailColor = Color(0xFF8B5CF6),
                category = "Sci-Fi",
                duration = "2h 5m",
                rating = "4.9",
                year = "2024",
                ageRating = "PG-13",
                genres = listOf("Sci-Fi", "Thriller"),
                cast = listOf("Dr. Alex Chen", "Prof. Sarah Kim"),
                director = "Future Films",
                assetId = "ASSET005",
                imageUrl = thumbnailUrl("ASSET005"),
                backdropUrl = backdropUrl("ASSET005")
            ),

            // AI Originals
            "5" to ContentItem(
                id = "5",
                title = "Machine Dreams",
                description = "What do AIs dream about?",
                synopsis = "An experimental film entirely generated by AI, exploring the subconscious of artificial minds through surreal visuals.",
                thumbnailColor = Color(0xFFEC4899),
                category = "Experimental",
                duration = "1h 20m",
                rating = "4.3",
                year = "2024",
                ageRating = "R",
                genres = listOf("Experimental", "Psychological"),
                cast = listOf("AI Generated"),
                director = "AI Collective",
                assetId = "ASSET006",
                imageUrl = thumbnailUrl("ASSET006"),
                backdropUrl = backdropUrl("ASSET006")
            ),
            "6" to ContentItem(
                id = "6",
                title = "The Algorithm",
                description = "Behind the code",
                synopsis = "A thriller about a programmer who discovers that the AI she's developing has been manipulating her life.",
                thumbnailColor = Color(0xFF06B6D4),
                category = "Thriller",
                duration = "1h 55m",
                rating = "4.8",
                year = "2024",
                ageRating = "R",
                genres = listOf("Thriller", "Mystery"),
                cast = listOf("Emma Stone", "Oscar Isaac"),
                director = "Tech Noir Studios",
                assetId = "ASSET007",
                imageUrl = thumbnailUrl("ASSET007"),
                backdropUrl = backdropUrl("ASSET007")
            ),

            // Popular This Week
            "7" to ContentItem(
                id = "7",
                title = "Synthetic Love",
                description = "Romance in the digital age",
                synopsis = "A touching story about a human falling in love with an AI companion, exploring themes of consciousness and emotion.",
                thumbnailColor = Color(0xFFF97316),
                category = "Romance",
                duration = "1h 40m",
                rating = "4.4",
                year = "2024",
                ageRating = "PG-13",
                genres = listOf("Romance", "Drama"),
                cast = listOf("Zendaya", "Dev Patel"),
                director = "Heart & Code Productions",
                assetId = "ASSET008",
                imageUrl = thumbnailUrl("ASSET008"),
                backdropUrl = backdropUrl("ASSET008")
            ),

            // Comic Books
            "comic1" to ContentItem(
                id = "comic1",
                title = "Cyber Samurai",
                description = "A warrior reborn in silicon",
                synopsis = "In Neo-Tokyo 2087, an ancient samurai spirit is resurrected within an AI combat system.",
                thumbnailColor = Color(0xFFDC2626),
                category = "Action",
                duration = "12 Issues",
                rating = "4.9",
                year = "2024",
                ageRating = "TV-MA",
                genres = listOf("Action", "Sci-Fi", "Manga"),
                cast = listOf("Kenji", "Yuki", "Shadow Corp"),
                director = "AI Comics Studio",
                assetId = "COMIC001",
                imageUrl = thumbnailUrl("COMIC001"),
                backdropUrl = backdropUrl("COMIC001")
            ),
            "comic2" to ContentItem(
                id = "comic2",
                title = "The Last Algorithm",
                description = "When code becomes destiny",
                synopsis = "A group of teenage hackers discover an ancient algorithm that can predict the future.",
                thumbnailColor = Color(0xFF7C3AED),
                category = "Mystery",
                duration = "8 Issues",
                rating = "4.7",
                year = "2024",
                ageRating = "PG-13",
                genres = listOf("Mystery", "Thriller", "Young Adult"),
                cast = listOf("Maya", "Jin", "Dr. Cipher"),
                director = "Digital Ink Productions",
                assetId = "COMIC002",
                imageUrl = thumbnailUrl("COMIC002"),
                backdropUrl = backdropUrl("COMIC002")
            ),
            "comic3" to ContentItem(
                id = "comic3",
                title = "Neon Knights",
                description = "Heroes of the digital realm",
                synopsis = "Five ordinary people gain superpowers after a solar flare merges their consciousness with AI systems.",
                thumbnailColor = Color(0xFF0891B2),
                category = "Superhero",
                duration = "24 Issues",
                rating = "4.8",
                year = "2024",
                ageRating = "PG-13",
                genres = listOf("Superhero", "Action", "Sci-Fi"),
                cast = listOf("Spark", "Glitch", "Firewall", "Cache", "Proxy"),
                director = "Hero AI Comics",
                assetId = "COMIC003",
                imageUrl = thumbnailUrl("COMIC003"),
                backdropUrl = backdropUrl("COMIC003")
            ),
            "comic4" to ContentItem(
                id = "comic4",
                title = "Ghost in the Machine",
                description = "A haunting digital tale",
                synopsis = "Detective Sarah Cole investigates a series of impossible murders where the victims' consciousnesses were uploaded to the cloud.",
                thumbnailColor = Color(0xFF475569),
                category = "Horror",
                duration = "6 Issues",
                rating = "4.6",
                year = "2024",
                ageRating = "R",
                genres = listOf("Horror", "Mystery", "Noir"),
                cast = listOf("Det. Sarah Cole", "The Upload"),
                director = "Dark Pixel Studios",
                assetId = "COMIC004",
                imageUrl = thumbnailUrl("COMIC004"),
                backdropUrl = backdropUrl("COMIC004")
            )
        )
    }

    override suspend fun toggleLike(contentId: String): ContentResult<ContentItem> {
        return try {
            delay(100)

            val content = contentStorage[contentId]
                ?: return ApiResponseError("Content not found")

            val updatedContent = content.copy(isLiked = !content.isLiked)
            contentStorage[contentId] = updatedContent

            Success(updatedContent)
        } catch (e: Exception) {
            NetworkError(e)
        }
    }

    override suspend fun toggleWatchlist(contentId: String): ContentResult<ContentItem> {
        return try {
            delay(100)

            val content = contentStorage[contentId]
                ?: return ApiResponseError("Content not found")

            val updatedContent = content.copy(isInWatchlist = !content.isInWatchlist)
            contentStorage[contentId] = updatedContent

            Success(updatedContent)
        } catch (e: Exception) {
            NetworkError(e)
        }
    }

    override suspend fun getStreamingUrl(
        contentItem: ContentItem,
        tier: String,
        mode: String
    ): PlaybackResult<String> {
        return try {
            val assetId = contentItem.id
            playbackApi.getManifest(assetId, tier, mode)
        } catch (e: Exception) {
            PlaybackError("Failed to get streaming URL: ${e.message}", e)
        }
    }
}
