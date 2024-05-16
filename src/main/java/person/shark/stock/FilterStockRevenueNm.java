package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class FilterStockRevenueNm {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> revenueList = filterWorker.filterByRevenueRegressionNm(stockDoList, 3, 12, 0, 0 );
        fileWorker.saveToDividendExcel("all_stock_revenue_regression_nm.xlsx", revenueList);
    }
}
