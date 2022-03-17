package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RawDO {
    private BigDecimal raw;
    private String fmt;
}
