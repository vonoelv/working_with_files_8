package org.example;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import org.example.domain.Configuration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipWithMultipleFilesTest {
    public static final String ZIP_FILE = "files/ZipWithMultipleFilesTest/testzipfile.zip";
    public static final List<String> FILES_TO_CHECK_IN_ZIP = List.of(
            "junit-user-guide-5.8.2.pdf",
            "prajs_ot_1705.xlsx",
            "Machine_readable_file_bdc_sf_2021_q4.csv",
            "sample4.json");
    private final ClassLoader cl = getClass().getClassLoader();


    @Test
    void checkZipContent() throws Exception {
        ZipFile zf = new ZipFile(new File(cl.getResource(ZIP_FILE).toURI()));
        List<ZipEntry> zipEntries = zf.stream()
                .filter(zipEntry -> FILES_TO_CHECK_IN_ZIP.contains(zipEntry.getName()))
                .collect(Collectors.toList());

        assertThat(zipEntries.stream().map(ZipEntry::getName)).containsExactlyInAnyOrderElementsOf(FILES_TO_CHECK_IN_ZIP);
        for (ZipEntry zipEntry : zipEntries) {
            checkZipContentByExtension(zf, zipEntry);
        }
    }

    private void checkZipContentByExtension(ZipFile zf, ZipEntry zipEntry) throws Exception {
        switch (Files.getFileExtension(zipEntry.getName())) {
            case "pdf":
                checkPdf(zf, zipEntry);
                break;
            case "xlsx":
                checkXlsx(zf, zipEntry);
                break;
            case "csv":
                checkCsv(zf, zipEntry);
                break;
            case "json":
                checkJsonUsingJson(zf, zipEntry);
                checkJsonUsingJackson(zf, zipEntry);
                break;
        }
    }

    private void checkPdf(ZipFile zf, ZipEntry zipEntry) throws Exception {
        try (InputStream is = zf.getInputStream(zipEntry)) {
            PDF pdf = new PDF(is);

            assertThat((pdf.author)).contains("Marc Philipp");
        }
    }

    private void checkXlsx(ZipFile zf, ZipEntry zipEntry) throws Exception {
        try (InputStream is = zf.getInputStream(zipEntry)) {
            XLS xls = new XLS(is);

            assertThat(xls.excel
                    .getSheetAt(0)
                    .getRow(11)
                    .getCell(1)
                    .getStringCellValue()).contains("Сахалинская обл, Южно-Сахалинск");
        }
    }

    private void checkCsv(ZipFile zf, ZipEntry zipEntry) throws Exception {
        try (InputStream is = zf.getInputStream(zipEntry);
             CSVReader reader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String[]> content = reader.readAll();

            assertThat(content.get(0)).contains(
                    "Series_reference",
                    "Period",
                    "Data_value",
                    "Suppressed",
                    "STATUS",
                    "UNITS",
                    "Magnitude",
                    "Subject",
                    "Group",
                    "Series_title_1",
                    "Series_title_2",
                    "Series_title_3",
                    "Series_title_4",
                    "Series_title_5");
        }
    }

    private void checkJsonUsingJson(ZipFile zf, ZipEntry zipEntry) throws Exception {
        Gson gson = new Gson();
        try (InputStream is = zf.getInputStream(zipEntry)) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Configuration config = gson.fromJson(json, Configuration.class);

            assertThat(config.getWidget().getDebug()).isEqualTo("on");
            assertThat(config.getWidget().getWindow().getTitle()).isEqualTo("Sample Konfabulator Widget");
            assertThat(config.getWidget().getImage().getSrc()).isEqualTo("Images/Sun.png");
            assertThat(config.getWidget().getText().getData()).isEqualTo("Click Here");
        }
    }

    private void checkJsonUsingJackson(ZipFile zf, ZipEntry zipEntry) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = zf.getInputStream(zipEntry)) {
            Configuration config = mapper.createParser(is).readValueAs(Configuration.class);

            assertThat(config.getWidget().getDebug()).isEqualTo("on");
            assertThat(config.getWidget().getWindow().getTitle()).isEqualTo("Sample Konfabulator Widget");
            assertThat(config.getWidget().getImage().getSrc()).isEqualTo("Images/Sun.png");
            assertThat(config.getWidget().getText().getData()).isEqualTo("Click Here");
        }
    }
}

