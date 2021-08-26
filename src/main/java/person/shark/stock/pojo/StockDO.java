package person.shark.stock.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

@Data
public class StockDO implements Serializable {

    @SerializedName("SecuritiesCompanyCode")
    private String id;
    @SerializedName("CompanyName")
    private String name;
    @SerializedName("Close")
    private Double price;

    private Double recentOneYearPayBack;
    private Double recentTwoYearPayBack;
    private Double recentThreeYearPayBack;
    private Double recentFourYearPayBack;
    private Double payBackRate;
    private Double payBackStandardDeviationRate;

}
