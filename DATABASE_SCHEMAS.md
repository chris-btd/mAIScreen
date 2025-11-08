# mAIScreen AWS AppSync GraphQL Schema

This document contains the complete GraphQL Schema Definition Language (SDL) schema for AWS AppSync. You can copy and paste this directly into your AppSync API.

---

## Complete GraphQL Schema

Copy everything from the line below to paste into AWS AppSync:

```graphql
# ============================================================================
# mAIScreen GraphQL Schema for AWS AppSync
# ============================================================================

# ============================================================================
# ENUMS
# ============================================================================

enum UserStatus {
  active
  suspended
  deleted
}

enum DeviceType {
  mobile
  tablet
  desktop
  tv
}

enum Platform {
  android
  ios
  web
  desktop
}

enum PlaybackQuality {
  p720
  p1080
  p4k
}

enum ContentType {
  movie
  series
  short_film
  podcast
  audiobook
}

enum ContentStatus {
  draft
  pending_review
  approved
  rejected
  published
  archived
}

enum SubscriptionStatus {
  active
  cancelled
  expired
  trial
}

enum BillingCycle {
  monthly
  annual
  lifetime
}

enum PaymentMethod {
  stripe
  apple_pay
  google_pay
  paypal
}

enum TransactionType {
  subscription
  minutes
  refund
}

enum TransactionStatus {
  pending
  completed
  failed
  refunded
}

enum SubmissionContentType {
  audio
  video
}

enum SubmissionSubType {
  movie
  series
  short_film
  podcast
  audiobook
}

enum SubmissionStatus {
  draft
  pending_review
  approved
  rejected
  published
}

enum FileType {
  video
  audio
  thumbnail
  poster
}

enum ProcessingStatus {
  uploading
  processing
  completed
  failed
}

enum ReportReason {
  inappropriate_content
  copyright_violation
  misleading_spam
  violence_harmful
  hate_speech
  other
}

enum ReportStatus {
  pending
  reviewing
  resolved
  dismissed
}

enum ReviewDecision {
  approved
  rejected
  needs_revision
}

enum ActivityType {
  login
  logout
  content_play
  content_like
  content_watchlist
  subscription_change
  payment
  submission_create
  search
  profile_update
}

# ============================================================================
# SCALARS
# ============================================================================

scalar AWSDate
scalar AWSTime
scalar AWSDateTime
scalar AWSTimestamp
scalar AWSEmail
scalar AWSJSON
scalar AWSURL
scalar AWSPhone

# ============================================================================
# TYPES
# ============================================================================

type User @aws_auth(cognito_groups: ["Users"]) {
  userId: ID!
  email: AWSEmail!
  firstName: String!
  lastName: String!
  displayName: String
  avatarUrl: AWSURL
  
  cognitoUserId: String!
  emailVerified: Boolean!
  createdAt: AWSDateTime!
  lastLoginAt: AWSDateTime
  
  status: UserStatus!
  suspendedReason: String
  suspendedUntil: AWSDateTime
  
  preferences: UserPreferences!
  metadata: UserMetadata
  
  # Resolved fields
  subscription: UserSubscription
  watchlist: [UserWatchlist!]
  likes: [UserLike!]
  recentlyWatched: [RecentlyWatched!]
  ratings: [UserRating!]
  submissions: [Submission!]
  paymentTransactions: [PaymentTransaction!]
}

type UserPreferences {
  notificationsEnabled: Boolean!
  emailNotifications: Boolean!
  playbackQuality: PlaybackQuality!
  autoplayEnabled: Boolean!
}

type UserMetadata {
  ipAddress: String
  userAgent: String
  lastDeviceInfo: String
}

type UserSession {
  sessionId: ID!
  userId: ID!
  deviceId: String
  deviceType: DeviceType!
  platform: Platform!
  
  accessToken: String! # Encrypted in DynamoDB
  refreshToken: String # Encrypted in DynamoDB
  expiresAt: AWSDateTime!
  
  ipAddress: String
  countryCode: String
  timezone: String
  
  isActive: Boolean!
  lastActivityAt: AWSDateTime!
  createdAt: AWSDateTime!
  terminatedAt: AWSDateTime
  
  user: User
}

# ============================================================================
# CONTENT TYPES
# ============================================================================

type Content @aws_auth(cognito_groups: ["Users", "Admins"]) {
  contentId: ID!
  title: String!
  description: String!
  synopsis: String!
  thumbnailUrl: AWSURL
  thumbnailColor: String!
  posterUrl: AWSURL
  
  contentType: ContentType!
  subType: String
  
  duration: Int! # Duration in seconds
  durationDisplay: String!
  releaseYear: Int!
  ageRating: String!
  
  category: String!
  genres: [String!]!
  tags: [String!]
  
  director: String
  creator: String
  host: String
  author: String
  cast: [String!]!
  productionCompany: String
  
  # Series-specific
  episodeCount: Int
  seasons: Int
  currentSeason: Int
  
  # Podcast-specific
  episodeDuration: Int # Average episode duration in seconds
  totalEpisodes: Int
  
  # Streaming
  assetId: String!
  manifestUrl: AWSURL
  videoUrl: AWSURL
  availableQualities: [PlaybackQuality!]!
  
  # Status & Moderation
  status: ContentStatus!
  reviewedBy: ID
  reviewedAt: AWSDateTime
  rejectionReason: String
  
  # Metadata
  submittedBy: ID
  featured: Boolean!
  isOriginal: Boolean!
  
  # Statistics
  rating: Float!
  ratingCount: Int!
  viewCount: Int!
  likeCount: Int!
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  publishedAt: AWSDateTime
  
  # Resolved fields
  episodes: [Episode!]
  categoryData: ContentCategory
  userLiked: Boolean
  inWatchlist: Boolean
  userRating: UserRating
}

type ContentCategory {
  categoryId: ID!
  name: String!
  displayOrder: Int!
  description: String
  thumbnailUrl: AWSURL
  
  contentType: ContentType
  contentIds: [ID!]!
  
  isActive: Boolean!
  isFeatured: Boolean!
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  
  # Resolved fields
  contents: [Content!]
}

type Episode {
  episodeId: ID!
  seriesId: ID!
  
  title: String!
  description: String!
  episodeNumber: Int!
  seasonNumber: Int!
  
  duration: Int! # Duration in seconds
  thumbnailUrl: AWSURL
  assetId: String!
  
  airDate: AWSDate
  rating: Float
  viewCount: Int!
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  
  # Resolved fields
  series: Content
}

# ============================================================================
# SUBSCRIPTION TYPES
# ============================================================================

type SubscriptionTier {
  tierId: ID!
  name: String!
  
  price: Int! # Price in cents
  currency: String!
  priceDisplay: String!
  priceSubtext: String!
  billingCycle: BillingCycle!
  
  deviceLimit: Int!
  streamingQuality: [PlaybackQuality!]!
  submissionMinutes: Int!
  minutesRollOver: Boolean!
  features: [String!]!
  
  isPopular: Boolean!
  displayOrder: Int!
  isActive: Boolean!
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

type UserSubscription {
  subscriptionId: ID!
  userId: ID!
  
  tierId: ID!
  tierName: String!
  
  status: SubscriptionStatus!
  startDate: AWSDateTime!
  endDate: AWSDateTime
  nextBillingDate: AWSDateTime
  cancelAtPeriodEnd: Boolean!
  
  monthlyMinutesAllotted: Int!
  minutesUsed: Int!
  minutesRemaining: Int!
  purchasedMinutes: Int!
  rolloverMinutes: Int!
  
  stripeSubscriptionId: String
  stripeCustomerId: String
  paymentMethodId: String
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  cancelledAt: AWSDateTime
  
  # Resolved fields
  user: User
  tier: SubscriptionTier
}

type PaymentTransaction {
  transactionId: ID!
  userId: ID!
  
  type: TransactionType!
  amount: Int! # Amount in cents
  currency: String!
  
  paymentMethod: PaymentMethod!
  stripePaymentIntentId: String
  stripeChargeId: String
  
  subscriptionId: ID
  tierId: ID
  minutesPurchased: Int
  minutePackageId: ID
  
  status: TransactionStatus!
  failureReason: String
  metadata: AWSJSON
  
  createdAt: AWSDateTime!
  completedAt: AWSDateTime
  
  # Resolved fields
  user: User
  subscription: UserSubscription
  minutePackage: MinutePackage
}

type MinutePackage {
  packageId: ID!
  name: String!
  minutes: Int!
  price: Int! # Price in cents
  priceDisplay: String!
  currency: String!
  
  isPopular: Boolean!
  displayOrder: Int!
  isActive: Boolean!
  
  bonusMinutes: Int
  description: String
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
}

# ============================================================================
# USER ENGAGEMENT TYPES
# ============================================================================

type UserWatchlist {
  watchlistId: ID!
  userId: ID!
  contentId: ID!
  
  addedAt: AWSDateTime!
  notes: String
  
  # Resolved fields
  user: User
  content: Content
}

type UserLike {
  likeId: ID!
  userId: ID!
  contentId: ID!
  
  createdAt: AWSDateTime!
  
  # Resolved fields
  user: User
  content: Content
}

type RecentlyWatched {
  watchId: ID!
  userId: ID!
  contentId: ID!
  
  progress: Int! # Watch progress in seconds
  completionPercentage: Int! # 0-100
  
  episodeId: ID
  seasonNumber: Int
  episodeNumber: Int
  
  startedAt: AWSDateTime!
  lastWatchedAt: AWSDateTime!
  completedAt: AWSDateTime
  
  # Resolved fields
  user: User
  content: Content
  episode: Episode
}

type UserRating {
  ratingId: ID!
  userId: ID!
  contentId: ID!
  
  rating: Int! # 1-5 stars
  review: String
  isPublic: Boolean!
  helpfulCount: Int!
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  
  # Resolved fields
  user: User
  content: Content
}

# ============================================================================
# SUBMISSION TYPES
# ============================================================================

type Submission {
  submissionId: ID!
  userId: ID!
  
  contentType: SubmissionContentType!
  subType: SubmissionSubType!
  
  title: String!
  description: String!
  genre: String
  year: Int
  
  duration: Int # Duration in seconds
  director: String
  creator: String
  host: String
  author: String
  cast: [String!]
  episodeCount: Int
  
  mediaFileUrl: AWSURL!
  thumbnailFileUrl: AWSURL
  mediaFileSize: Int!
  mediaFileFormat: String!
  minutesUsed: Int!
  
  status: SubmissionStatus!
  reviewedBy: ID
  reviewedAt: AWSDateTime
  reviewNotes: String
  rejectionReason: String
  
  contentId: ID # Created after approval
  
  ipAddress: String
  userAgent: String
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  submittedAt: AWSDateTime
  publishedAt: AWSDateTime
  
  # Resolved fields
  user: User
  content: Content
  mediaFiles: [SubmissionMediaFile!]
  reviews: [ContentReview!]
}

type SubmissionMediaFile {
  fileId: ID!
  submissionId: ID!
  userId: ID!
  
  fileName: String!
  fileType: FileType!
  mimeType: String!
  fileSize: Int! # Bytes
  s3Bucket: String!
  s3Key: String!
  s3Url: AWSURL!
  
  processingStatus: ProcessingStatus!
  processingError: String
  
  duration: Int # Seconds
  width: Int
  height: Int
  bitrate: Int
  codec: String
  
  transcodedFormats: [String!]
  transcodedUrls: AWSJSON # Map of format to URL
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  
  # Resolved fields
  submission: Submission
  user: User
}

# ============================================================================
# MODERATION TYPES
# ============================================================================

type ContentReport {
  reportId: ID!
  contentId: ID!
  reportedBy: ID!
  
  reason: ReportReason!
  details: String
  
  status: ReportStatus!
  reviewedBy: ID
  reviewedAt: AWSDateTime
  resolutionNotes: String
  
  createdAt: AWSDateTime!
  updatedAt: AWSDateTime!
  
  # Resolved fields
  content: Content
  reporter: User
  reviewer: User
}

type ContentReview {
  reviewId: ID!
  submissionId: ID!
  reviewerId: ID!
  
  decision: ReviewDecision!
  reviewNotes: String!
  qualityScore: Int # 1-10
  complianceScore: Int # 1-10
  
  reviewedAt: AWSDateTime!
  
  # Resolved fields
  submission: Submission
  reviewer: User
}

# ============================================================================
# ANALYTICS TYPES
# ============================================================================

type ContentView {
  viewId: ID!
  contentId: ID!
  userId: ID
  
  sessionId: String
  startedAt: AWSDateTime!
  duration: Int! # Seconds watched
  completionPercentage: Int! # 0-100
  completed: Boolean!
  
  episodeId: ID
  seasonNumber: Int
  episodeNumber: Int
  
  deviceType: DeviceType!
  platform: Platform!
  quality: PlaybackQuality!
  
  ipAddress: String
  countryCode: String
  
  # Resolved fields
  content: Content
  user: User
  episode: Episode
}

type UserActivity {
  activityId: ID!
  userId: ID!
  activityType: ActivityType!
  
  details: AWSJSON
  ipAddress: String
  userAgent: String
  deviceType: String
  
  timestamp: AWSDateTime!
  
  # Resolved fields
  user: User
}

# ============================================================================
# INPUT TYPES
# ============================================================================

input CreateUserInput {
  email: AWSEmail!
  firstName: String!
  lastName: String!
  cognitoUserId: String!
  displayName: String
}

input UpdateUserInput {
  firstName: String
  lastName: String
  displayName: String
  avatarUrl: AWSURL
  preferences: UserPreferencesInput
}

input UserPreferencesInput {
  notificationsEnabled: Boolean
  emailNotifications: Boolean
  playbackQuality: PlaybackQuality
  autoplayEnabled: Boolean
}

input CreateSubmissionInput {
  contentType: SubmissionContentType!
  subType: SubmissionSubType!
  title: String!
  description: String!
  genre: String
  year: Int
  duration: Int
  director: String
  creator: String
  host: String
  author: String
  cast: [String!]
  episodeCount: Int
  mediaFileUrl: AWSURL!
  thumbnailFileUrl: AWSURL
  mediaFileSize: Int!
  mediaFileFormat: String!
}

input UpdateSubmissionInput {
  title: String
  description: String
  genre: String
  year: Int
  director: String
  creator: String
  host: String
  author: String
  cast: [String!]
  episodeCount: Int
  status: SubmissionStatus
}

input CreateContentReportInput {
  contentId: ID!
  reason: ReportReason!
  details: String
}

input ReviewSubmissionInput {
  submissionId: ID!
  decision: ReviewDecision!
  reviewNotes: String!
  qualityScore: Int
  complianceScore: Int
}

input CreateUserRatingInput {
  contentId: ID!
  rating: Int! # 1-5
  review: String
  isPublic: Boolean
}

input UpdateUserRatingInput {
  rating: Int
  review: String
  isPublic: Boolean
}

input CreateSubscriptionInput {
  tierId: ID!
  paymentMethodId: String
}

input UpdateSubscriptionInput {
  tierId: ID
  cancelAtPeriodEnd: Boolean
}

input ProcessPaymentInput {
  type: TransactionType!
  amount: Int!
  currency: String!
  paymentMethod: PaymentMethod!
  subscriptionId: ID
  tierId: ID
  minutesPurchased: Int
  minutePackageId: ID
}

input CreateContentViewInput {
  contentId: ID!
  duration: Int!
  completionPercentage: Int!
  completed: Boolean!
  episodeId: ID
  seasonNumber: Int
  episodeNumber: Int
  deviceType: DeviceType!
  platform: Platform!
  quality: PlaybackQuality!
}

# ============================================================================
# QUERIES
# ============================================================================

type Query {
  # User Queries
  getUser(userId: ID!): User @aws_auth(cognito_groups: ["Users"])
  getCurrentUser: User @aws_auth(cognito_groups: ["Users"])
  getUserByEmail(email: AWSEmail!): User @aws_auth(cognito_groups: ["Admins"])
  
  # Content Queries
  getContent(contentId: ID!): Content
  listContents(
    category: String
    contentType: ContentType
    status: ContentStatus
    featured: Boolean
    limit: Int
    nextToken: String
  ): ContentConnection
  getFeaturedContent: Content
  getContentCategories: [ContentCategory!]!
  searchContent(query: String!, limit: Int, nextToken: String): ContentConnection
  
  # Episode Queries
  getEpisode(episodeId: ID!): Episode
  listEpisodes(seriesId: ID!, limit: Int, nextToken: String): EpisodeConnection
  
  # Subscription Queries
  getSubscriptionTiers: [SubscriptionTier!]!
  getSubscriptionTier(tierId: ID!): SubscriptionTier
  getUserSubscription(userId: ID!): UserSubscription @aws_auth(cognito_groups: ["Users"])
  
  # Minute Package Queries
  getMinutePackages: [MinutePackage!]!
  
  # User Engagement Queries
  getUserWatchlist(userId: ID!, limit: Int, nextToken: String): UserWatchlistConnection @aws_auth(cognito_groups: ["Users"])
  getUserLikes(userId: ID!, limit: Int, nextToken: String): UserLikeConnection @aws_auth(cognito_groups: ["Users"])
  getRecentlyWatched(userId: ID!, limit: Int): [RecentlyWatched!]! @aws_auth(cognito_groups: ["Users"])
  getUserRatings(contentId: ID!, limit: Int, nextToken: String): UserRatingConnection
  
  # Submission Queries
  getSubmission(submissionId: ID!): Submission @aws_auth(cognito_groups: ["Users"])
  listUserSubmissions(userId: ID!, status: SubmissionStatus, limit: Int, nextToken: String): SubmissionConnection @aws_auth(cognito_groups: ["Users"])
  listPendingSubmissions(limit: Int, nextToken: String): SubmissionConnection @aws_auth(cognito_groups: ["Admins", "Moderators"])
  
  # Payment Queries
  getPaymentTransactions(userId: ID!, limit: Int, nextToken: String): PaymentTransactionConnection @aws_auth(cognito_groups: ["Users"])
  getPaymentTransaction(transactionId: ID!): PaymentTransaction @aws_auth(cognito_groups: ["Users"])
  
  # Reports & Reviews
  getContentReports(contentId: ID!, limit: Int, nextToken: String): ContentReportConnection @aws_auth(cognito_groups: ["Admins", "Moderators"])
  getPendingReports(limit: Int, nextToken: String): ContentReportConnection @aws_auth(cognito_groups: ["Admins", "Moderators"])
  getSubmissionReviews(submissionId: ID!): [ContentReview!]! @aws_auth(cognito_groups: ["Admins", "Moderators"])
}

# ============================================================================
# MUTATIONS
# ============================================================================

type Mutation {
  # User Mutations
  createUser(input: CreateUserInput!): User
  updateUser(userId: ID!, input: UpdateUserInput!): User @aws_auth(cognito_groups: ["Users"])
  deleteUser(userId: ID!): Boolean @aws_auth(cognito_groups: ["Admins"])
  
  # Content Mutations
  createContent(input: CreateContentInput!): Content @aws_auth(cognito_groups: ["Admins"])
  updateContent(contentId: ID!, input: UpdateContentInput!): Content @aws_auth(cognito_groups: ["Admins"])
  deleteContent(contentId: ID!): Boolean @aws_auth(cognito_groups: ["Admins"])
  
  # Episode Mutations
  createEpisode(input: CreateEpisodeInput!): Episode @aws_auth(cognito_groups: ["Admins"])
  updateEpisode(episodeId: ID!, input: UpdateEpisodeInput!): Episode @aws_auth(cognito_groups: ["Admins"])
  deleteEpisode(episodeId: ID!): Boolean @aws_auth(cognito_groups: ["Admins"])
  
  # Subscription Mutations
  createSubscription(input: CreateSubscriptionInput!): UserSubscription @aws_auth(cognito_groups: ["Users"])
  updateSubscription(subscriptionId: ID!, input: UpdateSubscriptionInput!): UserSubscription @aws_auth(cognito_groups: ["Users"])
  cancelSubscription(subscriptionId: ID!): UserSubscription @aws_auth(cognito_groups: ["Users"])
  
  # Payment Mutations
  processPayment(input: ProcessPaymentInput!): PaymentTransaction @aws_auth(cognito_groups: ["Users"])
  purchaseMinutes(minutePackageId: ID!): PaymentTransaction @aws_auth(cognito_groups: ["Users"])
  
  # User Engagement Mutations
  addToWatchlist(contentId: ID!): UserWatchlist @aws_auth(cognito_groups: ["Users"])
  removeFromWatchlist(contentId: ID!): Boolean @aws_auth(cognito_groups: ["Users"])
  toggleLike(contentId: ID!): UserLike @aws_auth(cognito_groups: ["Users"])
  createUserRating(input: CreateUserRatingInput!): UserRating @aws_auth(cognito_groups: ["Users"])
  updateUserRating(ratingId: ID!, input: UpdateUserRatingInput!): UserRating @aws_auth(cognito_groups: ["Users"])
  deleteUserRating(ratingId: ID!): Boolean @aws_auth(cognito_groups: ["Users"])
  updateWatchProgress(contentId: ID!, progress: Int!, episodeId: ID, seasonNumber: Int, episodeNumber: Int): RecentlyWatched @aws_auth(cognito_groups: ["Users"])
  
  # Submission Mutations
  createSubmission(input: CreateSubmissionInput!): Submission @aws_auth(cognito_groups: ["Users"])
  updateSubmission(submissionId: ID!, input: UpdateSubmissionInput!): Submission @aws_auth(cognito_groups: ["Users"])
  submitForReview(submissionId: ID!): Submission @aws_auth(cognito_groups: ["Users"])
  deleteSubmission(submissionId: ID!): Boolean @aws_auth(cognito_groups: ["Users"])
  
  # Moderation Mutations
  createContentReport(input: CreateContentReportInput!): ContentReport @aws_auth(cognito_groups: ["Users"])
  reviewSubmission(input: ReviewSubmissionInput!): ContentReview @aws_auth(cognito_groups: ["Admins", "Moderators"])
  updateContentReport(reportId: ID!, status: ReportStatus!, resolutionNotes: String): ContentReport @aws_auth(cognito_groups: ["Admins", "Moderators"])
  
  # Analytics Mutations
  createContentView(input: CreateContentViewInput!): ContentView @aws_auth(cognito_groups: ["Users"])
}

# ============================================================================
# SUBSCRIPTIONS (Real-time Updates)
# ============================================================================

type Subscription {
  # Content Subscriptions
  onContentCreated: Content @aws_subscribe(mutations: ["createContent"])
  onContentUpdate(contentId: ID!): Content @aws_subscribe(mutations: ["updateContent"])
  onContentDeleted(contentId: ID!): Boolean @aws_subscribe(mutations: ["deleteContent"])
  
  # Submission Subscriptions
  onSubmissionCreated: Submission @aws_subscribe(mutations: ["createSubmission"]) @aws_auth(cognito_groups: ["Users"])
  onSubmissionUpdate(submissionId: ID!): Submission @aws_subscribe(mutations: ["updateSubmission", "submitForReview"]) @aws_auth(cognito_groups: ["Users"])
  onSubmissionReviewed: ContentReview @aws_subscribe(mutations: ["reviewSubmission"]) @aws_auth(cognito_groups: ["Admins", "Moderators"])
  
  # User Subscriptions
  onUserUpdate: User @aws_subscribe(mutations: ["updateUser"]) @aws_auth(cognito_groups: ["Users"])
  onSubscriptionCreated: UserSubscription @aws_subscribe(mutations: ["createSubscription"]) @aws_auth(cognito_groups: ["Users"])
  onSubscriptionUpdate: UserSubscription @aws_subscribe(mutations: ["updateSubscription", "cancelSubscription"]) @aws_auth(cognito_groups: ["Users"])
  
  # Payment Subscriptions
  onPaymentProcessed: PaymentTransaction @aws_subscribe(mutations: ["processPayment", "purchaseMinutes"]) @aws_auth(cognito_groups: ["Users"])
  
  # Moderation Subscriptions
  onNewReport: ContentReport @aws_subscribe(mutations: ["createContentReport"]) @aws_auth(cognito_groups: ["Admins", "Moderators"])
  onReportUpdated: ContentReport @aws_subscribe(mutations: ["updateContentReport"]) @aws_auth(cognito_groups: ["Admins", "Moderators"])
  onPendingSubmission: Submission @aws_subscribe(mutations: ["submitForReview"]) @aws_auth(cognito_groups: ["Admins", "Moderators"])
}

# ============================================================================
# CONNECTION TYPES (For Pagination)
# ============================================================================

type ContentConnection {
  items: [Content!]!
  nextToken: String
}

type EpisodeConnection {
  items: [Episode!]!
  nextToken: String
}

type UserWatchlistConnection {
  items: [UserWatchlist!]!
  nextToken: String
}

type UserLikeConnection {
  items: [UserLike!]!
  nextToken: String
}

type UserRatingConnection {
  items: [UserRating!]!
  nextToken: String
}

type SubmissionConnection {
  items: [Submission!]!
  nextToken: String
}

type PaymentTransactionConnection {
  items: [PaymentTransaction!]!
  nextToken: String
}

type ContentReportConnection {
  items: [ContentReport!]!
  nextToken: String
}

# ============================================================================
# ADDITIONAL INPUT TYPES (Referenced in Mutations)
# ============================================================================

input CreateContentInput {
  title: String!
  description: String!
  synopsis: String!
  thumbnailUrl: AWSURL
  thumbnailColor: String!
  posterUrl: AWSURL
  contentType: ContentType!
  subType: String
  duration: Int!
  durationDisplay: String!
  releaseYear: Int!
  ageRating: String!
  category: String!
  genres: [String!]!
  tags: [String!]
  director: String
  creator: String
  host: String
  author: String
  cast: [String!]!
  productionCompany: String
  episodeCount: Int
  seasons: Int
  currentSeason: Int
  episodeDuration: Int
  totalEpisodes: Int
  assetId: String!
  manifestUrl: AWSURL
  videoUrl: AWSURL
  availableQualities: [PlaybackQuality!]!
  submittedBy: ID
  featured: Boolean!
  isOriginal: Boolean!
}

input UpdateContentInput {
  title: String
  description: String
  synopsis: String
  thumbnailUrl: AWSURL
  thumbnailColor: String
  posterUrl: AWSURL
  category: String
  genres: [String!]
  tags: [String!]
  director: String
  creator: String
  host: String
  author: String
  cast: [String!]
  episodeCount: Int
  seasons: Int
  currentSeason: Int
  episodeDuration: Int
  totalEpisodes: Int
  manifestUrl: AWSURL
  videoUrl: AWSURL
  availableQualities: [PlaybackQuality!]
  status: ContentStatus
  featured: Boolean
  reviewedBy: ID
  rejectionReason: String
}

input CreateEpisodeInput {
  seriesId: ID!
  title: String!
  description: String!
  episodeNumber: Int!
  seasonNumber: Int!
  duration: Int!
  thumbnailUrl: AWSURL
  assetId: String!
  airDate: AWSDate
}

input UpdateEpisodeInput {
  title: String
  description: String
  episodeNumber: Int
  seasonNumber: Int
  duration: Int
  thumbnailUrl: AWSURL
  assetId: String
  airDate: AWSDate
}

# ============================================================================
# END OF SCHEMA
# ============================================================================
```

