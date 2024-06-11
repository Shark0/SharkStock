package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;

import java.util.List;

public class FilterByMaxRevenue {
    public static void main(String[] args) {
        FileWorker fileWorker = new FileWorker();
        List<StockDo> stockDoList = fileWorker.loadFromJson("all_stock.json");
        FilterWorker filterWorker = new FilterWorker();
        List<StockDo> resultList = filterWorker.filterByMaxRevenue(stockDoList, 3);
        fileWorker.saveToStockExcel("max_stock.xlsx", resultList);
    }
}

