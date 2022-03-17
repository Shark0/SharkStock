package person.shark.stock.worker.stock;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import person.shark.stock.pojo.StockDO;
import person.shark.stock.util.StringUtil;
import person.shark.stock.worker.http.HttpRequestWorker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StockWorker {

    public List<StockDO> findStockList() throws IOException, KeyManagementException, NoSuchAlgorithmException, CsvValidationException, InterruptedException {
        HashMap<String, StockDO> stockMap = new HashMap<>();
        Set<String> stockLockSet = new HashSet<>();
        HttpRequestWorker httpRequestWorker = new HttpRequestWorker();
        String url = "https://www.twse.com.tw/exchangeReport/STOCK_DAY_ALL?response=open_data";
        String response = httpRequestWorker.sendHttpsGetRequest(url);
        StringReader reader = new StringReader(response);
        CSVReader csvReader = new CSVReader(reader);
        String[] record;
        int index = 0;
        while ((record = csvReader.readNext()) != null) {
            if (index != 0) {
                String price = record[7];
                if (!StringUtil.isEmpty(price) && !record[0].startsWith("0")) {
                    String stockId = record[0];
                    StockDO stock = new StockDO();
                    stock.setId(stockId);
                    stock.setName(record[1]);
                    stock.setPrice(new BigDecimal(price));
                    stockMap.put(stockId, stock);
                    stockLockSet.add(stockId);
                    YahooStockWorker yahooStockWorker = new YahooStockWorker(stockId, (stockId1, yahooStock) -> {
                        if(yahooStock != null) {
                            try {
                                StockDO callBackStock = stockMap.get(stockId1);
                                callBackStock.setDividendRate(yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getDividendRate().getRaw());
                                callBackStock.setDividendYield(yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getDividendYield().getRaw());

                                BigDecimal forwardEps = new BigDecimal(0);
                                if(yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getForwardEps() != null &&
                                        yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getForwardEps().getRaw() != null) {
                                    forwardEps = yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getForwardEps().getRaw();
                                }
                                callBackStock.setForwardEps(forwardEps);

                                BigDecimal trailingEps = new BigDecimal(0);
                                if(yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getTrailingEps() != null &&
                                        yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getTrailingEps().getRaw() != null) {
                                    trailingEps = yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getTrailingEps().getRaw();
                                }
                                callBackStock.setTrailingEps(trailingEps);

                                BigDecimal forwardPe = new BigDecimal(0);
                                if(yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getForwardPe() != null &&
                                        yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getForwardPe().getRaw() != null) {
                                    forwardPe = yahooStock.getQuoteSummary().getResult().get(0).getDefaultKeyStatistics().getForwardPe().getRaw();
                                }
                                callBackStock.setForwardPe(forwardPe);

                                BigDecimal trailingPe = new BigDecimal(0);
                                if(yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getTrailingPe() != null &&
                                        yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getTrailingPe().getRaw() != null) {
                                    trailingPe = yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getTrailingPe().getRaw();
                                }
                                callBackStock.setTrailingPe(trailingPe);
                            } catch (Exception e) {
                                System.out.println("error stockId1 = " + stockId1);
                                e.printStackTrace();
                            }

                        } else {
                            stockMap.remove(stockId1);
                        }
                        stockLockSet.remove(stockId1);
                    });
                    new Thread(yahooStockWorker).start();
                    //打太快會被denied
                    Thread.sleep(100);
                }
            }
            index++;
        }

        while (!stockLockSet.isEmpty()) {
            Thread.sleep(100);
        }

        List<StockDO> stockList = new ArrayList<>();
        for(String stockId: stockMap.keySet()) {
            StockDO stock = stockMap.get(stockId);
            stockList.add(stock);
        }
        return stockList;
    }

    public List<StockDO> filterByDividendYield(List<StockDO> stockList, BigDecimal dividendYield) {
        return stockList.stream().filter(stock -> stock.getDividendYield().compareTo(dividendYield) >= 0).collect(Collectors.toList());
    }

    public List<StockDO> filterByPe(List<StockDO> stockList, BigDecimal pe) {
        BigDecimal minCondition = new BigDecimal(0);

        return stockList.stream().filter(stock ->
                stock.getTrailingPe().compareTo(minCondition) > 0 && stock.getTrailingPe().compareTo(pe) <= 0
        ).collect(Collectors.toList());
    }

    public List<StockDO> sortByDividendYield(List<StockDO> stockList) {
        stockList.sort(Comparator.comparing(stock -> stock.getDividendYield().multiply(new BigDecimal(-1))));
        return stockList;
    }

    public List<StockDO> sortByPe(List<StockDO> stockList) {
        stockList.sort(Comparator.comparing(stock -> stock.getTrailingPe()));
        return stockList;
    }

    public void saveToExcel(List<StockDO> stockList) {
        String fileName = "Stock_" + new SimpleDateFormat("yyyy-MM-dd_hh_mm").format(new Date()) + ".xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        List<String> titleList = List.of("Id", "Company", "Price", "Dividend", "Yield", "Trailing Eps", "Forward Eps", "Training Pe", "Forward Pe");
        Row titleRow = sheet.createRow(0);
        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(titleList.get(i));
        }

        int rowIndex = 1;
        for (StockDO stock : stockList) {
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
                        cell.setCellValue(stock.getDividendRate().doubleValue());
                        break;
                    case 4:
                        cell.setCellValue(stock.getDividendYield().doubleValue());
                        break;
                    case 5:
                        cell.setCellValue(stock.getTrailingEps().doubleValue());
                        break;
                    case 6:
                        cell.setCellValue(stock.getForwardEps().doubleValue());
                        break;
                    case 7:
                        cell.setCellValue(stock.getTrailingPe().doubleValue());
                        break;
                    case 8:
                        cell.setCellValue(stock.getForwardPe().doubleValue());
                        break;
                }
            }
            rowIndex = rowIndex + 1;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)){
            workbook.write(fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
