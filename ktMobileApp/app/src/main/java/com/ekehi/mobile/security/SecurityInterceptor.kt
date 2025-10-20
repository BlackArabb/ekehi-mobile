package com.ekehi.mobile.security

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * SecurityInterceptor adds security headers to HTTP requests and validates responses.
 * Implements OWASP secure coding practices for secure communication.
 */
class SecurityInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add security headers to the request
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("X-Content-Type-Options", "nosniff")
            .addHeader("X-Frame-Options", "DENY")
            .addHeader("X-XSS-Protection", "1; mode=block")
            .addHeader("Referrer-Policy", "no-referrer")
            .addHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
        
        // Add user agent if not present
        if (originalRequest.header("User-Agent") == null) {
            requestBuilder.addHeader("User-Agent", "Ekehi-Mobile-App/1.0")
        }
        
        val secureRequest = requestBuilder.build()
        val response = chain.proceed(secureRequest)
        
        // Validate response headers for security
        validateResponseHeaders(response)
        
        return response
    }
    
    /**
     * Validates response headers for security issues
     * @param response The HTTP response to validate
     */
    private fun validateResponseHeaders(response: Response) {
        // Check for content type sniffing protection
        val contentType = response.header("Content-Type")
        if (contentType != null && !contentType.contains("charset")) {
            // Log potential security issue
            SecurityLogger().logSecurityThreat(
                "Missing Charset",
                "Response missing charset in Content-Type header",
                ThreatSeverity.MEDIUM
            )
        }
        
        // Check for security headers in response
        val xssProtection = response.header("X-XSS-Protection")
        if (xssProtection == null || xssProtection != "1; mode=block") {
            // Log potential security issue
            SecurityLogger().logSecurityThreat(
                "Missing XSS Protection",
                "Response missing proper X-XSS-Protection header",
                ThreatSeverity.LOW
            )
        }
        
        // Check for content type options
        val contentTypeOptions = response.header("X-Content-Type-Options")
        if (contentTypeOptions == null || contentTypeOptions != "nosniff") {
            // Log potential security issue
            SecurityLogger().logSecurityThreat(
                "Missing Content Type Options",
                "Response missing proper X-Content-Type-Options header",
                ThreatSeverity.LOW
            )
        }
    }
}