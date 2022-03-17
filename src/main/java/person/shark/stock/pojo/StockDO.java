package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDO {

    private String id;

    private String name;

    private BigDecimal price;

    private BigDecimal dividendRate;

    private BigDecimal dividendYield;

    private BigDecimal trailingEps;

    private BigDecimal forwardEps;

    private BigDecimal trailingPe;

    private BigDecimal forwardPe;
}
