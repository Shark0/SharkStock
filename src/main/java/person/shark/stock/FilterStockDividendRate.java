package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class FilterStockDividendRate {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> dividendList = filterWorker.filterByDividendRate(stockDoList, new BigDecimal(0.02));
        System.out.println("dividendList = " + new Gson().toJson(dividendList));
        fileWorker.saveToDividendExcel("2_dividend.xlsx", dividendList);
    }
}
