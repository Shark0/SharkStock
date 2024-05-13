package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DividendDo {
    Integer year;
    private BigDecimal dividend;
    private BigDecimal dividendRate;
}
