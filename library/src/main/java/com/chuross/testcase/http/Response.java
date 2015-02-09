package com.chuross.testcase.http;

import com.google.common.collect.ListMultimap;
import com.google.common.net.MediaType;

import java.nio.charset.Charset;

public class Response {

    private int status;
    private String body;
    private ListMultimap<String, Object> responseHeaders;
    private MediaType contentType;
    private Charset encoding;

    public Response(int status, String body, ListMultimap<String, Object> responseHeaders, MediaType contentType) {
        this(status, body, responseHeaders, contentType, Charset.defaultCharset());
    }

    public Response(int status, String body, ListMultimap<String, Object> responseHeaders, MediaType contentType, Charset encoding) {
        this.status = status;
        this.body = body;
        this.responseHeaders = responseHeaders;
        this.contentType = contentType;
        this.encoding = encoding;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public ListMultimap<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public Charset getEncoding() {
        return encoding;
    }
}