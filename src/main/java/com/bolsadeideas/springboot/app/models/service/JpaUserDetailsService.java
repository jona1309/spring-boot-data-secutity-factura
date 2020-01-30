package com.bolsadeideas.springboot.app.models.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bolsadeideas.springboot.app.models.dao.IUsuarioDao;
import com.bolsadeideas.springboot.app.models.entity.Role;
import com.bolsadeideas.springboot.app.models.entity.Usuario;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private IUsuarioDao usuarioDao;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioDao.findByUserName(username);
		
		if(usuario == null) {
			log.error("El usuario " + username + " no existe");
			throw new UsernameNotFoundException("El usuario " + username + ", no existe.");
		}
		
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (Role role : usuario.getRoles()) {
			log.info("Role: " + role.getRole());
			authorities.add(new SimpleGrantedAuthority(role.getRole()));
		}
		
		if(authorities.isEmpty()) {
			log.error("El usuario " + username + " no tiene roles asignados");
			throw new UsernameNotFoundException("El usuario " + username + ", no tiene roles asignados.");
		}
		
		return new User(username, usuario.getPassword(), usuario.getEnabled(), true, true, true, authorities);
	}

}
