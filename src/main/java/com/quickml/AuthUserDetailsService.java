package com.quickml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.quickml.pojos.AuthUserDetails;
import com.quickml.pojos.User;
import com.quickml.repository.UserRepository;

@Component
public class AuthUserDetailsService implements UserDetailsService {

	@Autowired
    private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		
        if(user == null){
            throw new UsernameNotFoundException(username);
        }else{
            UserDetails details = new AuthUserDetails(user);
            return details;
        }
	}

}
