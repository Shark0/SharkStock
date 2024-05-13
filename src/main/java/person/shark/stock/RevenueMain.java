package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.RevenueDo;
import person.shark.stock.worker.stock.FileWorker;
import person.shark.stock.worker.stock.FilterWorker;
import person.shark.stock.worker.stock.JsoupWorker;

import java.util.List;

public class RevenueMain {

    public static void main(String[] argv) {
        String stockCode = "1101";
        JsoupWorker jsoupWorker = new JsoupWorker();
        List<RevenueDo> revenueDoList = jsoupWorker.revenue(stockCode);

        System.out.println("revenueDoList: " + new Gson().toJson(revenueDoList));
        FileWorker fileWorker = new FileWorker();
        fileWorker.generateRevenueExcel(stockCode, 5, revenueDoList);
        FilterWorker filterWorker = new FilterWorker();
        boolean isMeetRevenueRegressionCondition = filterWorker.isMeetRevenueRegressionCondition(revenueDoList, 3, 9, 0, 0);
        System.out.println("isMeetRevenueRegressionCondition = " + isMeetRevenueRegressionCondition);
    }
}
