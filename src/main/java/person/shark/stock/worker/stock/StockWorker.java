package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import person.shark.stock.pojo.StockDO;
import person.shark.stock.pojo.YahooStockDO;
import person.shark.stock.util.StringUtil;
import person.shark.stock.worker.http.HttpRequestWorker;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
                    YahooStockWorker yahooStockWorker = new YahooStockWorker(stockId, new YahooStockWorker.YahooStockListener() {
                        @Override
                        public void callBack(String stockId, YahooStockDO yahooStock) {
                            if(yahooStock != null) {
                                StockDO stock = stockMap.get(stockId);
                                stock.setDividendRate(yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getDividendRate().getRaw());
                                stock.setDividendYield(yahooStock.getQuoteSummary().getResult().get(0).getSummaryDetail().getDividendYield().getRaw());
                            } else {
                                stockMap.remove(stockId);
                            }
                            stockLockSet.remove(stockId);
                        }
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

    public List<StockDO> sortByDividendYield(List<StockDO> stockList) {
        stockList.sort(Comparator.comparing(stock -> stock.getDividendYield().multiply(new BigDecimal(-1))));
        return stockList;
    }

    public void print(List<StockDO> stockList) {
        for (StockDO stock : stockList) {
            BigDecimal dividendRateSevenPrice = stock.getDividendRate().divide(new BigDecimal(0.07), 2, RoundingMode.FLOOR);
            BigDecimal dividendRateSixDotFivePrice = stock.getDividendRate().divide(new BigDecimal(0.065), 2, RoundingMode.FLOOR);
            System.out.println("編號: " + stock.getId() + ", 名稱: " + stock.getName() + ", 價錢: " + stock.getPrice() +
                    ", 股利: " + stock.getDividendRate() + ", 殖利率: " + stock.getDividendYield() +
                    ", 殖利率7%價錢: " + dividendRateSevenPrice + ", 殖利率6.5%價錢: " + dividendRateSixDotFivePrice);
        }
    }
}
