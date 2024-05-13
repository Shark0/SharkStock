package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.StockWorker;

import java.util.List;

public class DownloadAllStockMain {
    public static void main(String[] argv) {
        StockWorker stockWorker = new StockWorker();
        List<StockDo> stockDoList = stockWorker.findStockList();
        FileWorker fileWorker = new FileWorker();
        fileWorker.saveToJson("all_stock.json", stockDoList);
    }
}
