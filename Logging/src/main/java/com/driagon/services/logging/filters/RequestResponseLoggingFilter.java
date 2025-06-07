package com.driagon.services.logging.filters;

import com.driagon.services.logging.utils.MaskedLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;

public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String COLON_STRING = ":";
    private static final String SPACE_STRING = " ";
    private static final String DASH = "-";

    private static final String REQUEST_TIMESTAMP_STRING_LABEL = "Request Timestamp:";
    private static final String REQUEST_CONTENT_LENGTH_STRING_LABEL = "Request Content Length:";
    private static final String RESPONSE_TIMESTAMP_STRING_LABEL = "Response Timestamp:";
    private static final String RESPONSE_TIME_STRING_LABEL = "Response Time:";
    private static final String POD_LABEL = "Host:";
    private static final String HOSTNAME_HEADER = "HOSTNAME";
    private static final String METHOD_LABEL = "Method:";
    private static final String OPERATION_LABEL = "Operation:";
    private static final String STATUS_CODE_LABEL = "Status Code:";
    private static final String NOT_PROVIDED = "Not Provided";

    private final Environment environment;
    private final Collection<String> excludePaths;
    private final Collection<String> requestHeaders;
    private final Collection<String> responseHeaders;

    private static final MaskedLogger log = MaskedLogger.getLogger(RequestResponseLoggingFilter.class);

    public RequestResponseLoggingFilter(Environment environment, Collection<String> excludePaths, Collection<String> requestHeaders, Collection<String> responseHeaders) {
        this.environment = environment;
        this.excludePaths = excludePaths;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
    }

    @Override
    protected boolean shouldNotFilter(final @NonNull HttpServletRequest request) {
        var path = request.getRequestURI();
        return !CollectionUtils.isEmpty(excludePaths) && excludePaths.stream().anyMatch(excludePath -> StringUtils.endsWithIgnoreCase(path, excludePath));
    }

    @Override
    protected void doFilterInternal(final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response, final @NonNull FilterChain filterChain) throws ServletException, IOException {
        var header = new StringBuilder();
        var start = Instant.now();
        header.append(REQUEST_TIMESTAMP_STRING_LABEL).append(start).append(SPACE_STRING);

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain, header);
            var end = Instant.now();
            header.append(RESPONSE_TIMESTAMP_STRING_LABEL).append(end).append(SPACE_STRING);
            header.append(RESPONSE_TIME_STRING_LABEL).append(ChronoUnit.MILLIS.between(start, end)).append(" ms");

            log.info(header.toString());
        }
    }

    private void doFilterWrapped(final ContentCachingRequestWrapper request, final ContentCachingResponseWrapper response, final FilterChain filterChain, final StringBuilder headerBuilder) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            afterRequest(request, response, headerBuilder);
            response.copyBodyToResponse();
        }
    }

    private void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, final StringBuilder headerBuilder) {
        logRequest(request, headerBuilder);
        logResponse(response, headerBuilder);
    }

    private void logRequest(ContentCachingRequestWrapper request, final StringBuilder headerBuilder) {
        headerBuilder.append(METHOD_LABEL).append(request.getMethod()).append(SPACE_STRING)
                .append(OPERATION_LABEL).append(request.getRequestURI()).append(SPACE_STRING);

        var queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString) && !queryString.equals("null") && !queryString.equals("undefined") && !queryString.equals("empty")) {
            headerBuilder.append(queryString).append(SPACE_STRING);
        }

        var contentLength = request.getContentLengthLong();
        headerBuilder.append(REQUEST_CONTENT_LENGTH_STRING_LABEL).append(contentLength == -1 ? NOT_PROVIDED : contentLength).append(SPACE_STRING)
                .append(POD_LABEL).append(environment.getProperty(HOSTNAME_HEADER)).append(SPACE_STRING);

        logHeaders(requestHeaders, request, headerBuilder);
    }

    private void logResponse(ContentCachingResponseWrapper response, final StringBuilder headerBuilder) {
        var status = response.getStatus();
        var statusDescription = HttpStatus.valueOf(status).getReasonPhrase();
        headerBuilder.append(STATUS_CODE_LABEL).append(status).append(SPACE_STRING).append(DASH).append(SPACE_STRING)
                .append(statusDescription).append(SPACE_STRING);

        logHeaders(responseHeaders, response, headerBuilder);
    }

    private void logHeaders(Collection<String> headers, HttpServletRequest request, StringBuilder headerBuilder) {
        if (!CollectionUtils.isEmpty(headers)) {
            for (var headerName : headers) {
                var headerValues = Collections.list(request.getHeaders(headerName));
                for (var headerValue : headerValues) {
                    headerBuilder.append(headerName).append(COLON_STRING).append(headerValue).append(SPACE_STRING);
                }
            }
        }
    }

    private void logHeaders(Collection<String> headers, HttpServletResponse response, StringBuilder headerBuilder) {
        if (!CollectionUtils.isEmpty(headers)) {
            for (var headerName : headers) {
                for (var headerValue : response.getHeaders(headerName)) {
                    headerBuilder.append(headerName).append(COLON_STRING).append(headerValue).append(SPACE_STRING);
                }
            }
        }
    }

    private ContentCachingRequestWrapper wrapRequest(final HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        }
        return new ContentCachingRequestWrapper(request);
    }

    private ContentCachingResponseWrapper wrapResponse(final HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        }
        return new ContentCachingResponseWrapper(response);
    }
}