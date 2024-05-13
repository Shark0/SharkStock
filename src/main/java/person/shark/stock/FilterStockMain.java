package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class FilterStockMain {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> dividendList = filterWorker.filterByDividendRate(stockDoList, new BigDecimal(0.02));
        System.out.println("dividendList = " + new Gson().toJson(dividendList));
        fileWorker.saveToDividendExcel("5_dividend.xlsx", dividendList);
//        List<StockDo> revenueList = filterWorker.filterByRevenueRegression(stockDoList, 3, 12, 0, 0 );
        List<StockDo> revenueList = filterWorker.filterByRevenueRegression(stockDoList, 4);
        fileWorker.saveToDividendExcel("all_stock_revenue_regression.xlsx", revenueList);
    }
}
