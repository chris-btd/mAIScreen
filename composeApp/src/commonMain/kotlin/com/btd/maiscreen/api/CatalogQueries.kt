package com.btd.maiscreen.api

import kotlinx.serialization.Serializable

/**
 * GraphQL query against the AppSync schema documented in aws_backend_setup_guide_v_0.md
 * (Collection -> Program -> MediaAsset, joined by MediaAsset.assetId matching the
 * S3 "assets/{assetId}/..." layout).
 *
 * IMPORTANT: this schema is still a draft — the content pipeline (Submission -> Review
 * -> Program) hasn't been built yet, and several ContentItem fields (cast, director,
 * year, rating, genres, a single display category) don't have a home in the schema
 * yet. Once that's decided, this query and the mapping below will need updating —
 * everything not yet backed by a real field is left as an empty/default value rather
 * than guessed.
 */
internal const val GET_CATALOG_QUERY = """
    query GetCatalog {
      listCollections(filter: { published: { eq: true } }, limit: 20) {
        items {
          id
          title
          slug
          order
          items {
            items {
              id
              title
              logline
              synopsis
              maturityRating
              posterKey
              heroKey
              primaryAsset {
                assetId
                durationSec
                thumbsPrefix
              }
            }
          }
        }
      }
    }
"""

@Serializable
data class CatalogResponse(
    val listCollections: CollectionConnectionDto? = null
)

@Serializable
data class CollectionConnectionDto(
    val items: List<CollectionDto> = emptyList()
)

@Serializable
data class CollectionDto(
    val id: String,
    val title: String,
    val slug: String? = null,
    val order: Int? = null,
    val items: ProgramConnectionDto? = null
)

@Serializable
data class ProgramConnectionDto(
    val items: List<ProgramDto> = emptyList()
)

@Serializable
data class ProgramDto(
    val id: String,
    val title: String,
    val logline: String? = null,
    val synopsis: String? = null,
    val maturityRating: String? = null,
    val posterKey: String? = null,
    val heroKey: String? = null,
    val primaryAsset: MediaAssetDto? = null
)

@Serializable
data class MediaAssetDto(
    val assetId: String,
    val durationSec: Int? = null,
    val thumbsPrefix: String? = null
)

/** The slug a curated "hero"/featured Collection is expected to use. */
internal const val HERO_COLLECTION_SLUG = "hero"
