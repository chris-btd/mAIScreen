# Required Lambda Environment Variables for CloudFront Signed Cookies

Your `play` Lambda function needs these environment variables:

## Required Variables:
```
CLOUDFRONT_DOMAIN=d2ofvyi6ramrc2.cloudfront.net
CLOUDFRONT_KEY_PAIR_ID=your-key-pair-id
CLOUDFRONT_PRIVATE_KEY=your-private-key-content
CLOUDFRONT_POLICY_DURATION=3600
```

## Optional Variables:
```
CLOUDFRONT_ACCESS_KEY_ID=your-access-key-id
CLOUDFRONT_SECRET_ACCESS_KEY=your-secret-access-key
```

## CloudFront Configuration Steps:

1. **Create a CloudFront Key Pair:**
   - Go to AWS CloudFront Console
   - Navigate to "Public keys" in the left sidebar
   - Click "Create public key"
   - Upload your public key or generate a new key pair

2. **Create a CloudFront Distribution:**
   - Go to CloudFront Console
   - Create a new distribution
   - Set the origin to your S3 bucket
   - Configure signed URLs/cookies in the "Behaviors" section

3. **Set up the Lambda Function:**
   - Add the environment variables listed above
   - Make sure the Lambda has permissions to access CloudFront
   - Test the function to ensure it generates valid signed cookies
