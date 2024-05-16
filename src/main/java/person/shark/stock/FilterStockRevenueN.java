package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.util.List;

public class FilterStockRevenueN {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> revenueList = filterWorker.filterByRevenueRegressionN(stockDoList, 3);
        fileWorker.saveToDividendExcel("all_stock_revenue_regression_n.xlsx", revenueList);
    }
}
