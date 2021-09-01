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

    private BigDecimal dividend;

    private BigDecimal dividendYield;
}
