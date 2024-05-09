package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.RevenueDo;
import person.shark.stock.worker.stock.ExcelWorker;
import person.shark.stock.worker.stock.JsoupWorker;

import java.util.List;

public class RevenueMain {

    public static void main(String[] argv) {
        String stockCode = "2905";
        JsoupWorker jsoupWorker = new JsoupWorker();
        List<RevenueDo> revenueDoList = jsoupWorker.revenue(stockCode);
        System.out.println("revenueDoList: " + new Gson().toJson(revenueDoList));
        ExcelWorker excelWorker = new ExcelWorker();
        excelWorker.generateRevenueExcel(stockCode, 5, revenueDoList);
    }
}
