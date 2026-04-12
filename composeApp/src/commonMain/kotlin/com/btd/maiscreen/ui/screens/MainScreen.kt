package com.btd.maiscreen.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.btd.maiscreen.config.CategoryType
import com.btd.maiscreen.ui.state.MainScreenUiState
import com.btd.maiscreen.viewmodel.MainScreenViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

data class ContentItem(
    val id: String,
    val title: String,
    val description: String,
    val synopsis: String,
    val thumbnailColor: Color,
    val category: String,
    val duration: String,
    val rating: String,
    val year: String,
    val ageRating: String,
    val genres: List<String>,
    val cast: List<String>,
    val director: String,
    val assetId: String = id, // Use ID as asset ID by default
    val isLiked: Boolean = false,
    val isInWatchlist: Boolean = false,
    val imageUrl: String? = null,  // URL for content thumbnail
    val backdropUrl: String? = null  // URL for hero background
)

data class ContentCategory(
    val id: String = "",
    val name: String,
    val type: CategoryType = CategoryType.STANDARD,
    val items: List<ContentItem>,
    val themeColor: Color? = null,
    val themeIcon: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    onContentClick: (ContentItem) -> Unit = {},
    onPlayContent: (ContentItem) -> Unit = {},
    onToggleLike: (ContentItem) -> Unit = {},
    onToggleWatchlist: (ContentItem) -> Unit = {},
    recentlyWatched: List<ContentItem> = emptyList(),
    viewModel: MainScreenViewModel = remember { MainScreenViewModel() }
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.state.collectAsState()

    // Drawer state
    var showDrawer by remember { mutableStateOf(false) }

    // Handle loading state
    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    // Handle error state
    if (uiState.hasError) {
        ErrorScreen(
            message = uiState.errorMessage ?: "Unknown error occurred",
            onRetry = { viewModel.retryLoadContent() }
        )
        return
    }

    // Get data from ViewModel state
    val featuredContent = uiState.featuredContent
    val contentCategories = uiState.contentCategories
    val watchList = uiState.watchList

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            // Top Navigation Bar
            TopNavigationBar(
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
                onMenuClick = { showDrawer = true }
            )
            
            // Featured Content Hero Section
            featuredContent?.let { content ->
                FeaturedContentHero(
                    content = content,
                    onPlayClick = { onPlayContent(content) }
                )
            }
            
            // Recently Watched Section (only show if recently watched has items)
            if (recentlyWatched.isNotEmpty()) {
                RecentlyWatchedSection(
                    recentlyWatched = recentlyWatched,
                    onContentClick = onContentClick,
                    onShowDetails = { content ->
                        viewModel.showContentDetails(content)
                        onContentClick(content)
                    },
                    onPlayContent = onPlayContent,
                    onToggleLike = onToggleLike,
                    onToggleWatchlist = onToggleWatchlist
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // My List Section (only show if watchlist has items)
            if (watchList.isNotEmpty()) {
                MyListSection(
                    watchList = watchList,
                    onContentClick = onContentClick,
                    onShowDetails = { content ->
                        viewModel.showContentDetails(content)
                        onContentClick(content)
                    },
                    onPlayContent = onPlayContent,
                    onToggleLike = onToggleLike,
                    onToggleWatchlist = onToggleWatchlist
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Content Categories
            contentCategories.forEach { category ->
                ContentCategorySection(
                    category = category,
                    onContentClick = onContentClick,
                    onShowDetails = { content ->
                        viewModel.showContentDetails(content)
                        onContentClick(content)
                    },
                    onPlayContent = onPlayContent,
                    onToggleLike = onToggleLike,
                    onToggleWatchlist = onToggleWatchlist
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Details Dialog
        if (uiState.showDetailsDialog && uiState.selectedContent != null) {
            ContentDetailsDialog(
                content = uiState.selectedContent!!,
                onDismiss = { viewModel.hideContentDetails() },
                onPlay = { content ->
                    onPlayContent(content)
                    viewModel.hideContentDetails()
                },
                onToggleLike = { content -> viewModel.toggleLike(content) },
                onToggleWatchlist = { content -> viewModel.toggleWatchlist(content) }
            )
        }
        
        // Sliding Drawer
        if (showDrawer) {
            SlidingDrawer(
                onDismiss = { showDrawer = false },
                onSettingsClick = {
                    showDrawer = false
                    onSettingsClick()
                },
                onUploadClick = {
                    showDrawer = false
                    onUploadClick()
                }
            )
        }
    }
}

@Composable
private fun TopNavigationBar(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Profile image + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular profile image button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 20.sp
                )
            }
            
            Text(
                text = "mAIScreen",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Right side: Search and Profile icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onSearchClick) {
                Text(
                    text = "🔍",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
            
            IconButton(onClick = onProfileClick) {
                Text(
                    text = "📊",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FeaturedContentHero(
    content: ContentItem,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        // Image background
        if (content.backdropUrl != null) {
            AsyncImage(
                model = content.backdropUrl,
                contentDescription = content.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback to solid color if no image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(content.thumbnailColor)
            )
        }

        // Bottom gradient overlay (from black to transparent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black
                        )
                    )
                )
        )

        // Left gradient overlay (from black to transparent)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 48.dp, vertical = 48.dp)
                .fillMaxWidth(0.6f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = content.title,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )

            Text(
                text = content.description,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 26.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF46D369)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${content.rating}",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = content.duration,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Text(
                    text = content.year,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                // Age rating badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = content.ageRating,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play button - white fill
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "▶",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // More Info button - semi-transparent
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "ℹ",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "More Info",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyWatchedSection(
    recentlyWatched: List<ContentItem>,
    onContentClick: (ContentItem) -> Unit,
    onShowDetails: (ContentItem) -> Unit,
    onPlayContent: (ContentItem) -> Unit,
    onToggleLike: (ContentItem) -> Unit,
    onToggleWatchlist: (ContentItem) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🕐",
                    fontSize = 20.sp
                )
                Text(
                    text = "Recently Watched",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Text(
                text = "${recentlyWatched.size} ${if (recentlyWatched.size == 1) "video" else "videos"}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentlyWatched) { item ->
                EnhancedContentCard(
                    content = item,
                    onClick = { onContentClick(item) },
                    onShowDetails = { onShowDetails(item) },
                    onPlay = { onPlayContent(item) },
                    onToggleLike = { onToggleLike(item) },
                    onToggleWatchlist = { onToggleWatchlist(item) }
                )
            }
        }
    }
}

@Composable
private fun MyListSection(
    watchList: List<ContentItem>,
    onContentClick: (ContentItem) -> Unit,
    onShowDetails: (ContentItem) -> Unit,
    onPlayContent: (ContentItem) -> Unit,
    onToggleLike: (ContentItem) -> Unit,
    onToggleWatchlist: (ContentItem) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📋",
                    fontSize = 20.sp
                )
                Text(
                    text = "My List",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Text(
                text = "${watchList.size} ${if (watchList.size == 1) "item" else "items"}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(watchList) { item ->
                EnhancedContentCard(
                    content = item,
                    onClick = { onContentClick(item) },
                    onShowDetails = { onShowDetails(item) },
                    onPlay = { onPlayContent(item) },
                    onToggleLike = { onToggleLike(item) },
                    onToggleWatchlist = { onToggleWatchlist(item) }
                )
            }
        }
    }
}

@Composable
private fun ContentCategorySection(
    category: ContentCategory,
    onContentClick: (ContentItem) -> Unit,
    onShowDetails: (ContentItem) -> Unit,
    onPlayContent: (ContentItem) -> Unit,
    onToggleLike: (ContentItem) -> Unit,
    onToggleWatchlist: (ContentItem) -> Unit
) {
    Column {
        // Use themed header for themed categories, standard header for others
        when (category.type) {
            CategoryType.THEMED -> {
                ThemedCategoryHeader(
                    name = category.name,
                    themeIcon = category.themeIcon,
                    themeColor = category.themeColor
                )
            }
            else -> {
                Text(
                    text = category.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(
                if (category.type == CategoryType.TOP_10) 48.dp else 12.dp
            )
        ) {
            itemsIndexed(category.items) { index, item ->
                when (category.type) {
                    CategoryType.TOP_10 -> {
                        Top10ContentCard(
                            content = item,
                            rank = index + 1,
                            onClick = { onContentClick(item) },
                            onShowDetails = { onShowDetails(item) },
                            onPlay = { onPlayContent(item) },
                            onToggleLike = { onToggleLike(item) },
                            onToggleWatchlist = { onToggleWatchlist(item) }
                        )
                    }
                    else -> {
                        EnhancedContentCard(
                            content = item,
                            onClick = { onContentClick(item) },
                            onShowDetails = { onShowDetails(item) },
                            onPlay = { onPlayContent(item) },
                            onToggleLike = { onToggleLike(item) },
                            onToggleWatchlist = { onToggleWatchlist(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemedCategoryHeader(
    name: String,
    themeIcon: String?,
    themeColor: Color?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themeIcon?.let {
            Text(
                text = it,
                fontSize = 24.sp
            )
        }
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = themeColor ?: Color.White
        )
        themeColor?.let {
            // Colored underline indicator
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(it.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun Top10ContentCard(
    content: ContentItem,
    rank: Int,
    onClick: () -> Unit,
    onShowDetails: () -> Unit,
    onPlay: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        label = "top10_card_scale"
    )

    Row(
        modifier = Modifier
            .height(200.dp)
            .scale(scale),
        verticalAlignment = Alignment.Bottom
    ) {
        // Large outlined rank number
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomStart
        ) {
            // Background fill text (black)
            Text(
                text = rank.toString(),
                fontSize = 140.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                modifier = Modifier.offset(x = (-10).dp, y = 30.dp)
            )
            // Outline text (white stroke effect using shadow)
            Text(
                text = rank.toString(),
                fontSize = 140.sp,
                fontWeight = FontWeight.Black,
                color = Color.Transparent,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.White,
                        offset = Offset(0f, 0f),
                        blurRadius = 0f
                    ),
                    drawStyle = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                ),
                modifier = Modifier.offset(x = (-10).dp, y = 30.dp)
            )
        }

        // Content card - 2:3 aspect ratio (poster style)
        Card(
            modifier = Modifier
                .width(130.dp)
                .aspectRatio(2f / 3f)  // 2:3 poster aspect ratio
                .offset(x = (-25).dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onShowDetails()
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = content.thumbnailColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isHovered) 12.dp else 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Image background
                if (content.imageUrl != null) {
                    AsyncImage(
                        model = content.imageUrl,
                        contentDescription = content.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                )
                // Hover overlay
                if (isHovered) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onPlay,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            Text(
                                text = "▶",
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Title at bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = content.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedContentCard(
    content: ContentItem,
    onClick: () -> Unit,
    onShowDetails: () -> Unit,
    onPlay: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .width(200.dp)
            .aspectRatio(16f / 9f)  // 16:9 aspect ratio
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onShowDetails()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = content.thumbnailColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 12.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image background
            if (content.imageUrl != null) {
                AsyncImage(
                    model = content.imageUrl,
                    contentDescription = content.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isHovered) 0.9f else 0.7f)
                            )
                        )
                    )
            )

            // Hover overlay with action buttons
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play button
                        IconButton(
                            onClick = onPlay,
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            Text(
                                text = "▶",
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Like button
                            IconButton(
                                onClick = onToggleLike,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.5f),
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Text(
                                    text = if (content.isLiked) "❤️" else "🤍",
                                    fontSize = 12.sp
                                )
                            }

                            // Watchlist button
                            IconButton(
                                onClick = onToggleWatchlist,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.5f),
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Text(
                                    text = if (content.isInWatchlist) "✓" else "+",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // More info button
                            IconButton(
                                onClick = onShowDetails,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.5f),
                                        RoundedCornerShape(14.dp)
                                    )
                            ) {
                                Text(
                                    text = "ⓘ",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Title overlay at bottom (visible on hover)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black
                                )
                            )
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = content.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentDetailsDialog(
    content: ContentItem,
    onDismiss: () -> Unit,
    onPlay: (ContentItem) -> Unit,
    onToggleLike: (ContentItem) -> Unit,
    onToggleWatchlist: (ContentItem) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B1B1B)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "×",
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
                
                // Content thumbnail
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = content.thumbnailColor
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        
                        // Play button overlay
                        IconButton(
                            onClick = { onPlay(content) },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                                .background(
                                    Color.White.copy(alpha = 0.9f),
                                    RoundedCornerShape(32.dp)
                                )
                        ) {
                            Text(
                                text = "▶️",
                                fontSize = 28.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content details
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Title and year
                    Text(
                        text = "${content.title} (${content.year})",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Rating, duration, age rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = content.rating,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = content.duration,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = content.ageRating,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play button
                        Button(
                            onClick = { onPlay(content) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "▶️",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Like button
                        OutlinedButton(
                            onClick = { onToggleLike(content) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (content.isLiked) "❤️" else "🤍",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (content.isLiked) "Liked" else "Like",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Watchlist button
                        OutlinedButton(
                            onClick = { onToggleWatchlist(content) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (content.isInWatchlist) "✓" else "+",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (content.isInWatchlist) "In List" else "My List",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Synopsis
                    Text(
                        text = "Synopsis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = content.synopsis,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Genres
                    Text(
                        text = "Genres",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(content.genres) { genre ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Cast and crew
                    Text(
                        text = "Cast & Crew",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Director: ${content.director}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Cast: ${content.cast.joinToString(", ")}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF6366F1)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading content...",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Oops! Something went wrong",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SlidingDrawer(
    onDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    // Dark overlay background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    )
    
    // Drawer content
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Drawer panel
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(Color(0xFF1B2951))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* Prevent clicks from passing through */ }
        ) {
            // Header with profile
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 40.sp
                    )
                }
                
                Text(
                    text = "My Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Menu items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Upload option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUploadClick)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📤",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Upload",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                // Settings option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSettingsClick)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Settings",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            // Footer
            Text(
                text = "mAIScreen v1.0.0",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        // Empty space (to allow clicking outside drawer to close)
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}
