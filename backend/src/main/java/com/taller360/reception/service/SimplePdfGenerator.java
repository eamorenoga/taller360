package com.taller360.reception.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SimplePdfGenerator {
  public byte[] generate(String title, List<String> lines) {
    StringBuilder text = new StringBuilder();
    text.append("BT /F1 16 Tf 50 780 Td (").append(escape(title)).append(") Tj ");
    text.append("/F1 10 Tf 0 -24 Td ");
    for (String line : lines) {
      text.append("(").append(escape(line)).append(") Tj 0 -15 Td ");
    }
    text.append("ET");
    byte[] stream = text.toString().getBytes(StandardCharsets.UTF_8);
    List<String> objects = new ArrayList<>();
    objects.add("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
    objects.add("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
    objects.add("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
    objects.add("4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");
    objects.add("5 0 obj << /Length " + stream.length + " >> stream\n" + new String(stream, StandardCharsets.UTF_8) + "\nendstream endobj\n");
    StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
    List<Integer> offsets = new ArrayList<>();
    for (String object : objects) {
      offsets.add(pdf.length());
      pdf.append(object);
    }
    int xref = pdf.length();
    pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
    pdf.append("0000000000 65535 f \n");
    for (Integer offset : offsets) {
      pdf.append(String.format("%010d 00000 n \n", offset));
    }
    pdf.append("trailer << /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
    pdf.append("startxref\n").append(xref).append("\n%%EOF");
    return pdf.toString().getBytes(StandardCharsets.UTF_8);
  }

  private String escape(String value) {
    return value == null ? "" : value.replaceAll("[^\\x00-\\x7F]", "?").replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
  }
}
