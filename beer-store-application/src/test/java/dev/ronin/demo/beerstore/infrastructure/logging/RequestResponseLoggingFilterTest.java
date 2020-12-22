package dev.ronin.demo.beerstore.infrastructure.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.security.Principal;

import static dev.ronin.demo.beerstore.infrastructure.logging.RequestResponseLoggingFilter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingFilterTest {

    public static final String TEST_JSON = "{\"name\":\"Tester\",\"address\":\"1095 Budapest\"}";
    private ListAppender<ILoggingEvent> appender;
    private final Logger appLogger = (Logger) LoggerFactory.getLogger("com.khb.bbsh.core.infrastructure.rest.logging");

    @Mock
    private Principal principal;

    @Mock
    MockFilterChain filterChain;

    RequestResponseLoggingFilter loggingFilter;

    @BeforeEach
    public void setUp() {
        appender = new ListAppender<>();
        appender.start();
        appLogger.addAppender(appender);
        loggingFilter = new RequestResponseLoggingFilter();
    }

    @AfterEach
    public void tearDown() {
        appLogger.detachAppender(appender);
    }

    @Test
    @DisplayName("Test single GET without body")
    void testWithSimpleGETWithoutBody() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        request.setContentType(null);
        // when:
        loggingFilter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains(INCOMING_ARROW).contains("GET http://localhost");
        assertThat(appender.list.get(1).getFormattedMessage()).contains(OUTGOING_ARROW).contains("GET http://localhost")
                .contains(RETURNED_STATUS_TEXT);
    }

    @Test
    @DisplayName("Test single GET with query parameters")
    void testWithSimpleGETWithQueryParameters() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        request.setQueryString("param1=value1&param2=value2");
        // when:
        loggingFilter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains(INCOMING_ARROW).contains("GET http://localhost?param1=value1&param2=value2");
        assertThat(appender.list.get(1).getFormattedMessage()).contains(OUTGOING_ARROW).contains("GET http://localhost?param1=value1&param2=value2")
                .contains(RETURNED_STATUS_TEXT);
    }

    @Test
    @DisplayName("Test single GET with Principal")
    void testWithSimpleGETWithPrincipal() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        when(principal.getName()).thenReturn("username");
        request.setUserPrincipal(principal);
        // when:
        loggingFilter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains(INCOMING_ARROW).contains("GET http://localhost, principalName=username");
        assertThat(appender.list.get(1).getFormattedMessage()).contains(OUTGOING_ARROW).contains("GET http://localhost, principalName=username")
                .contains(RETURNED_STATUS_TEXT);
    }

    @Test
    @DisplayName("Test single GET with AuthType")
    void testWithSimpleGETWithAuthType() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        request.setAuthType("BASIC");
        // when:
        loggingFilter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains(INCOMING_ARROW)
                .contains("GET http://localhost").contains(AUTH_TYPE_TEXT + "BASIC");
    }

    @Test
    @DisplayName("Test single GET with RequestBody")
    void testWithSimpleGETWithRequestBody() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        request.setContent(TEST_JSON.getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(invocation -> {
            ContentCachingRequestWrapper req = invocation.getArgument(0);
            IOUtils.toString(req.getInputStream());
            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
        // when:
        loggingFilter.doFilterInternal(request, response, filterChain);
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
        assertThat(appender.list.get(0).getFormattedMessage()).contains(REQUEST_BODY_TEXT)
                .contains("\"name\" : \"Tester\",").contains("\"address\" : \"1095 Budapest\"");
    }

    @Test
    @DisplayName("Test single GET with Invalid Body")
    void testWithSimpleGETWithInvalidJsonBody() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        request.setContent("asdasfasgjsgkldsfjglsnvd".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(invocation -> {
            ContentCachingRequestWrapper req = invocation.getArgument(0);
            IOUtils.toString(req.getInputStream());
            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
        // when:
        loggingFilter.doFilterInternal(request, response, filterChain);
        //Then
        assertThat(appender.list.size()).isEqualTo(3);
        assertThat(appender.list.get(0).getFormattedMessage()).isEqualTo(CANNOT_PARSE_BODY_MESSAGE);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.WARN);
    }

    @Test
    @DisplayName("Test single GET with Empty Body")
    void testWithSimpleGETWithEmptyBody() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        request.setContent("".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(invocation -> {
            ContentCachingRequestWrapper req = invocation.getArgument(0);
            IOUtils.toString(req.getInputStream());
            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
        // when:
        loggingFilter.doFilterInternal(request, response, filterChain);
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Test single GET with response body")
    void testWithSimpleGETWithResponseBody() throws Exception {
        //Given
        MockHttpServletRequest request = getMockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        doAnswer(invocation -> {
            ContentCachingResponseWrapper resp = invocation.getArgument(1);
            resp.getWriter().write(TEST_JSON);
            IOUtils.toString(resp.getContentInputStream());
            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
        // when:
        loggingFilter.doFilterInternal(request, response, filterChain);
        //Then
        assertThat(appender.list.size()).isEqualTo(2);
        assertThat(appender.list.get(1).getFormattedMessage()).contains(RESPONSE_BODY_TEXT)
                .contains("\"name\" : \"Tester\",").contains("\"address\" : \"1095 Budapest\"");
    }

    private MockHttpServletRequest getMockHttpServletRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return request;
    }
}