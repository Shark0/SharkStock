package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class StockRevenueRegressionNm {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> revenueList =
                filterWorker.filterByRevenueRegressionNm(
                        stockDoList, 3, 9, new BigDecimal("0"), new BigDecimal("0"));
        fileWorker.generateRevenueRegressionNmExcel("revenue_regression_nm.xlsx", revenueList);
    }
}
