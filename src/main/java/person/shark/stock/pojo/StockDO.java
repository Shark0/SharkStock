package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;

@Data
public class StockDO {

    private String id;

    private String name;

    private BigDecimal price;

    private BigDecimal dividendRate;

    private BigDecimal dividendYield;
}
