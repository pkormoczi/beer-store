package dev.ronin.demo.beerstore.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j(topic = "dev.ronin.demo.beerstore.infrastructure.logging")
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    public static final String RESPONSE_BODY_TEXT = "\nResponse body:\n";
    public static final String REQUEST_BODY_TEXT = "\nRequest body:\n";
    public static final String INCOMING_ARROW = "==> ";
    public static final String OUTGOING_ARROW = "<== ";
    public static final String AUTH_TYPE_TEXT = ", authType=";
    public static final String PRINCIPAL_NAME_TEXT = ", principalName=";
    public static final String RETURNED_STATUS_TEXT = ": returned status=";
    public static final String CANNOT_PARSE_BODY_MESSAGE = "Cannot parse body!";
    private static final int MAX_PAYLOAD_LENGTH = 50000;

    private String contentAsString(byte[] content) {
        if (content != null && content.length != 0) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String valueAsString = objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(objectMapper.readValue(content, Object.class));
                return valueAsString
                        .substring(0, Math.min(valueAsString.length(), MAX_PAYLOAD_LENGTH));
            } catch (Exception ex) {
                log.warn(CANNOT_PARSE_BODY_MESSAGE, ex);
            }
        }
        return "";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        StringBuilder reqInfo = readRequestInfo(request, startTime);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(wrappedRequest, wrappedResponse);     // ======== This performs the actual request!

        long duration = System.currentTimeMillis() - startTime;
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s%s%s",
                    INCOMING_ARROW,
                    reqInfo,
                    bodyAsFormattedJson(wrappedRequest.getContentType(), wrappedRequest.getContentAsByteArray(), REQUEST_BODY_TEXT)));
            log.debug(String.format("%s%s%s%d in %dms %s",
                    OUTGOING_ARROW,
                    reqInfo,
                    RETURNED_STATUS_TEXT,
                    response.getStatus(),
                    duration,
                    bodyAsFormattedJson(wrappedResponse.getContentType(), wrappedResponse.getContentAsByteArray(), RESPONSE_BODY_TEXT)));
        }
        wrappedResponse.copyBodyToResponse();
    }

    private String bodyAsFormattedJson(String contentType, byte[] content, String label) {
        String body = "";
        if (contentTypeIsJson(contentType)) {
            String requestBody = contentAsString(content);
            if (!requestBody.isEmpty()) {
                body = label + requestBody;
            }
        }
        return body;
    }

    private StringBuilder readRequestInfo(HttpServletRequest request, long startTime) {
        StringBuilder reqInfo = new StringBuilder()
                .append("[")
                .append(startTime % 10000)  // request ID
                .append("] ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL());

        String queryString = request.getQueryString();
        if (queryString != null) {
            reqInfo.append("?").append(queryString);
        }

        if (request.getAuthType() != null) {
            reqInfo.append(AUTH_TYPE_TEXT)
                    .append(request.getAuthType());
        }
        if (request.getUserPrincipal() != null) {
            reqInfo.append(PRINCIPAL_NAME_TEXT)
                    .append(request.getUserPrincipal().getName());
        }
        return reqInfo;
    }

    private boolean contentTypeIsJson(String contentType) {
        return contentType != null && !contentType.isEmpty() && MediaType.valueOf(contentType).includes(MediaType.APPLICATION_JSON);
    }
}