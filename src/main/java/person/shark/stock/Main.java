package person.shark.stock;

import com.opencsv.exceptions.CsvValidationException;
import person.shark.stock.pojo.StockDO;
import person.shark.stock.worker.stock.StockWorker;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Main {

    public static void main(String[] argv) throws IOException, KeyManagementException,
            NoSuchAlgorithmException, CsvValidationException, InterruptedException {
        StockWorker stockWorker = new StockWorker();
        List<StockDO> stockList = stockWorker.findStockList();
        stockList = stockWorker.filterByDividendYield(stockList, new BigDecimal(0.07));
        stockList = stockWorker.sortByDividendYield(stockList);
        stockWorker.print(stockList);
    }
}
