package com.bolsadeideas.springboot.app.view.pdf;

import java.awt.Color;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.bolsadeideas.springboot.app.models.entity.Factura;
import com.bolsadeideas.springboot.app.models.entity.ItemFactura;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


@Component("/factura/ver")
public class FacturaPdfView extends AbstractPdfView{
	
	//@Autowired
	//private MessageSource messageSource;
	
	//@Autowired
	//private LocaleResolver localeResolver;

	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Factura factura = (Factura) model.get("factura");
		//Locale locale = localeResolver.resolveLocale(request);
		MessageSourceAccessor mesajes = this.getMessageSourceAccessor();
		
		PdfPTable tabla1 = new PdfPTable(1);
		
		//PdfPCell cellDatosCliente = new PdfPCell(new Phrase(messageSource.getMessage("text.factura.ver.datos.cliente", null, locale)));
		PdfPCell cellDatosCliente = new PdfPCell(new Phrase(mesajes.getMessage("text.factura.ver.datos.cliente")));
		cellDatosCliente.setBackgroundColor(new Color(184, 218, 255));
		cellDatosCliente.setPadding(8f);
		
		tabla1.addCell(cellDatosCliente);
		tabla1.addCell(factura.getCliente().getNombreCompleto());
		tabla1.addCell(factura.getCliente().getEmail());
		tabla1.setSpacingAfter(20);
		
		PdfPTable tabla2 = new PdfPTable(1);
		
		//PdfPCell cellDatosFactura = new PdfPCell(new Phrase(messageSource.getMessage("text.factura.ver.datos.factura", null, locale)));
		PdfPCell cellDatosFactura = new PdfPCell(new Phrase(mesajes.getMessage("text.factura.ver.datos.factura")));
		cellDatosFactura.setBackgroundColor(new Color(195, 230, 203));
		cellDatosFactura.setPadding(8f);
		
		tabla2.addCell(cellDatosFactura);
		tabla2.addCell(mesajes.getMessage("text.cliente.factura.folio") + ": " + factura.getId());
		tabla2.addCell(mesajes.getMessage("text.cliente.factura.descripcion") + ": " + factura.getDescripcion());
		tabla2.addCell(mesajes.getMessage("text.cliente.factura.fecha") + ": " + factura.getCreateAt());
		tabla2.setSpacingAfter(20);
		
		PdfPTable tabla3 = new PdfPTable(4);
		tabla3.setWidths(new float[] {3.5f, 1, 1, 1});
		
		tabla3.addCell(mesajes.getMessage("text.factura.form.item.nombre"));
		tabla3.addCell(mesajes.getMessage("text.factura.form.item.precio"));
		tabla3.addCell(mesajes.getMessage("text.factura.form.item.cantidad"));
		tabla3.addCell(mesajes.getMessage("text.factura.form.item.total"));
		for(ItemFactura item : factura.getItems()) {
			tabla3.addCell(item.getProducto().getNombre());
			tabla3.addCell(item.getProducto().getPrecio().toString());
			
			PdfPCell cellCantidadValue = new PdfPCell(new Phrase(item.getCantidad().toString()));
			cellCantidadValue.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			tabla3.addCell(cellCantidadValue);
			
			tabla3.addCell(item.calcularImporte().toString());
		}
		
		PdfPCell cellTotal = new PdfPCell(new Phrase("Total: "));
		cellTotal.setColspan(3);
		cellTotal.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		
		tabla3.addCell(cellTotal);
		tabla3.addCell(factura.getTotal().toString());
		
		tabla3.setSpacingAfter(20);
		
		document.add(tabla1);
		document.add(tabla2);
		document.add(tabla3);
	}

}
