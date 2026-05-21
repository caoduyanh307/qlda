package com.ddangkhoa.trafficfx.util;

import com.ddangkhoa.trafficfx.model.DataRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DataImportExportUtil {
    private DataImportExportUtil() {}

    public static List<DataRecord> readRecords(Path file, List<String> columns, Map<String, String> columnTitles) throws IOException {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".csv")) {
            return readCsv(file, columns, columnTitles);
        }
        if (name.endsWith(".xlsx")) {
            return readXlsx(file, columns, columnTitles);
        }
        throw new IOException("Chỉ hỗ trợ file .csv hoặc .xlsx");
    }

    public static void writeRecords(Path file, List<String> columns, Map<String, String> columnTitles, List<DataRecord> records) throws IOException {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".csv")) {
            writeCsv(file, columns, columnTitles, records);
            return;
        }
        if (name.endsWith(".xlsx")) {
            writeXlsx(file, columns, columnTitles, records);
            return;
        }
        throw new IOException("Chỉ hỗ trợ xuất .csv hoặc .xlsx");
    }

    private static List<DataRecord> readCsv(Path file, List<String> columns, Map<String, String> columnTitles) throws IOException {
        List<DataRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return records;
            }
            List<String> headers = parseCsvLine(stripBom(headerLine));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> cells = parseCsvLine(line);
                records.add(recordFromRow(headers, cells, columns, columnTitles));
            }
        }
        return records;
    }

    private static void writeCsv(Path file, List<String> columns, Map<String, String> columnTitles, List<DataRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write(toCsvLine(columns.stream().map(c -> columnTitles.getOrDefault(c, c)).toList()));
            writer.newLine();
            for (DataRecord record : records) {
                writer.write(toCsvLine(columns.stream().map(record::get).toList()));
                writer.newLine();
            }
        }
    }

    private static List<DataRecord> readXlsx(Path file, List<String> columns, Map<String, String> columnTitles) throws IOException {
        List<String> sharedStrings = new ArrayList<>();
        byte[] sheetData = null;
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("xl/sharedStrings.xml".equals(entry.getName())) {
                    sharedStrings = parseSharedStrings(zip.readAllBytes());
                } else if ("xl/worksheets/sheet1.xml".equals(entry.getName())) {
                    sheetData = zip.readAllBytes();
                }
            }
        }
        if (sheetData == null) {
            throw new IOException("Không tìm thấy sheet1 trong file Excel.");
        }
        List<List<String>> rows = parseSheetRows(sheetData, sharedStrings);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<String> headers = rows.get(0);
        List<DataRecord> records = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            List<String> cells = rows.get(i);
            if (cells.stream().allMatch(String::isBlank)) continue;
            records.add(recordFromRow(headers, cells, columns, columnTitles));
        }
        return records;
    }

    private static void writeXlsx(Path file, List<String> columns, Map<String, String> columnTitles, List<DataRecord> records) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            put(zip, "[Content_Types].xml", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
                    <Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">
                      <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>
                      <Default Extension=\"xml\" ContentType=\"application/xml\"/>
                      <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>
                      <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>
                    </Types>
                    """);
            put(zip, "_rels/.rels", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
                    <Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">
                      <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>
                    </Relationships>
                    """);
            put(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
                    <Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">
                      <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>
                    </Relationships>
                    """);
            put(zip, "xl/workbook.xml", """
                    <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
                    <workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">
                      <sheets><sheet name=\"Data\" sheetId=\"1\" r:id=\"rId1\"/></sheets>
                    </workbook>
                    """);
            put(zip, "xl/worksheets/sheet1.xml", buildSheetXml(columns, columnTitles, records));
        }
    }

    private static String buildSheetXml(List<String> columns, Map<String, String> columnTitles, List<DataRecord> records) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        xml.append(rowXml(1, columns.stream().map(c -> columnTitles.getOrDefault(c, c)).toList()));
        int rowIndex = 2;
        for (DataRecord record : records) {
            xml.append(rowXml(rowIndex++, columns.stream().map(record::get).toList()));
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private static String rowXml(int rowIndex, List<String> values) {
        StringBuilder row = new StringBuilder("<row r=\"").append(rowIndex).append("\">");
        for (int i = 0; i < values.size(); i++) {
            row.append("<c r=\"").append(columnName(i + 1)).append(rowIndex).append("\" t=\"inlineStr\"><is><t>")
                    .append(escapeXml(values.get(i))).append("</t></is></c>");
        }
        row.append("</row>");
        return row.toString();
    }

    private static List<String> parseSharedStrings(byte[] data) throws IOException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.ByteArrayInputStream(data));
            NodeList items = doc.getElementsByTagName("si");
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                NodeList texts = item.getElementsByTagName("t");
                StringBuilder text = new StringBuilder();
                for (int j = 0; j < texts.getLength(); j++) {
                    text.append(texts.item(j).getTextContent());
                }
                strings.add(text.toString());
            }
            return strings;
        } catch (Exception exception) {
            throw new IOException("Không đọc được shared strings của Excel.", exception);
        }
    }

    private static List<List<String>> parseSheetRows(byte[] data, List<String> sharedStrings) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(data));
            NodeList rowNodes = doc.getElementsByTagName("row");
            List<List<String>> rows = new ArrayList<>();
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element rowElement = (Element) rowNodes.item(i);
                NodeList cellNodes = rowElement.getElementsByTagName("c");
                List<String> row = new ArrayList<>();
                for (int j = 0; j < cellNodes.getLength(); j++) {
                    Element cell = (Element) cellNodes.item(j);
                    int index = columnIndex(cell.getAttribute("r"));
                    while (row.size() < index) row.add("");
                    row.set(index - 1, readCell(cell, sharedStrings));
                }
                rows.add(row);
            }
            return rows;
        } catch (Exception exception) {
            throw new IOException("Không đọc được sheet Excel.", exception);
        }
    }

    private static String readCell(Element cell, List<String> sharedStrings) {
        String type = cell.getAttribute("t");
        if ("s".equals(type)) {
            NodeList values = cell.getElementsByTagName("v");
            if (values.getLength() == 0) return "";
            int index = Integer.parseInt(values.item(0).getTextContent());
            return index >= 0 && index < sharedStrings.size() ? sharedStrings.get(index) : "";
        }
        if ("inlineStr".equals(type)) {
            NodeList texts = cell.getElementsByTagName("t");
            return texts.getLength() == 0 ? "" : texts.item(0).getTextContent();
        }
        NodeList values = cell.getElementsByTagName("v");
        return values.getLength() == 0 ? "" : values.item(0).getTextContent();
    }

    private static DataRecord recordFromRow(List<String> headers, List<String> cells, List<String> columns, Map<String, String> columnTitles) {
        DataRecord record = new DataRecord();
        Map<String, Integer> headerIndex = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndex.put(normalize(headers.get(i)), i);
        }
        for (String column : columns) {
            String title = columnTitles.getOrDefault(column, column);
            Integer index = headerIndex.get(normalize(title));
            if (index == null) index = headerIndex.get(normalize(column));
            record.put(column, index != null && index < cells.size() ? cells.get(index).trim() : "");
        }
        return record;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quote = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quote = !quote;
                }
            } else if (ch == ',' && !quote) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString());
        return result;
    }

    private static String toCsvLine(List<String> values) {
        return values.stream().map(DataImportExportUtil::escapeCsv).reduce((a, b) -> a + "," + b).orElse("");
    }

    private static String escapeCsv(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.stripLeading().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String stripBom(String text) {
        return text != null && text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace(" ", "").replace("_", "");
    }

    private static String escapeXml(String value) {
        String safe = value == null ? "" : value;
        return safe.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String columnName(int number) {
        StringBuilder name = new StringBuilder();
        while (number > 0) {
            int rem = (number - 1) % 26;
            name.insert(0, (char) ('A' + rem));
            number = (number - 1) / 26;
        }
        return name.toString();
    }

    private static int columnIndex(String ref) {
        int index = 0;
        for (int i = 0; i < ref.length(); i++) {
            char ch = ref.charAt(i);
            if (Character.isLetter(ch)) {
                index = index * 26 + (Character.toUpperCase(ch) - 'A' + 1);
            } else {
                break;
            }
        }
        return Math.max(1, index);
    }
}
