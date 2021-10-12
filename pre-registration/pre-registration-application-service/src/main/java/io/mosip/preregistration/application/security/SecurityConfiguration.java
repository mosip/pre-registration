package io.mosip.preregistration.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import io.mosip.preregistration.application.config.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Value("${mosip.security.csrf-enable:false}")
	private boolean isCSRFEnable;
	
	@Value("${mosip.security.cors-enable:false}")
	private boolean isCORSEnable;

	@Value("${mosip.security.origins:localhost:8080}")
	private String origins;
	
	private static final RequestMatcher PROTECTED_URLS = new OrRequestMatcher(
			new AntPathRequestMatcher("/applications/**"), new AntPathRequestMatcher("/documents/**"), new AntPathRequestMatcher("/internal/**"),
			new AntPathRequestMatcher("/qrCode/**"), new AntPathRequestMatcher("/notification/**"),
			new AntPathRequestMatcher("/transliteration/**"), new AntPathRequestMatcher("/uispec/**"),
			new AntPathRequestMatcher("/logAudit"), new AntPathRequestMatcher("/login/refreshconfig/"),new AntPathRequestMatcher("/appointment/**"));

	AuthenticationProvider provider;

	public SecurityConfiguration(final AuthenticationProvider authenticationProvider) {
		super();
		this.provider = authenticationProvider;
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(provider);
	}

	@Override
	public void configure(final WebSecurity webSecurity) {
		webSecurity.ignoring().antMatchers("/**/assets/**", "/**/icons/**", "/**/screenshots/**", "/favicon**",
				"/**/favicon**", "/**/css/**", "/**/js/**", "/**/error**", "/**/webjars/**", "/**/v2/api-docs",
				"/**/configuration/ui", "/**/configuration/security", "/**/swagger-resources/**", "/**/swagger-ui.html",
				"/**/csrf", "/*/", "**/authenticate/**", "/**/actuator/**", "/**/authmanager/**", "/sendOtp",
				"/validateOtp", "/invalidateToken", "/config", "/login", "/logout", "/validateOTP", "/sendOTP",
				"/**/login", "/**/login/**", "/**/login-redirect/**", "/**/logout", "/**/h2-console/**",
				"/**/**/license/**", "/**/callback/**", "/**/authenticate/**");
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		if (!isCSRFEnable) {
			http = http.csrf().disable();
		}
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().exceptionHandling().and()
				.authenticationProvider(provider)
				.addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class).authorizeRequests()
				.requestMatchers(PROTECTED_URLS).authenticated().and().formLogin().disable()
				.httpBasic().disable().logout().disable();
		if (isCORSEnable) {
			http.addFilterBefore(new CorsFilter(origins), AuthenticationFilter.class);
		}
	}

	@Bean
	AuthenticationFilter authenticationFilter() throws Exception {
		final AuthenticationFilter filter = new AuthenticationFilter(PROTECTED_URLS);
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}

	@Bean
	AuthenticationEntryPoint forbiddenEntryPoint() {
		return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
	}
}