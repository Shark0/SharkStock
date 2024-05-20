package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueRegressionNDo {
    BigDecimal totalNDeviationRevenue;
    private BigDecimal nSlop;
}
