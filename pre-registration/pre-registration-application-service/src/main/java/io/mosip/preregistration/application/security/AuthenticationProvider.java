package io.mosip.preregistration.application.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProvider.class);

	private byte[] secret = TextCodec.BASE64.decode("Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=");

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String userName,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
		LOGGER.info("In retriveUser method of AuthenticationProvider class" + usernamePasswordAuthenticationToken);
		Object token = usernamePasswordAuthenticationToken.getCredentials();
		LOGGER.info("In retriveUser method of AuthenticationProvider class" + token);
		MosipUserDto mosipUserDto = new MosipUserDto();

		try {

			Jws<Claims> clamis = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.toString());
			mosipUserDto.setUserId(clamis.getBody().get("userId").toString());
			mosipUserDto.setName(clamis.getBody().get("user_name").toString());
			mosipUserDto.setToken(token.toString());
			mosipUserDto.setRole(clamis.getBody().get("roles").toString());
			LOGGER.info("extracted token details" + mosipUserDto);

		} catch (JwtException e) {
			LOGGER.error("exception while parsing the token" + e);
			throw new UsernameNotFoundException("Cannot find user with authentication token=" + token);
		}

		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, token.toString());
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList(mosipUserDto.getRole());
		authUserDetails.setAuthorities(grantedAuthorities);
		return authUserDetails;
	}
}
