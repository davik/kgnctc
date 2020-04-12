package com.quickml.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.quickml.AuthUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	    	.authorizeRequests()
	          .antMatchers("/login", "/error", "/css/main.css", "/js/utility.js").permitAll()
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
	
	@Autowired
    AuthUserDetailsService userDetailsService ;
	
	@Autowired
    public void configAuthBuilder(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder()); 
    }
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
