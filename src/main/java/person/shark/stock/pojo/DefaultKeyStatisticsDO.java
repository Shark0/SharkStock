package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

@Data
public class DefaultKeyStatisticsDO implements Serializable {

    @SerializedName("forwardPE")
    private RawDO forwardPe;

    @SerializedName("trailingEps")
    private RawDO trailingEps;

    @SerializedName("forwardEps")
    private RawDO forwardEps;
}
