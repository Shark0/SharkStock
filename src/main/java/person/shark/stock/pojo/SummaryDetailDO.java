package person.shark.stock.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SummaryDetailDO implements Serializable {
    private DividendRateDO dividendRate;
    private DividendYieldDO dividendYield;
}
