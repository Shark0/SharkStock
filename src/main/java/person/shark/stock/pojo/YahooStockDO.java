package person.shark.stock.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class YahooStockDO implements Serializable {
    private PriceDO price;
    private SummaryDetailDO summaryDetail;
}
