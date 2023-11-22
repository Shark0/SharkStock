package person.shark.stock.worker.stock;

import person.shark.stock.pojo.StockDo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class FilterWorker {
    public List<StockDo> filterByDividendRate(List<StockDo> stockDoList, BigDecimal dividendRate) {
        return stockDoList.stream().filter(stockDo -> stockDo.getCurrentDividendRate().compareTo(dividendRate) >= 0).collect(Collectors.toList());
    }

    public List<StockDo> filterByTempLowDividendRate(List<StockDo> stockList, int gap) {
        BigDecimal gapBigDecimal = BigDecimal.valueOf(gap);
        List<StockDo> stockDoList = new ArrayList<>();
        for(StockDo stockDo: stockList) {
            BigDecimal thePreviousYearDividendRate = stockDo.getThePreviousYearDividendRate();
            BigDecimal thePastTwoYearDividendRate = stockDo.getThePastTwoYearDividendRate();
            BigDecimal thePastThreeYearDividendRate = stockDo.getThePastThreeYearDividendRate();
            if(thePastThreeYearDividendRate.add(thePastTwoYearDividendRate).divide(BigDecimal.valueOf(2))
                    .subtract(thePreviousYearDividendRate).compareTo(gapBigDecimal) >= 0) {
                stockDoList.add(stockDo);
            }
        }

        return stockDoList;
    }
}
