package person.shark.stock;

import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.StockWorker;

import java.util.List;

public class GenerateAllStockExcel {

    public static void main(String[] argv) {
        StockWorker stockWorker = new StockWorker();
        List<StockDo> stockList = stockWorker.findStockList();

    }
}
