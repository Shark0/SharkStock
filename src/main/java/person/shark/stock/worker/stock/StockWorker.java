package person.shark.stock.worker.stock;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import person.shark.stock.pojo.StockDo;
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
                    jsoupWorker.findYahooStockInfo(stock);
                    stockList.add(stock);
                }
                index++;
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockList;
    }
}
