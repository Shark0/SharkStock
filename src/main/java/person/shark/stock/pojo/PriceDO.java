package person.shark.stock.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PriceDO implements Serializable {

    private RegularMarketPriceDO regularMarketPrice;
}
