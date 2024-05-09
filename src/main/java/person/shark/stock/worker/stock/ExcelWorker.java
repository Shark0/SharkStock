package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import person.shark.stock.pojo.RevenueDo;
import person.shark.stock.pojo.StockDo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelWorker {
    public void saveToExcel(String fileName, List<StockDo> stockList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int thePreviousYear = currentYear - 1;
        int thePastTwoYear = currentYear - 2;
        int thePastThreeYear = currentYear - 3;

        List<String> titleList = List.of("Id", "Company", "Price", currentYear + " Dividend Rate",
                thePreviousYear + " Dividend Rate", thePastTwoYear + " Dividend Rate",
                thePastThreeYear + " Dividend Rate");
        Row titleRow = sheet.createRow(0);
        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(titleList.get(i));
        }

        int rowIndex = 1;
        for (StockDo stock : stockList) {
            Row row = sheet.createRow(rowIndex);
            System.out.println(new Gson().toJson(stock));
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
                        cell.setCellValue(stock.getCurrentDividendRate().doubleValue());
                        break;
                    case 4:
                        cell.setCellValue(stock.getThePreviousYearDividendRate().doubleValue());
                        break;
                    case 5:
                        cell.setCellValue(stock.getThePastTwoYearDividendRate().doubleValue());
                        break;
                    case 6:
                        cell.setCellValue(stock.getThePastThreeYearDividendRate().doubleValue());
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

    public List<StockDo> loadFromToExcel(String fileName) {
        List<StockDo> stockDoList = new ArrayList<>();
        try(Workbook workbook = WorkbookFactory.create(new File(fileName))) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            int i = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if(i == 0 ) {
                    i ++;
                    continue;
                }
                Iterator<Cell> cellIterator = row.cellIterator();
                StockDo stockDo = new StockDo();
                int j = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (j) {
                        case 0:
                            stockDo.setId(cell.getStringCellValue());
                            break;
                        case 1:
                            stockDo.setName(cell.getStringCellValue());
                            break;
                        case 2:
                            stockDo.setPrice(BigDecimal.valueOf(cell.getNumericCellValue()));
                            break;
                        case 3:
                            stockDo.setCurrentDividendRate(BigDecimal.valueOf(cell.getNumericCellValue()));
                            break;
                        case 4:
                            stockDo.setThePreviousYearDividendRate(BigDecimal.valueOf(cell.getNumericCellValue()));
                            break;
                        case 5:
                            stockDo.setThePastTwoYearDividendRate(BigDecimal.valueOf(cell.getNumericCellValue()));
                            break;
                        case 6:
                            stockDo.setThePastThreeYearDividendRate(BigDecimal.valueOf(cell.getNumericCellValue()));
                            break;
                    }
                    j++;
                }
                stockDoList.add(stockDo);
                i ++;
            }
        } catch (IOException e) {
            return stockDoList;
        }
        return stockDoList;
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
}
