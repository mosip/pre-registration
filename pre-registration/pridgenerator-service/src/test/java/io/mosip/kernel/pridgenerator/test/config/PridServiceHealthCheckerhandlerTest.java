package io.mosip.kernel.pridgenerator.test.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.pridgenerator.config.PridServiceHealthCheckerhandler;
import io.mosip.kernel.pridgenerator.constant.HibernatePersistenceConstant;
import io.mosip.kernel.pridgenerator.constant.PRIDGeneratorConstant;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;

@RunWith(VertxUnitRunner.class)
public class PridServiceHealthCheckerhandlerTest {

	private Vertx vertx;
	private PridServiceHealthCheckerhandler handler;

	@Before
	public void setUp() {
		vertx = Vertx.vertx();
		Environment environment = mock(Environment.class);
		when(environment.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_DRIVER))
				.thenReturn("org.h2.Driver");
		when(environment.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_URL))
				.thenReturn("jdbc:h2:mem:pridhealth;DB_CLOSE_DELAY=-1");
		when(environment.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_USER)).thenReturn("sa");
		when(environment.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_PASS)).thenReturn("");
		handler = new PridServiceHealthCheckerhandler(vertx, null, new ObjectMapper(), environment);
		handler.register("diskspace", handler::dispSpaceHealthChecker);
		handler.register("db", handler::databaseHealthChecker);
	}

	@After
	public void tearDown() {
		if (vertx != null) {
			vertx.close();
		}
	}

	@Test
	public void healthReturnsRegisteredChecks(TestContext context) {
		Async async = context.async();
		Router router = Router.router(vertx);
		router.get("/v1/pridgenerator" + PRIDGeneratorConstant.HEALTH_ENDPOINT).handler(handler);
		vertx.createHttpServer().requestHandler(router).listen(0, ar -> {
			context.assertTrue(ar.succeeded());
			int port = ar.result().actualPort();
			vertx.createHttpClient().getNow(port, "127.0.0.1", "/v1/pridgenerator/actuator/health",
					health -> health.bodyHandler(body -> {
						JsonObject json = body.toJsonObject();
						context.assertEquals("UP", json.getString("status"));
						context.assertTrue(json.getJsonObject("details").containsKey("db"));
						async.complete();
					}));
		});
	}
}
