package com.quickml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@SpringBootApplication
public class SpringBootWebApplication extends WebSecurityConfigurerAdapter {
	// secret123
	private static final String ENCODED_PASSWORD = "$2a$10$AIUufK8g6EFhBcumRRV2L.AQNz3Bjp7oDQVFiO5JJMBFZQ6x2/R/2";

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

	@Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	    	.authorizeRequests()
	          .antMatchers("/login", "/error").permitAll()
	          .antMatchers("/**").authenticated()
	          .and()
	        .formLogin()
	          .loginPage("/login")
	          .permitAll()
	          .and()
	        .logout()
	          .clearAuthentication(true)
	          .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	          .logoutSuccessUrl("/login")
	          .invalidateHttpSession(true)
	          .deleteCookies("JSESSIONID")
	          .permitAll();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
			.passwordEncoder(passwordEncoder())
			.withUser("avik")
			.password(ENCODED_PASSWORD)
			.roles("USER");
	}
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}