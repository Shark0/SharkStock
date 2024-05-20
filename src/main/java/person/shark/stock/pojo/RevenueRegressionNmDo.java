package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueRegressionNmDo {
    private BigDecimal nSlope;
    private BigDecimal totalNDeviationRevenue;
    private BigDecimal mSlope;
    private BigDecimal totalMDeviationRevenue;
}
