package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import person.shark.stock.pojo.StockDO;
import person.shark.stock.pojo.YahooStockDO;
import person.shark.stock.worker.http.HttpRequestWorker;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StockWorker {

    public List<StockDO> findStockList() throws IOException, KeyManagementException, NoSuchAlgorithmException, CsvValidationException {
        List<StockDO> stockList = new ArrayList<>();
        HttpRequestWorker httpRequestWorker = new HttpRequestWorker();
        String url = "https://www.twse.com.tw/exchangeReport/STOCK_DAY_ALL?response=open_data";
        String response = httpRequestWorker.sendHttpsGetRequest(url);
        StringReader reader = new StringReader(response);
        CSVReader csvReader = new CSVReader(reader);
        String[] record;
        int index = 0;
        while ((record = csvReader.readNext()) != null) {
            if (index != 0) {
                StockDO stock = new StockDO();
                stock.setId(record[0]);
                stock.setName(record[1]);
                if(!stock.getId().startsWith("0")) {
                    YahooStockDO yahooStock = findYahooStock(stock.getId());
                    if (yahooStock != null &&
                            yahooStock.getSummaryDetail() != null &&
                            yahooStock.getSummaryDetail().getDividendYield() != null &&
                            yahooStock.getSummaryDetail().getDividendYield().getRaw() != null &&
                            yahooStock.getSummaryDetail().getDividendRate() != null &&
                            yahooStock.getSummaryDetail().getDividendRate().getRaw() != null
                    ) {
                        stock.setPrice(yahooStock.getPrice().getRegularMarketPrice().getRaw());
                        stock.setDividend(yahooStock.getSummaryDetail().getDividendRate().getRaw());
                        stock.setDividendYield(yahooStock.getSummaryDetail().getDividendYield().getRaw());
                        stockList.add(stock);
                    }
                }
            }
            index++;
        }
        return stockList;
    }

    private YahooStockDO findYahooStock(String stockId) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/v2/get-summary?symbol=" + stockId + ".tw")
                    .get()
                    .addHeader("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", "aae42a4805msh1f83c3aab7c5452p1bc18bjsn9d6c72c432ab")
                    .build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            YahooStockDO yahooStock = new Gson().fromJson(result, YahooStockDO.class);
            return yahooStock;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
