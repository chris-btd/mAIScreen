package com.btd.maiscreen.util

/**
 * Password strength validator that enforces security requirements
 * Requirements:
 * - Minimum 12 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one number
 * - At least one special character
 */
object PasswordValidator {

    private const val MIN_PASSWORD_LENGTH = 12
    private val COMMON_PASSWORDS = setOf(
        "password", "12345678", "123456789", "1234567890",
        "qwerty", "abc123", "password123", "admin",
        "letmein", "welcome", "monkey", "dragon"
    )

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val strength: PasswordStrength = PasswordStrength.WEAK
    )

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }

    /**
     * Validate password against security requirements
     */
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()

        // Check minimum length
        if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Password must be at least $MIN_PASSWORD_LENGTH characters long")
        }

        // Check for uppercase letter
        if (!password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }

        // Check for lowercase letter
        if (!password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }

        // Check for digit
        if (!password.any { it.isDigit() }) {
            errors.add("Password must contain at least one number")
        }

        // Check for special character
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Password must contain at least one special character (!@#$%^&*)")
        }

        // Check against common passwords
        if (COMMON_PASSWORDS.any { password.lowercase().contains(it) }) {
            errors.add("Password is too common. Please choose a more unique password")
        }

        // Calculate strength
        val strength = calculateStrength(password)

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            strength = strength
        )
    }

    /**
     * Calculate password strength
     */
    private fun calculateStrength(password: String): PasswordStrength {
        var score = 0

        // Length scoring
        when {
            password.length >= 16 -> score += 2
            password.length >= 12 -> score += 1
        }

        // Complexity scoring
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        // Diversity scoring
        val uniqueChars = password.toSet().size
        if (uniqueChars >= password.length * 0.7) score++

        return when (score) {
            in 0..2 -> PasswordStrength.WEAK
            3, 4 -> PasswordStrength.MEDIUM
            5, 6 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }

    /**
     * Get a user-friendly message for password strength
     */
    fun getStrengthMessage(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.WEAK -> "Weak password"
            PasswordStrength.MEDIUM -> "Medium strength password"
            PasswordStrength.STRONG -> "Strong password"
            PasswordStrength.VERY_STRONG -> "Very strong password"
        }
    }
}
