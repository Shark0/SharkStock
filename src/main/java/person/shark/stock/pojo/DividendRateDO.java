package person.shark.stock.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DividendRateDO implements Serializable {
    private BigDecimal raw;
}
