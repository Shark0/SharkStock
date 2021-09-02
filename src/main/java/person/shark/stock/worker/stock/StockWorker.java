package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import person.shark.stock.pojo.StockDO;
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
        Set<String> dividendLockSet = new HashSet<>();
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

                    dividendLockSet.add(stockId);
                    Runnable runnable = () -> {
                        HashMap<String, BigDecimal> dividendMap = findDividendById(stockId);
                        StockDO stock1 = stockMap.get(stockId);
                        stock1.setDividendMap(dividendMap);
                        dividendLockSet.remove(stockId);
                    };
                    new Thread(runnable).start();
                    //打太快會被denied
                    Thread.sleep(400);
                }
            }
            index++;
        }

        while (!dividendLockSet.isEmpty()) {
            Thread.sleep(100);
        }

        List<StockDO> stockList = new ArrayList<>();
        for(String stockId: stockMap.keySet()) {
            StockDO stock = stockMap.get(stockId);
            if(stock.getDividendMap() != null &&  !stock.getDividendMap().isEmpty()) {
                calculateDividend(stock);
                System.out.println(new Gson().toJson(stock));
                stockList.add(stock);
            }
        }
        return stockList;
    }

    public HashMap<String, BigDecimal> findDividendById(String stockId) {
        HashMap hashMap = new HashMap();
        try {
            String url = "https://tw.stock.yahoo.com/d/s/dividend_" + stockId + ".html";
            System.out.println("url: " + url);
            Document document = Jsoup.connect(url).timeout(100000).post();
            if(document.getElementsByTag("tbody").size() == 0) {
                return hashMap;
            }
            Elements yearDividendTrElements = document.getElementsByTag("tbody").get(3)
                    .getElementsByTag("tr");
            int trIndex = 0;
            for (Element yearDividendTrElement : yearDividendTrElements) {
                if (trIndex > 0) {
                    Elements yearDividendInfoTdElements = yearDividendTrElement.getElementsByTag("td");
                    String dividendTime = yearDividendInfoTdElements.get(0).text();
                    BigDecimal dividend = new BigDecimal(yearDividendInfoTdElements.get(6).text());
                    hashMap.put(dividendTime, dividend);
                }
                trIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    private void calculateDividend(StockDO stock) {
        HashMap<String, BigDecimal> yearDividendMap = new HashMap<>();
        HashMap<String, BigDecimal> yearDividendRateMap = new HashMap<>();
        for(String time: stock.getDividendMap().keySet()) {
            String year = time.split("年")[0];
            //處理上半年、下半年、季
            BigDecimal yearDividend = yearDividendMap.get(year);
            if(yearDividend == null) {
                yearDividend = BigDecimal.ZERO;
                yearDividendMap.put(year, yearDividend);
            }
            BigDecimal timeDividend = stock.getDividendMap().get(time);
            yearDividendMap.put(year, yearDividend.add(timeDividend));

            BigDecimal yearDividendRate = yearDividendRateMap.get(year);
            if(yearDividendRate == null) {
                yearDividendRate = BigDecimal.ZERO;
                yearDividendRateMap.put(year, yearDividendRate);
            }
            if(time.contains("季")) {
                yearDividendRateMap.put(year, yearDividendRate.add(new BigDecimal(0.25)));
            } else if(time.contains("上半年") || time.contains("下半年")) {
                yearDividendRateMap.put(year, yearDividendRate.add(new BigDecimal(0.5)));
            } else {
                yearDividendRateMap.put(year, yearDividendRate.add(new BigDecimal(1)));
            }
        }
        //找最近的一年
        int year = 0 ;
        for(String key: yearDividendMap.keySet()) {
            Integer keyYear = Integer.valueOf(key);
            if(keyYear > year) {
                year = keyYear;
            }
        }
        System.out.println("calculateDividend year: " + year);
        System.out.println("calculateDividend yearDividendRateMap: " + new Gson().toJson(yearDividendMap));
        System.out.println("calculateDividend yearDividendRateMap: " + new Gson().toJson(yearDividendRateMap));
        BigDecimal yearDividend = yearDividendMap.get(String.valueOf(year));
        BigDecimal yearDividendRate = yearDividendRateMap.get(String.valueOf(year));
        BigDecimal dividend = yearDividend.divide(yearDividendRate, 4 , RoundingMode.FLOOR);
        BigDecimal dividendYield = dividend.divide(stock.getPrice(), 4, RoundingMode.FLOOR);

        stock.setDividend(dividend);
        stock.setDividendYield(dividendYield);
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
            System.out.println("編號: " + stock.getId() + ", 名稱: " + stock.getName() + ", 價錢: " + stock.getPrice() +
                    ", 股利: " + stock.getDividend() + ", 殖利率: " + stock.getDividendYield());
        }
    }
}
