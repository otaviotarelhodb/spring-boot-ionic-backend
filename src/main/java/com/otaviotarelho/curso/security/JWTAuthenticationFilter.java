package com.otaviotarelho.curso.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otaviotarelho.curso.dto.CredenciaisDTO;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private static final String BEARER = "Bearer";

	private static final String AUTHORIZATION = "Authorization";

	private AuthenticationManager authenticationManager;
	
	private JWTUtil jwtUtil;
	 
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Override
	    public Authentication attemptAuthentication(HttpServletRequest req,
	                                                HttpServletResponse res) throws AuthenticationException {
			
	        try {
	        		CredenciaisDTO creds = new ObjectMapper()
	                    .readValue(req.getInputStream(), CredenciaisDTO.class);

	            return authenticationManager.authenticate(
	                    new UsernamePasswordAuthenticationToken(
	                            creds.getEmail(),
	                            creds.getSenha(),
	                            new ArrayList<>())
	            );
	            
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    @Override
	    protected void successfulAuthentication(HttpServletRequest req,
	                                            HttpServletResponse res,
	                                            FilterChain chain,
	                                            Authentication auth) throws IOException, ServletException {
	    		String username = ((UserSpringSecurity) auth.getPrincipal()).getUsername();
	    		String token = jwtUtil.generateToken(username).toString();
	        res.addHeader(AUTHORIZATION, BEARER + token);
	        res.addHeader("access-control-expose-headers", "Authorization");
	    }

}
