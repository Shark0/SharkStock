package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDo {
    private String id;
    private String name;
    private BigDecimal price;
    private BigDecimal currentDividendRate;
    private BigDecimal thePreviousYearDividend;
    private BigDecimal thePreviousYearDividendRate;
    private BigDecimal thePastTwoYearDividendRate;
    private BigDecimal thePastThreeYearDividendRate;
}
