package person.shark.stock.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class RevenueDo {
    private Date date;
    private BigDecimal revenue;
}
