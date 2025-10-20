package com.ekehi.mobile.security

import org.junit.Assert.*
import org.junit.Test

class OutputEncoderTest {
    
    @Test
    fun `encodeForHtml should escape HTML characters`() {
        assertEquals("Hello &lt;World&gt;", OutputEncoder.encodeForHtml("Hello <World>"))
        assertEquals("Test &amp; Example", OutputEncoder.encodeForHtml("Test & Example"))
        assertEquals("Quote &quot;Test&quot;", OutputEncoder.encodeForHtml("Quote \"Test\""))
        assertEquals("Apostrophe &#39;Test&#39;", OutputEncoder.encodeForHtml("Apostrophe 'Test'"))
    }
    
    @Test
    fun `encodeForUrl should encode URL parameters`() {
        assertEquals("Hello%20World", OutputEncoder.encodeForUrl("Hello World"))
        assertEquals("test%40example.com", OutputEncoder.encodeForUrl("test@example.com"))
        assertEquals("a%2Bb%3Dc", OutputEncoder.encodeForUrl("a+b=c"))
    }
    
    @Test
    fun `encodeForJavaScript should escape JavaScript characters`() {
        assertEquals("Hello \\\"World\\\"", OutputEncoder.encodeForJavaScript("Hello \"World\""))
        assertEquals("Test\\nNewLine", OutputEncoder.encodeForJavaScript("Test\nNewLine"))
        assertEquals("Test\\rCarriageReturn", OutputEncoder.encodeForJavaScript("Test\rCarriageReturn"))
        assertEquals("Test\\tTab", OutputEncoder.encodeForJavaScript("Test\tTab"))
        assertEquals("Test\\\\Backslash", OutputEncoder.encodeForJavaScript("Test\\Backslash"))
    }
    
    @Test
    fun `encodeForCss should escape CSS characters`() {
        assertEquals("Test\\\"Quote", OutputEncoder.encodeForCss("Test\"Quote"))
        assertEquals("Test\\'Apostrophe", OutputEncoder.encodeForCss("Test'Apostrophe"))
        assertEquals("Test\\\\Backslash", OutputEncoder.encodeForCss("Test\\Backslash"))
    }
    
    @Test
    fun `encodeForXml should escape XML characters`() {
        assertEquals("Hello &lt;World&gt;", OutputEncoder.encodeForXml("Hello <World>"))
        assertEquals("Test &amp; Example", OutputEncoder.encodeForXml("Test & Example"))
        assertEquals("Quote &quot;Test&quot;", OutputEncoder.encodeForXml("Quote \"Test\""))
        assertEquals("Apostrophe &apos;Test&apos;", OutputEncoder.encodeForXml("Apostrophe 'Test'"))
    }
    
    @Test
    fun `encodeForJson should escape JSON characters`() {
        assertEquals("Hello \\\"World\\\"", OutputEncoder.encodeForJson("Hello \"World\""))
        assertEquals("Test\\/Path", OutputEncoder.encodeForJson("Test/Path"))
        assertEquals("Test\\\\Backslash", OutputEncoder.encodeForJson("Test\\Backslash"))
        assertEquals("Test\\bBackspace", OutputEncoder.encodeForJson("Test\bBackspace"))
        assertEquals("Test\\fFormFeed", OutputEncoder.encodeForJson("Test\fFormFeed"))
        assertEquals("Test\\nNewLine", OutputEncoder.encodeForJson("Test\nNewLine"))
        assertEquals("Test\\rCarriageReturn", OutputEncoder.encodeForJson("Test\rCarriageReturn"))
        assertEquals("Test\\tTab", OutputEncoder.encodeForJson("Test\tTab"))
    }
    
    @Test
    fun `encodeForContext should encode based on context`() {
        assertEquals("Hello &lt;World&gt;", OutputEncoder.encodeForContext("Hello <World>", OutputContext.HTML))
        assertEquals("Hello%20World", OutputEncoder.encodeForContext("Hello World", OutputContext.URL))
        assertEquals("Hello \\\"World\\\"", OutputEncoder.encodeForContext("Hello \"World\"", OutputContext.JAVASCRIPT))
        assertEquals("Hello &lt;World&gt;", OutputEncoder.encodeForContext("Hello <World>", OutputContext.XML))
    }
    
    @Test
    fun `encodeForBase64 and decodeFromBase64 should work correctly`() {
        val original = "Hello World".toByteArray()
        val encoded = OutputEncoder.encodeForBase64(original)
        val decoded = OutputEncoder.decodeFromBase64(encoded)
        
        assertNotNull(encoded)
        assertFalse(encoded.isEmpty())
        assertArrayEquals(original, decoded)
    }
}