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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val isInWatchlist: Boolean = false
)

data class ContentCategory(
    val name: String,
    val items: List<ContentItem>
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
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    
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
    
    // Generate random stars for background
    val stars = remember {
        List(100) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat()
            )
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background gradient with stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.verticalGradient(
                colors = listOf(black, navyBlue),
                startY = 0f,
                endY = size.height
            )
            drawRect(gradient)
            
            // Draw stars
            stars.forEach { star ->
                val starSize = Random.nextFloat() * 2f + 1f
                drawCircle(
                    color = starWhite.copy(alpha = Random.nextFloat() * 0.3f + 0.1f),
                    radius = starSize,
                    center = Offset(
                        x = star.x * size.width,
                        y = star.y * size.height
                    )
                )
            }
        }
        
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
            .height(400.dp)
            .padding(horizontal = 24.dp)
    ) {
        // Featured content background
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = content.thumbnailColor
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // Content info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = content.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = content.description,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 22.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
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
                        
                        Text(
                            text = content.category,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Play button
                    Button(
                        onClick = onPlayClick,
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
        Text(
            text = category.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(category.items) { item ->
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
            .width(160.dp)
            .height(240.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { 
                if (isHovered) {
                    // On desktop/web, clicking when hovered shows details
                    onShowDetails()
                } else {
                    // On mobile, single click shows details
                    onShowDetails()
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = content.thumbnailColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
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
                                Color.Black.copy(alpha = if (isHovered) 0.8f else 0.6f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Hover overlay with action buttons
            androidx.compose.animation.AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play button
                        IconButton(
                            onClick = onPlay,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(24.dp)
                                )
                        ) {
                            Text(
                                text = "▶️",
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Like button
                            IconButton(
                                onClick = onToggleLike,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Text(
                                    text = if (content.isLiked) "❤️" else "🤍",
                                    fontSize = 14.sp
                                )
                            }
                            
                            // Watchlist button
                            IconButton(
                                onClick = onToggleWatchlist,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Text(
                                    text = if (content.isInWatchlist) "✓" else "+",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // More info button
                            IconButton(
                                onClick = onShowDetails,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Text(
                                    text = "i",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // Content info (always visible)
            androidx.compose.animation.AnimatedVisibility(
                visible = !isHovered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = content.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = content.rating,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = content.duration,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
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
