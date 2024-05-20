package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import person.shark.stock.pojo.DividendDo;
import person.shark.stock.pojo.RevenueDo;
import person.shark.stock.pojo.StockDo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileWorker {

    public void saveToJson(String fileName, List<StockDo> stockList) {
        String json = new Gson().toJson(stockList);
        String path = "file/" + fileName;
        try {
            Files.write(Paths.get(path), json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StockDo> loadFromJson(String fileName) {
        String path = "file/" + fileName;
        try {
            String json = Files.readString(Paths.get(path));
            return new Gson().fromJson(json, new TypeToken<List<StockDo>>(){}.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void saveToDividendExcel(String fileName, List<StockDo> stockList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int thePreviousYear = currentYear - 1;
        int thePastTwoYear = currentYear - 2;
        int thePastThreeYear = currentYear - 3;

        List<String> titleList = List.of("Id", "Company", "Price", currentYear + " Dividend",
                thePreviousYear + " Dividend", thePastTwoYear + " Dividend",
                thePastThreeYear + " Dividend");
        Row titleRow = sheet.createRow(0);
        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(titleList.get(i));
        }

        int rowIndex = 1;
        for (StockDo stock : stockList) {
            Row row = sheet.createRow(rowIndex);
            HashMap<Integer, BigDecimal> yearDividendMap = new HashMap<>();
            List<DividendDo> dividendDoList = stock.getDividendList();
            for(DividendDo dividendDo: dividendDoList) {
                Integer year = dividendDo.getYear();
                BigDecimal dividend = dividendDo.getDividend();
                yearDividendMap.put(year, dividend);
            }

            for(int i = 0; i < titleList.size(); i ++) {
                Cell cell = row.createCell(i);
                switch (i) {
                    case 0:
                        cell.setCellValue(stock.getId());
                        break;
                    case 1:
                        cell.setCellValue(stock.getName());
                        break;
                    case 2:
                        cell.setCellValue(stock.getPrice().doubleValue());
                        break;
                    case 3:
                        if(yearDividendMap.get(currentYear) != null) {
                            cell.setCellValue(yearDividendMap.get(currentYear).doubleValue());
                        } else {
                            cell.setCellValue(0);
                        }
                        break;
                    case 4:

                        if(yearDividendMap.get(thePreviousYear) != null) {
                            cell.setCellValue(yearDividendMap.get(thePreviousYear).doubleValue());
                        } else {
                            cell.setCellValue(0);
                        }
                        break;
                    case 5:
                        if(yearDividendMap.get(thePastTwoYear) != null) {
                            cell.setCellValue(yearDividendMap.get(thePastTwoYear).doubleValue());
                        } else {
                            cell.setCellValue(0);
                        }
                        break;
                    case 6:
                        if(yearDividendMap.get(thePastThreeYear) != null) {
                            cell.setCellValue(yearDividendMap.get(thePastThreeYear).doubleValue());
                        } else {
                            cell.setCellValue(0);
                        }
                        break;
                }
            }
            rowIndex = rowIndex + 1;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream("file/" + fileName)){
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateRevenueExcel(String stockCode, int statisticsYearCount, List<RevenueDo> revenueDoList) {
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM");
        for(RevenueDo revenueDo: revenueDoList) {
            String key = simpleDateFormat.format(revenueDo.getDate());
            revenueMap.put(key, revenueDo.getRevenue());
        }

        System.out.println("revenueMap: " + new Gson().toJson(revenueMap));
        List<String> titleList = List.of("", "1", "2", "3", "4", "5", "6",
                "7", "8", "9", "10", "11", "12");
        String fileName = stockCode + "_revenue.xlsx";
        try(FileOutputStream fileOutputStream = new FileOutputStream("file/" + fileName);
            Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row titleRow = sheet.createRow(0);
            for (int i = 0; i < titleList.size(); i++) {
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(titleList.get(i));
            }

            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            for(int i = 0; i < statisticsYearCount; i++) {
                int year = currentYear - i;
                for(int j = 0; j < 2; j ++) {
                    int index = i * 2 + j + 1;
                    Row row = sheet.createRow(index);
                    for(int k = 0; k < 13; k++) {
                        Cell cell = row.createCell(k);
                        String key = String.format("%d_%02d", year, k);
                        if(j == 0) {
                            if (k == 0) {
                                cell.setCellValue(year + "營收");
                            } else {
                                BigDecimal revenue = revenueMap.get(key);
                                if(revenue != null) {
                                    cell.setCellValue(revenue.toString());
                                } else {
                                    cell.setCellValue("");
                                }
                            }
                        } else {
                            if (k == 0) {
                                cell.setCellValue(year + "季營收");
                            } else {
                                if (k % 3 == 0) {
                                    BigDecimal seasonRevenue = new BigDecimal(0);
                                    BigDecimal currentMonthRevenue = revenueMap.get(key);
                                    if (currentMonthRevenue != null) {
                                        seasonRevenue = seasonRevenue.add(currentMonthRevenue);
                                    }
                                    BigDecimal previewOneMonthRevenue =
                                            revenueMap.get(String.format("%d_%02d", year, (k - 1)));
                                    if (previewOneMonthRevenue != null) {
                                        seasonRevenue = seasonRevenue.add(previewOneMonthRevenue);
                                    }
                                    BigDecimal previewTwoMonthRevenue =
                                            revenueMap.get(String.format("%d_%02d", year, (k - 2)));
                                    if (previewTwoMonthRevenue != null) {
                                        seasonRevenue = seasonRevenue.add(previewTwoMonthRevenue);
                                    }
                                    cell.setCellValue(seasonRevenue.toString());
                                } else {
                                    cell.setCellValue("");
                                }
                            }
                        }
                    }
                }
            }
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void generateRevenueRegressionNExcel(String fileName, List<StockDo> stockList) {
        try(Workbook workbook = new XSSFWorkbook()) {
            List<String> titleList = List.of("Id", "Company", "Price", "N Slope");
            Sheet sheet = workbook.createSheet();
            Row titleRow = sheet.createRow(0);
            for (int i = 0; i < titleList.size(); i++) {
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(titleList.get(i));
            }

            int rowIndex = 1;
            for (StockDo stock : stockList) {
                Row row = sheet.createRow(rowIndex);
                for(int i = 0; i < titleList.size(); i ++) {
                    Cell cell = row.createCell(i);
                    switch (i) {
                        case 0:
                            cell.setCellValue(stock.getId());
                            break;
                        case 1:
                            cell.setCellValue(stock.getName());
                            break;
                        case 2:
                            cell.setCellValue(stock.getPrice().doubleValue());
                            break;
                        case 3:
                            cell.setCellValue(stock.getNSlope().doubleValue());
                            break;

                    }
                }
                rowIndex = rowIndex + 1;
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream("file/" + fileName)){
                workbook.write(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateRevenueRegressionNmExcel(String fileName, List<StockDo> stockList) {
        try(Workbook workbook = new XSSFWorkbook()) {
            List<String> titleList = List.of("Id", "Company", "Price", "N Slope", "M Slope");
            Sheet sheet = workbook.createSheet();
            Row titleRow = sheet.createRow(0);
            for (int i = 0; i < titleList.size(); i++) {
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(titleList.get(i));
            }

            int rowIndex = 1;
            for (StockDo stock : stockList) {
                Row row = sheet.createRow(rowIndex);
                for(int i = 0; i < titleList.size(); i ++) {
                    Cell cell = row.createCell(i);
                    switch (i) {
                        case 0:
                            cell.setCellValue(stock.getId());
                            break;
                        case 1:
                            cell.setCellValue(stock.getName());
                            break;
                        case 2:
                            cell.setCellValue(stock.getPrice().doubleValue());
                            break;
                        case 3:
                            cell.setCellValue(stock.getNSlope().doubleValue());
                            break;
                        case 4:
                            cell.setCellValue(stock.getMSlope().doubleValue());
                            break;

                    }
                }
                rowIndex = rowIndex + 1;
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream("file/" + fileName)){
                workbook.write(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
