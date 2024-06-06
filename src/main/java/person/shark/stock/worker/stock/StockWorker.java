package person.shark.stock.worker.stock;

import com.opencsv.CSVReader;
import person.shark.stock.pojo.DividendDo;
import person.shark.stock.pojo.EpsDo;
import person.shark.stock.pojo.RevenueDo;
import person.shark.stock.pojo.StockDo;
import person.shark.stock.util.StringUtil;
import person.shark.stock.worker.http.HttpRequestWorker;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockWorker {

    public List<StockDo> findStockList() {
        JsoupWorker jsoupWorker = new JsoupWorker();
        List<StockDo> stockList = new ArrayList<>();
        try {
            HttpRequestWorker httpRequestWorker = new HttpRequestWorker();
            String url = "https://www.twse.com.tw/exchangeReport/STOCK_DAY_ALL?response=open_data";
            String response = httpRequestWorker.sendHttpsGetRequest(url);
            StringReader reader = new StringReader(response);
            CSVReader csvReader = new CSVReader(reader);
            String[] record;
            int index = 0;
            while ((record = csvReader.readNext()) != null) {
                if (index == 0 || record.length == 0) {
                    //avoid title
                    index ++;
                    continue;
                }
                String price = record[7];
                if (!StringUtil.isEmpty(price) && !record[0].startsWith("0")) {
                    String stockId = record[0];
                    StockDo stock = new StockDo();
                    stock.setId(stockId);
                    stock.setName(record[1]);
                    stock.setPrice(new BigDecimal(price));
                    List<DividendDo> divideList = jsoupWorker.dividend(stockId, 5);
                    stock.setDividendList(divideList);
                    List<RevenueDo> revenueList = jsoupWorker.revenue(stockId);
                    stock.setRevenueList(revenueList);
                    List<EpsDo> epsList = jsoupWorker.eps(stockId, 5);
                    stock.setEpsList(epsList);
                    stockList.add(stock);
                }
                index++;
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockList;
    }
}
