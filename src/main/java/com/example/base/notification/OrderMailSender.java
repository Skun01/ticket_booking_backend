package com.example.base.notification;

import com.example.base.config.property.MailProperties;
import com.example.base.dto.notification.PaidOrderMailData;
import com.example.base.dto.notification.TicketMailData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMailSender {

	private final JavaMailSender mailSender;
	private final MailProperties properties;

	public void send(PaidOrderMailData data) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
			helper.setFrom(properties.from());
			helper.setTo(data.recipient());
			helper.setSubject("Sport Slot - Vé đơn " + data.orderCode());
			helper.setText(body(data), false);
		}
		catch (MessagingException ex) {
			throw new MailPreparationException("Could not prepare order ticket email", ex);
		}
		mailSender.send(mimeMessage);
	}

	private String body(PaidOrderMailData data) {
		StringBuilder text = new StringBuilder();
		text.append("Xin chào ").append(data.fullName()).append(",\n\n");
		text.append("Đơn ").append(data.orderCode()).append(" đã thanh toán thành công.\n");
		text.append("Tổng tiền: ").append(data.totalAmount().toPlainString()).append("\n");
		text.append("Thời gian bên dưới theo UTC (Z).\n\n");
		for (TicketMailData ticket : data.tickets()) {
			text.append("Sự kiện: ").append(ticket.eventTitle()).append("\n");
			text.append("Hạng vé: ").append(ticket.ticketTypeName()).append("\n");
			text.append("Mã vé: ").append(ticket.ticketCode()).append("\n");
			text.append("Trạng thái vé: ").append(ticket.status()).append("\n");
			text.append("Thời gian: ").append(ticket.startTime()).append(" - ")
					.append(ticket.endTime()).append("\n");
			text.append("Địa điểm: ").append(ticket.venueName()).append(" - ")
					.append(ticket.address()).append(", ").append(ticket.city()).append("\n\n");
		}
		text.append("Vui lòng bảo mật mã vé và xuất trình khi check-in.\n");

		return text.toString();
	}

}
