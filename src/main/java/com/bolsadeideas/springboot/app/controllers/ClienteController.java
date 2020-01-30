package com.bolsadeideas.springboot.app.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.service.IClienteService;
import com.bolsadeideas.springboot.app.models.service.IUploadFileService;
import com.bolsadeideas.springboot.app.util.paginator.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private IClienteService clienteService;

	@Autowired
	private IUploadFileService uploadFileService;
	
	@Autowired
	private MessageSource messageSource;

	@GetMapping(value = { "/listar", "/" })
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
			Authentication authentication, HttpServletRequest request, Locale locale) {
		// List<Cliente> clientes = clienteService.findAll();

		if (authentication != null) {
			log.info("El usuario " + authentication.getName() + " esta solicitando listar los clientes");
		}

		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		// if(authentication != null) {
		// log.info("El usuario " + auth.getName() + " esta solicitando listar los
		// clientes");
		// }

		// if(hasRole("ROLE_ADMIN")) {
		// log.info("El usuario " + authentication.getName() + " tiene acceso total");
		// }else {
		// if(authentication != null) {
		// log.info("El usuario " + authentication.getName() + " NO tiene acceso
		// total");
		// }else {
		// log.info("El invitado NO tiene acceso total");
		// }
		// }

		//SecurityContextHolderAwareRequestWrapper securityContext = new SecurityContextHolderAwareRequestWrapper(request,"");
		//if (securityContext.isUserInRole("ROLE_ADMIN")) {
		//	log.info("El usuario " + authentication.getName() + " tiene acceso total");
		//} else {
		//	if (authentication != null) {
		//		log.info("El usuario " + authentication.getName() + " NO tiene acceso total");
		//	} else {
		//		log.info("El invitado NO tiene acceso total");
		//	}
		//}
		
		if (request.isUserInRole("ROLE_ADMIN")) {
			log.info("El usuario " + authentication.getName() + " tiene acceso total");
		} else {
			if (authentication != null) {
				log.info("El usuario " + authentication.getName() + " NO tiene acceso total");
			} else {
				log.info("El invitado NO tiene acceso total");
			}
		}

		Pageable pageRequest = PageRequest.of(page, 5);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

		model.addAttribute("titulo", messageSource.getMessage("text.cliente.listar.titulo", null, locale));
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}
	
	@Secured("ROLE_USER")
	@GetMapping("/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {

		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/form")
	public String form(Model model) {
		Cliente cliente = new Cliente();
		model.addAttribute("titulo", "Formulario de cliente");
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/form")
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de cliente");
			return "form";
		}

		if (!foto.isEmpty()) {
			if (cliente.getId() != null && cliente.getId() > 0) {
				if (cliente.getFoto() != null && cliente.getFoto().length() > 0) {
					uploadFileService.delete(cliente.getFoto());
				}
			}

			String uniqueFilename = null;

			try {
				uniqueFilename = uploadFileService.copy(foto);
				flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");
				cliente.setFoto(uniqueFilename);
			} catch (IOException ioe) {
				flash.addFlashAttribute("error", ioe.getMessage());
			}
		}

		String mensajeFlash = (cliente.getId() != null) ? "Cliente creado con éxito!" : "Cliente editado con éxito!";

		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		Cliente cliente = null;
		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute("error", "El id del cliente no existe en base de datos");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El id del cliente no puede ser cero");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Formulario de cliente");
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);
			clienteService.delete(id);
			flash.addFlashAttribute("success", "Cliente eliminado con éxito!");

			if (uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto" + cliente.getFoto() + " eliminada con exito!");
			} else {
				flash.addFlashAttribute("warning", "La foto" + cliente.getFoto() + " no fue eliminada.");
			}
		}
		return "redirect:/listar";
	}

	@Secured("ROLE_USER")
	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes flash) {
		// Cliente cliente = clienteService.findOne(id);
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);

		if (cliente == null) {
			flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
			return "redirect:/listar";
		}

		model.addAttribute("titulo", "Detalle del cliente");
		model.addAttribute("cliente", cliente);

		return "ver";
	}

	private boolean hasRole(String rol) {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) {
			return false;
		}

		Authentication authentication = context.getAuthentication();

		if (authentication == null) {
			return false;
		}

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		//for (GrantedAuthority a : authorities) {
		//	if (rol.equals(a.getAuthority())) {
		//		return true;
		//	}
		//}

		return authorities.contains(new SimpleGrantedAuthority(rol));

	}

}
