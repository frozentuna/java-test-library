package com.chuross.testcase.http;

import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RequestPattern {

    private String path;
    private ListMultimap<String, Object> parameters;
    private ListMultimap<String, Object> requestHeaders;

    public RequestPattern(String path, ListMultimap<String, Object> parameters) {
        this(path, parameters, null);
    }

    public RequestPattern(String path, ListMultimap<String, Object> parameters, ListMultimap<String, Object> requestHeaders) {
        this.path = path;
        this.parameters = parameters;
        this.requestHeaders = requestHeaders;
    }

    public String getPath() {
        return path;
    }

    public ListMultimap<String, Object> getParameters() {
        return parameters;
    }

    public ListMultimap<String, Object> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof RequestPattern)) {
            return false;
        }
        RequestPattern that = (RequestPattern) obj;
        return new EqualsBuilder().append(path, that.getPath()).append(parameters, that.getParameters()).append(requestHeaders, that.getRequestHeaders()).build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(parameters).append(requestHeaders).build();
    }
}
