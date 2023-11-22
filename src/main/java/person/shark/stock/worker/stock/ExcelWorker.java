package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import person.shark.stock.pojo.StockDo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelWorker {
    public void saveToExcel(String fileName, List<StockDo> stockList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        List<String> titleList = List.of("Id", "Company", "Price", "Current Year Dividend Rate",
                "The Previous Year Dividend Rate", "The Past Two Year Dividend Rate", "The Past Three Year Dividend Rate");
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

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)){
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
}
