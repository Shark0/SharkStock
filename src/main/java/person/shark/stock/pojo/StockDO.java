package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StockDO {

    private String id;

    private String name;

    private BigDecimal price;

    private BigDecimal dividend1;
    private BigDecimal dividendYield1;

    private BigDecimal dividend2;
    private BigDecimal dividendYield2;

    private BigDecimal dividend3;
    private BigDecimal dividendYield3;

    private BigDecimal dividend4;
    private BigDecimal dividendYield4;

    private BigDecimal dividendYieldStandardDeviationRate;

}
