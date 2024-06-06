package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class StockDo {
    private String id;
    private String name;
    private BigDecimal price;
    private List<DividendDo> dividendList;
    private List<RevenueDo> revenueList;
    private List<EpsDo> epsList;
    private BigDecimal epsPriceRatio;
    private BigDecimal nSlope;
    private BigDecimal totalNDeviationRevenue;
    private BigDecimal mSlope;
    private BigDecimal totalMDeviationRevenue;
}
