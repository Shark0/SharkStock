package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.ExcelWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.math.BigDecimal;
import java.util.List;

public class FilterStockMain {
    public static void main(String[] argv) {
        ExcelWorker excelWorker = new ExcelWorker();
        List<StockDo> stockList = excelWorker.loadFromToExcel("ALL_STOCK.xlsx");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> highDividendRateStockList = filterWorker.filterByDividendRate(stockList, new BigDecimal("5"));
        excelWorker.saveToExcel("HIGH_DIVIDEND_RATE.xlsx", highDividendRateStockList);
        List<StockDo> tempLowDividendRateStockList = filterWorker.filterByTempLowDividendRate(stockList, 4);
        excelWorker.saveToExcel("TEMP_LOW_DIVIDEND_RATE.xlsx", tempLowDividendRateStockList);
    }
}
