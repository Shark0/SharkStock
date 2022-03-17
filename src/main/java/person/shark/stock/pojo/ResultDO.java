package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResultDO implements Serializable {
    private SummaryDetailDO summaryDetail;
    private DefaultKeyStatisticsDO defaultKeyStatistics;
}
