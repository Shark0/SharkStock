package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class FilterStockByEpsPriceRatioAndRevenueRegressN {
    public static void main(String[] argv) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> filterResultList  = filterWorker.filterByRevenueRegressionN(stockDoList, 3, new BigDecimal("80"));
        System.out.println("filterResultList size: " + filterResultList.size());
        filterResultList = filterWorker.filterAndSortByEpsPriceRatio(filterResultList, new BigDecimal("0.05"));
        System.out.println("filterResultList size: " + filterResultList.size());
        fileWorker.saveToEpsPriceRatioAndRevenueRegressionNExcel("all_stock_eps_price_ratio_revenue_slot.xlsx", filterResultList);
    }
}
