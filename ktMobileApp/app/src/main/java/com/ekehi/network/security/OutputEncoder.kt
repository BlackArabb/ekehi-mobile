package com.ekehi.network.security

import android.text.Html
import android.text.Spanned
import android.util.Base64
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * OutputEncoder handles encoding of data for safe output to prevent XSS and other injection attacks.
 * Implements OWASP secure coding practices for output encoding.
 */
object OutputEncoder {
    
    /**
     * Encodes HTML special characters to prevent XSS attacks
     * @param input The input string to encode
     * @return HTML encoded string
     */
    fun encodeForHtml(input: String): String {
        return Html.escapeHtml(input)
    }
    
    /**
     * Encodes HTML special characters and returns a Spanned object
     * @param input The input string to encode
     * @return HTML encoded Spanned object
     */
    fun encodeForHtmlSpanned(input: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(Html.escapeHtml(input), Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(Html.escapeHtml(input))
        }
    }
    
    /**
     * Encodes URL parameters to prevent URL injection
     * @param input The input string to encode
     * @return URL encoded string
     */
    fun encodeForUrl(input: String): String {
        return URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
    }
    
    /**
     * Encodes data for JavaScript contexts
     * @param input The input string to encode
     * @return JavaScript encoded string
     */
    fun encodeForJavaScript(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    /**
     * Encodes data for CSS contexts
     * @param input The input string to encode
     * @return CSS encoded string
     */
    fun encodeForCss(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
    }
    
    /**
     * Encodes binary data to Base64
     * @param input The input byte array to encode
     * @return Base64 encoded string
     */
    fun encodeForBase64(input: ByteArray): String {
        return Base64.encodeToString(input, Base64.DEFAULT)
    }
    
    /**
     * Decodes Base64 data
     * @param input The Base64 encoded string
     * @return Decoded byte array
     */
    fun decodeFromBase64(input: String): ByteArray {
        return Base64.decode(input, Base64.DEFAULT)
    }
    
    /**
     * Encodes data for XML contexts
     * @param input The input string to encode
     * @return XML encoded string
     */
    fun encodeForXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
    
    /**
     * Encodes data for JSON contexts
     * @param input The input string to encode
     * @return JSON encoded string
     */
    fun encodeForJson(input: String): String {
        return input.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("/", "\\/")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")  // Use Unicode escape for form feed
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    /**
     * Contextual encoding based on the output context
     * @param input The input string to encode
     * @param context The context where the output will be used
     * @return Appropriately encoded string
     */
    fun encodeForContext(input: String, context: OutputContext): String {
        return when (context) {
            OutputContext.HTML -> encodeForHtml(input)
            OutputContext.URL -> encodeForUrl(input)
            OutputContext.JAVASCRIPT -> encodeForJavaScript(input)
            OutputContext.CSS -> encodeForCss(input)
            OutputContext.XML -> encodeForXml(input)
            OutputContext.JSON -> encodeForJson(input)
        }
    }
}

/**
 * Enum representing different output contexts
 */
enum class OutputContext {
    HTML, URL, JAVASCRIPT, CSS, XML, JSON
}