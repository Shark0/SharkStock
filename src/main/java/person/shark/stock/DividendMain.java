package person.shark.stock;

import com.google.gson.Gson;
import person.shark.stock.pojo.DividendDo;
import person.shark.stock.worker.stock.JsoupWorker;

import java.util.List;

public class DividendMain {

    public static void main(String[] argv) {
        String stockCode = "2905";
        JsoupWorker jsoupWorker = new JsoupWorker();
        List<DividendDo> dividendDoList = jsoupWorker.dividend(stockCode, 5);
        System.out.println(new Gson().toJson(dividendDoList));
    }
}
