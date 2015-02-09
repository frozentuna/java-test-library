package com.chuross.testcase.http;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.*;

public class HttpRequestTestCase {

    public static final String HOST = "localhost";
    public static int PORT = 3000;
    public static final String BASE_URL = String.format(Locale.JAPAN, "http://%s:%d", HOST, PORT);
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestTestCase.class);
    private Server server;
    private Map<RequestPattern, Response> responseMap = Maps.newHashMap();

    @Before
    public void beforeServerTest() throws Exception {
        InetSocketAddress socketAddress = new InetSocketAddress(HOST, PORT);
        server = new Server(socketAddress);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
                onHandle(target, baseRequest, request, response);
            }
        });
        if(server.isRunning()) {
            server.stop();
        }
        server.start();
    }

    @After
    public void afterServerTest() throws Exception {
        server.stop();
        server.destroy();
    }

    private void onHandle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        String method = request.getMethod();
        InputStream inputStream = request.getInputStream();
        try {
            ListMultimap<String, Object> parameters = getParameters(request);
            ListMultimap<String, Object> requestHeaders = getRequestHeader(request);
            RequestPattern pattern = new RequestPattern(target, parameters, requestHeaders);
            LOGGER.info("jetty:pattern-path:{}, pattern-parameter:{}, pattern-header:{}, path:{}, method:{}", pattern.getPath(), pattern.getParameters(), pattern.getRequestHeaders(), target, method);
            if(!responseMap.containsKey(pattern)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                baseRequest.setHandled(true);
                return;
            }
            Response responseValue = responseMap.get(pattern);
            setResponseHeadersIfNotNull(response, responseValue.getResponseHeaders());
            response.setStatus(responseValue.getStatus());
            response.setContentType(responseValue.getContentType().toString());
            response.setCharacterEncoding(Charset.defaultCharset().toString());
            response.getWriter().write(responseValue.getBody());
            baseRequest.setHandled(true);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private ListMultimap<String, Object> getParameters(HttpServletRequest request) {
        ListMultimap<String, Object> parameters = ArrayListMultimap.create();
        Enumeration<String> names = request.getParameterNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            parameters.putAll(name, Arrays.asList(request.getParameterValues(name)));
        }
        return parameters;
    }

    private ListMultimap<String, Object> getRequestHeader(HttpServletRequest request) {
        ListMultimap<String, Object> requestHeaders = ArrayListMultimap.create();
        Enumeration<String> names = request.getHeaderNames();
        List<String> registeredNames = getRegisteredHeaderNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            if(!registeredNames.contains(name)) {
                continue;
            }
            requestHeaders.putAll(name, getRequestHeaderValues(request, name));
        }
        return requestHeaders;
    }

    private List<String> getRequestHeaderValues(HttpServletRequest request, String name) {
        List<String> headerValues = Lists.newArrayList();
        Enumeration<String> values = request.getHeaders(name);
        while(values.hasMoreElements()) {
            headerValues.add(values.nextElement());
        }
        return headerValues;
    }

    private List<String> getRegisteredHeaderNames() {
        List<String> names = Lists.newArrayList();
        for(RequestPattern pattern : responseMap.keySet()) {
            for(String name : pattern.getParameters().keySet()) {
                names.add(name);
            }
        }
        return names;
    }

    private void setResponseHeadersIfNotNull(HttpServletResponse response, ListMultimap<String, Object> responseHeaders) {
        if(responseHeaders == null || responseHeaders.size() <= 0) {
            return;
        }
        for(Map.Entry<String, Collection<Object>> entry : responseHeaders.asMap().entrySet()) {
            String name = entry.getKey();
            for(Object value : entry.getValue()) {
                response.addHeader(name, value.toString());
            }
        }
    }

    public void putResponse(RequestPattern pattern, Response response) {
        if(pattern == null || response == null) {
            throw new IllegalArgumentException();
        }
        responseMap.put(pattern, response);
        LOGGER.info("expected:pattern-path:{}, pattern-parameter:{}, pattern-header:{}", pattern.getPath(), pattern.getParameters(), pattern.getRequestHeaders());
    }

    public String getUrl(String path) {
        return String.format(Locale.JAPAN, "%s/%s", BASE_URL, path.startsWith("/") ? path.substring(1) : path);
    }
}
