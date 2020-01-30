package com.bolsadeideas.springboot.app.util.paginator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

public class PageRender<T> {

	private String url;
	private Page<T> page;

	private int totalPaginas;
	private int numeroElementosPorPagina;
	private int paginaActual;

	private List<PageItem> paginas;

	public PageRender(String url, Page<T> page) {
		this.url = url;
		this.page = page;
		this.numeroElementosPorPagina = page.getSize();
		this.totalPaginas = page.getTotalPages();
		this.paginaActual = page.getNumber() + 1;

		int desde, hasta;
		if (this.totalPaginas <= this.numeroElementosPorPagina) {
			desde = 1;
			hasta = this.totalPaginas;
		} else {
			if (this.paginaActual <= this.numeroElementosPorPagina / 2) {
				desde = 1;
				hasta = this.numeroElementosPorPagina;
			} else if (this.paginaActual >= this.totalPaginas - this.numeroElementosPorPagina / 2) {
				desde = this.totalPaginas - this.numeroElementosPorPagina + 1;
				hasta = this.numeroElementosPorPagina;
			} else {
				desde = this.paginaActual - this.numeroElementosPorPagina / 2;
				hasta = this.numeroElementosPorPagina;
			}
		}

		this.paginas = new ArrayList<>();

		for (int i = 0; i < hasta; i++) {
			paginas.add(new PageItem(desde + i, this.paginaActual == desde + i));
		}
	}

	public String getUrl() {
		return url;
	}

	public int getTotalPaginas() {
		return totalPaginas;
	}

	public int getPaginaActual() {
		return paginaActual;
	}

	public List<PageItem> getPaginas() {
		return paginas;
	}

	public boolean isFirst() {
		return this.page.isFirst();
	}

	public boolean isLast() {
		return this.page.isLast();
	}

	public boolean isHasNext() {
		return this.page.hasNext();
	}

	public boolean isHasPrevious() {
		return page.hasPrevious();
	}

}
