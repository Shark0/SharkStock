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

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class StockWorker {

    public List<StockDO> findStockList() throws IOException, NoSuchAlgorithmException, KeyManagementException, CsvValidationException {
        HttpRequestWorker httpRequestWorker = new HttpRequestWorker();
        String url = "https://www.twse.com.tw/exchangeReport/STOCK_DAY_ALL?response=open_data";
        String response = httpRequestWorker.sendHttpsGetRequest(url);
        StringReader reader = new StringReader(response);
        CSVReader csvReader = new CSVReader(reader);
        BigDecimal minDividendYield = new BigDecimal(0.5);

        List<StockDO> stockList = new ArrayList<>();
        String[] record;
        int index = 0;
        while ((record = csvReader.readNext()) != null) {
            if (index != 0) {
                String price = record[7];
                if (StringUtil.isEmpty(price)) {
                    continue;
                }
                StockDO stock = new StockDO();
                stock.setId(record[0]);
                stock.setPrice(new BigDecimal(price));
                stock = findStockDividendInfo(stock);
//                if (stock.getDividend1() != null) {
//                    BigDecimal dividendYield = stock.getDividend1().divide(stock.getPrice(), 4, RoundingMode.FLOOR);
//                    if (dividendYield.compareTo(minDividendYield) >= 0) {
//                        stock.setName(record[1]);
//                        stockList.add(stock);
//                        System.out.println(new Gson().toJson(stock));
//                    }
//                }
                stock.setName(record[1]);
                stockList.add(stock);
                System.out.println(new Gson().toJson(stock));
            }
            index++;
        }
        return stockList;
    }

    private StockDO findStockDividendInfo(StockDO stock) {
        String url = "https://tw.stock.yahoo.com/d/s/dividend_" + stock.getId() + ".html";
//        System.out.println("url: " + url);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        try {
            Document document = Jsoup.connect(url).get();
            Elements yearDividendTrElements = document.getElementsByTag("tbody").get(3)
                    .getElementsByTag("tr");
            int trIndex = 0;
            for (Element yearDividendTrElement : yearDividendTrElements) {
                if (trIndex > 0) {
                    Elements yearDividendInfoTdElements = yearDividendTrElement.getElementsByTag("td");
                    int dividendYear = Integer.valueOf(yearDividendInfoTdElements.get(1).text().split("-")[0]);
                    BigDecimal dividend = new BigDecimal(yearDividendInfoTdElements.get(6).text());
                    int yearDifference = year - dividendYear;
                    switch (yearDifference) {
                        case 0:
                            if (stock.getDividend1() == null) {
                                stock.setDividend1(dividend);
                            } else {
                                stock.setDividend1(stock.getDividend1().add(dividend));
                            }
                            break;
                        case 1:
                            if (stock.getDividend2() == null) {
                                stock.setDividend2(dividend);
                            } else {
                                stock.setDividend2(stock.getDividend2().add(dividend));
                            }
                            break;
                        case 2:
                            if (stock.getDividend3() == null) {
                                stock.setDividend3(dividend);
                            } else {
                                stock.setDividend3(stock.getDividend3().add(dividend));
                            }
                            break;
                        case 3:
                            if (stock.getDividend4() == null) {
                                stock.setDividend4(dividend);
                            } else {
                                stock.setDividend4(stock.getDividend4().add(dividend));
                            }
                            break;
                    }
                }
                trIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return stock;
    }

    public List<StockDO> sortByDividendYield(List<StockDO> stockList) {
        stockList.sort(Comparator.comparing(stock -> stock.getDividendYield1().multiply(new BigDecimal(-1))));
        return stockList;
    }

    public void print(List<StockDO> stockList) {
        Gson gson = new Gson();
        for (StockDO stock : stockList) {
            System.out.println(gson.toJson(stock));
        }
    }
}