---

## Setup Instructions

1. **Open AWS AppSync Console**
   - Navigate to your AppSync API

2. **Go to Schema**
   - Click on "Schema" in the left sidebar

3. **Edit Schema**
   - Click "Edit schema" button
   - Delete any existing schema

4. **Paste Schema**
   - Copy the entire GraphQL schema from the code block above (from `# ============================================================================` to `# ============================================================================`)
   - Paste it into the schema editor

5. **Save Schema**
   - Click "Save schema"

6. **Configure Data Sources**
   - After saving, configure your data sources (DynamoDB tables)
   - Map resolvers to queries/mutations
   - Set up authorization rules

## Authorization Setup

The schema uses `@aws_auth(cognito_groups: ["Users", "Admins", "Moderators"])` directives. Make sure to:

1. **Create Cognito User Groups:**
   - `Users` - Regular users
   - `Admins` - Administrators
   - `Moderators` - Content moderators

2. **Configure AppSync Authorization:**
   - Use AWS Cognito User Pools as the authorization type
   - Assign users to appropriate groups

## Notes

- **Scalars**: Uses AWS AppSync custom scalars (AWSDate, AWSDateTime, AWSURL, etc.)
- **Authorization**: Uses `@aws_auth` directive for field-level authorization
- **Subscriptions**: Uses `@aws_subscribe` for real-time updates
- **Pagination**: Connection types included for list queries with pagination support
- **Resolvers**: You'll need to create VTL/AppSync JS resolvers to connect to DynamoDB

---

*Last Updated: 2024*
*Schema Version: 1.0*
*AppSync Compatible: Yes*
