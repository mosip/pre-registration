package io.mosip.kernel.pridgenerator.router;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.pridgenerator.constant.EventType;
import io.mosip.kernel.pridgenerator.constant.PRIDGeneratorConstant;
import io.mosip.kernel.pridgenerator.constant.PRIDGeneratorErrorCode;
import io.mosip.kernel.pridgenerator.dto.PridFetchResponseDto;
import io.mosip.kernel.pridgenerator.exception.PridGeneratorServiceException;
import io.mosip.kernel.pridgenerator.service.PridService;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Router for vertx server
 * 
 * @author Ajay J
 * @since 1.0.0
 *
 */
@Component
public class PridFetcherRouter {

	@Value("${mosip.kernel.prid.get_executor_pool:400}")
	private int workerExecutorPool;

	@Value("${mosip.kernel.prid.fetch.secret:}")
	private String fetchSecret;

	private static final Logger LOGGER = LoggerFactory.getLogger(PridFetcherRouter.class);

	@Autowired
	private PridService pridService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Creates router for vertx server
	 * 
	 * @param vertx vertx
	 * @return Router
	 */
	public Router createRouter(Vertx vertx) {
		LOGGER.info("worker executor pool {}", workerExecutorPool);
		Router router = Router.router(vertx);
		router.get().handler(routingContext -> {
			if (!isFetchAuthorized(routingContext)) {
				writeUnauthorized(routingContext);
				return;
			}
			LOGGER.info("publishing event to CHECKPOOL");
			// send a publish event to prid pool checker
			routingContext.response().headers().add("Content-Type", "application/json");
			vertx.eventBus().publish(EventType.CHECKPOOL, EventType.CHECKPOOL);
			ResponseWrapper<PridFetchResponseDto> reswrp = new ResponseWrapper<>();
			WorkerExecutor executor = vertx.createSharedWorkerExecutor("get-prid", workerExecutorPool);
			executor.executeBlocking(blockingCodeHandler -> {
				PridFetchResponseDto pridFetchResponseDto = null;
				try {
					pridFetchResponseDto = pridService.fetchPrid();
				} catch (PridGeneratorServiceException exception) {
					ServiceError error = new ServiceError(exception.getErrorCode(), exception.getMessage());
					setError(routingContext, error, blockingCodeHandler);
				}
				String timestamp = DateUtils2.getUTCCurrentDateTimeString();
				reswrp.setResponsetime(DateUtils2.convertUTCToLocalDateTime(timestamp));
				reswrp.setResponse(pridFetchResponseDto);
				reswrp.setErrors(null);
				blockingCodeHandler.complete();
			}, false, resultHandler -> {
				if (resultHandler.succeeded()) {
					try {
						routingContext.response().end(objectMapper.writeValueAsString(reswrp));
					} catch (JsonProcessingException exception) {
						ExceptionUtils.logRootCause(exception);
						ServiceError error = new ServiceError(
								PRIDGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(), exception.getMessage());
						setError(routingContext, error, null);
					}
				}
			});
		});

		return router;
	}

	private boolean isFetchAuthorized(RoutingContext routingContext) {
		if (fetchSecret == null || fetchSecret.isBlank()) {
			return true;
		}
		String provided = routingContext.request().getHeader(PRIDGeneratorConstant.PRID_FETCH_SECRET_HEADER);
		if (provided == null) {
			return false;
		}
		byte[] expected = fetchSecret.getBytes(StandardCharsets.UTF_8);
		byte[] actual = provided.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(expected, actual);
	}

	private void writeUnauthorized(RoutingContext routingContext) {
		ServiceError error = new ServiceError(PRIDGeneratorErrorCode.UNAUTHORIZED.getErrorCode(),
				PRIDGeneratorErrorCode.UNAUTHORIZED.getErrorMessage());
		ResponseWrapper<ServiceError> errorResponse = new ResponseWrapper<>();
		errorResponse.getErrors().add(error);
		try {
			routingContext.response().setStatusCode(401).putHeader("Content-Type", "application/json")
					.end(objectMapper.writeValueAsString(errorResponse));
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
			routingContext.response().setStatusCode(401).end();
		}
	}

	private void setError(RoutingContext routingContext, ServiceError error, Promise<Object> blockingCodeHandler) {
		ResponseWrapper<ServiceError> errorResponse = new ResponseWrapper<>();
		errorResponse.getErrors().add(error);
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode;
		if (routingContext.getBodyAsJson() != null) {
			try {
				reqNode = objectMapper.readTree(routingContext.getBodyAsJson().toString());
				errorResponse.setId(reqNode.path("id").asText());
				errorResponse.setVersion(reqNode.path("version").asText());
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
		try {
			routingContext.response().setStatusCode(200).end(objectMapper.writeValueAsString(errorResponse));
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}

		LOGGER.error(error.getMessage());
		if (blockingCodeHandler != null) {
			blockingCodeHandler.fail(error.getMessage());
		}
	}

}
