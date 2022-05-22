package org.example.lecture;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.example.lecture.domain.Teacher;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

public class FilesParsingTest {
    ClassLoader classLoader = getClass().getClassLoader();

    @Test
    void ParsePdfTest() throws IOException {
        open("https://junit.org/junit5/docs/current/user-guide/");
        File downloadedFile = $(By.linkText("PDF download")).download();

        PDF pdf = new PDF(downloadedFile);
        assertThat((pdf.author)).contains("Marc Philipp");
    }

    @Test
    void parseXlsTest() throws FileNotFoundException {
        open("http://romashka2008.ru/price");
        //File downloadedFile = $(By.linkText("Скачать Прайс-лист Excel")).download();
        File xlsDownload = $(".site-main__inner a[href*='prajs_ot']").download();

        XLS xls = new XLS(xlsDownload);
        assertThat(xls.excel
                .getSheetAt(0)
                .getRow(11)
                .getCell(1)
                .getStringCellValue()).contains("Сахалинская обл, Южно-Сахалинск");
    }

    @Test
    void parseCsvTest() throws IOException, CsvException {
        try (InputStream is = classLoader.getResourceAsStream("files/FilesParsingTest/Machine_readable_file_bdc_sf_2021_q4.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(is))) {
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

    @Test
    void parseZipTest() throws Exception {
        try (InputStream is = classLoader.getResourceAsStream("files/FilesParsingTest/business-financial-data-december-2021-quarter-csv.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                assertThat(entry.getName()).isEqualTo("Machine_readable_file_bdc_sf_2021_q4.csv");
            }
        }
    }

    @Test
    void jsonTest() throws Exception {
        Gson gson = new Gson();
        try (InputStream is = classLoader.getResourceAsStream("files/FilesParsingTest/simple.json")) {
            assert is != null;
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            assertThat(jsonObject.get("name").getAsString()).isEqualTo("Ivan");
            assertThat(jsonObject.get("address").getAsJsonObject().get("street").getAsString()).isEqualTo("Mira");
        }
    }

    @Test
    void jsonTypeTest() throws Exception {
        Gson gson = new Gson();
        try (InputStream is = classLoader.getResourceAsStream("files/FilesParsingTest/simple.json")) {
            assert is != null;
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Teacher jsonObject = gson.fromJson(json, Teacher.class);
            assertThat(jsonObject.name).isEqualTo("Ivan");
            assertThat(jsonObject.address.street).isEqualTo("Mira");
        }
    }
}
