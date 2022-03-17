package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

@Data
public class SummaryDetailDO implements Serializable {
    private RawDO dividendRate;

    private RawDO dividendYield;

    @SerializedName("trailingPE")
    private RawDO trailingPe;
}
