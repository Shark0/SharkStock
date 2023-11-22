package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.StockDo;
import person.shark.stock.worker.stock.ExcelWorker;
import person.shark.stock.worker.stock.StockWorker;

import java.util.List;

public class DownloadAllStockMain {

    public static void main(String[] argv) {
        StockWorker stockWorker = new StockWorker();
        List<StockDo> stockList = stockWorker.findStockList();
        ExcelWorker excelWorker = new ExcelWorker();
        excelWorker.saveToExcel("ALL_STOCK.xlsx", stockList);
    }
}
