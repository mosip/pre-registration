package io.mosip.kernel.pridgenerator.test.router;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.pridgenerator.constant.PRIDGeneratorConstant;
import io.mosip.kernel.pridgenerator.router.PridFetcherRouter;
import io.mosip.kernel.pridgenerator.service.PridService;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@RunWith(MockitoJUnitRunner.class)
public class PridFetcherRouterTest {

	@InjectMocks
	private PridFetcherRouter router;

	@Mock
	private PridService pridService;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(router, "workerExecutorPool", 2);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		ReflectionTestUtils.setField(router, "objectMapper", mapper);
	}

	@Test
	public void isFetchAuthorizedWhenSecretBlank() {
		ReflectionTestUtils.setField(router, "fetchSecret", "");
		RoutingContext ctx = mock(RoutingContext.class);
		assertTrue((Boolean) ReflectionTestUtils.invokeMethod(router, "isFetchAuthorized", ctx));
	}

	@Test
	public void isFetchAuthorizedRejectsMissingHeader() {
		ReflectionTestUtils.setField(router, "fetchSecret", "s3cret");
		RoutingContext ctx = mock(RoutingContext.class);
		HttpServerRequest request = mock(HttpServerRequest.class);
		when(ctx.request()).thenReturn(request);
		when(request.getHeader(PRIDGeneratorConstant.PRID_FETCH_SECRET_HEADER)).thenReturn(null);
		assertFalse((Boolean) ReflectionTestUtils.invokeMethod(router, "isFetchAuthorized", ctx));
	}

	@Test
	public void isFetchAuthorizedAcceptsMatchingHeader() {
		ReflectionTestUtils.setField(router, "fetchSecret", "s3cret");
		RoutingContext ctx = mock(RoutingContext.class);
		HttpServerRequest request = mock(HttpServerRequest.class);
		when(ctx.request()).thenReturn(request);
		when(request.getHeader(PRIDGeneratorConstant.PRID_FETCH_SECRET_HEADER)).thenReturn("s3cret");
		assertTrue((Boolean) ReflectionTestUtils.invokeMethod(router, "isFetchAuthorized", ctx));
	}

	@Test
	public void writeUnauthorizedSets401() {
		ReflectionTestUtils.setField(router, "fetchSecret", "s3cret");
		RoutingContext ctx = mock(RoutingContext.class);
		HttpServerResponse response = mock(HttpServerResponse.class);
		when(ctx.response()).thenReturn(response);
		when(response.setStatusCode(anyInt())).thenReturn(response);
		when(response.putHeader(anyString(), anyString())).thenReturn(response);

		ReflectionTestUtils.invokeMethod(router, "writeUnauthorized", ctx);

		verify(response, org.mockito.Mockito.atLeastOnce()).setStatusCode(401);
		verify(response).end(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	public void setErrorWritesEnvelopeAndFailsPromise() {
		RoutingContext ctx = mock(RoutingContext.class);
		HttpServerResponse response = mock(HttpServerResponse.class);
		when(ctx.response()).thenReturn(response);
		when(ctx.getBodyAsJson()).thenReturn(null);
		when(response.setStatusCode(anyInt())).thenReturn(response);

		io.vertx.core.Promise<Object> promise = io.vertx.core.Promise.promise();
		ReflectionTestUtils.invokeMethod(router, "setError", ctx, new ServiceError("KER-PRID-001", "none"), promise);

		verify(response).setStatusCode(200);
		assertTrue(promise.future().failed());
	}
}
