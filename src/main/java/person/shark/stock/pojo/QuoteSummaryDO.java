package person.shark.stock.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QuoteSummaryDO {
    private List<ResultDO> result;
}
