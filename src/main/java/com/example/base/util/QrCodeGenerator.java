package com.example.base.util;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.example.base.exception.AppException;
import com.example.base.exception.PdfErrorCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QrCodeGenerator {

	public BufferedImage generate(String ticketCode) {
		try {
			Map<EncodeHintType, Object> hints = Map.of(
					EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name(),
					EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
					EncodeHintType.MARGIN, 4);

			return MatrixToImageWriter.toBufferedImage(
					new QRCodeWriter().encode(ticketCode, BarcodeFormat.QR_CODE, 240, 240, hints));
		}
		catch (WriterException ex) {
			log.error("Could not generate ticket QR code", ex);
			throw new AppException(PdfErrorCode.QR_GENERATION_FAILED);
		}
	}

}
