package com.example.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.base.dto.notification.TicketMailData;
import com.example.base.dto.report.DailyRevenueResponse;
import com.example.base.dto.report.EventReportResponse;
import com.example.base.exception.AppException;
import com.example.base.exception.PdfErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JasperPdfRenderer {

	private static final DateTimeFormatter UTC_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm 'UTC'")
			.withZone(ZoneOffset.UTC);

	private final QrCodeGenerator qrCodeGenerator;
	private final JasperReport ticketReport;
	private final JasperReport revenueReport;

	public JasperPdfRenderer(QrCodeGenerator qrCodeGenerator) {
		this.qrCodeGenerator = qrCodeGenerator;
		// Template cố định được compile một lần; mỗi lần fill vẫn dùng dữ liệu và tham số riêng.
		this.ticketReport = compile("reports/ticket.jrxml");
		this.revenueReport = compile("reports/revenue.jrxml");
	}

	public byte[] ticket(TicketMailData ticket) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("EVENT_TITLE", ticket.eventTitle());
		parameters.put("TICKET_CODE", ticket.ticketCode());
		parameters.put("TICKET_TYPE", ticket.ticketTypeName());
		parameters.put("STATUS", ticket.status().name());
		parameters.put("START_TIME", UTC_TIME.format(ticket.startTime()));
		parameters.put("END_TIME", UTC_TIME.format(ticket.endTime()));
		parameters.put("VENUE", ticket.venueName() + " - " + ticket.address() + ", " + ticket.city());
		parameters.put("QR_CODE", qrCodeGenerator.generate(ticket.ticketCode()));

		return render(ticketReport, parameters, new JREmptyDataSource(1));
	}

	public byte[] revenue(EventReportResponse report) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("EVENT_ID", report.eventId());
		parameters.put("EVENT_TITLE", report.eventTitle());
		parameters.put("TICKETS_SOLD", report.ticketsSold());
		List<Map<String, ?>> rows = new ArrayList<>();
		// Record không có JavaBean getter, dùng map để Jasper đọc field không phụ thuộc reflection của BeanUtils.
		for (DailyRevenueResponse day : report.dailyRevenue()) {
			rows.add(Map.of("date", day.date().toString(), "paidOrders", day.paidOrders(), "revenue", day.revenue()));
		}

		return render(revenueReport, parameters, new JRMapCollectionDataSource(rows));
	}

	private static JasperReport compile(String resource) {
		try (InputStream input = new ClassPathResource(resource).getInputStream()) {
			return JasperCompileManager.compileReport(input);
		}
		catch (IOException | JRException ex) {
			log.error("Could not compile PDF template: {}", resource, ex);
			throw new AppException(PdfErrorCode.PDF_GENERATION_FAILED);
		}
	}

	private byte[] render(JasperReport report, Map<String, Object> parameters, JRDataSource dataSource) {
		parameters.put(JRParameter.REPORT_LOCALE, Locale.forLanguageTag("vi-VN"));
		try {
			return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(report, parameters, dataSource));
		}
		catch (JRException ex) {
			log.error("Could not render PDF template: {}", report.getName(), ex);
			throw new AppException(PdfErrorCode.PDF_GENERATION_FAILED);
		}
	}

}
