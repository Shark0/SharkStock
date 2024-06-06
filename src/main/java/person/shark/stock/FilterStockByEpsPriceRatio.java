package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class FilterStockByEpsPriceRatio {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> filterResultList = filterWorker.filterAndSortByEpsPriceRatio(
                stockDoList, new BigDecimal("0"));
        if(filterResultList.size() > 100) {
            filterResultList = filterResultList.subList(0, 200);
        }
        fileWorker.saveToEpsPriceRatioAndRevenueRegressionNExcel("all_stock_eps_price_ratio.xlsx", filterResultList);
    }
}
