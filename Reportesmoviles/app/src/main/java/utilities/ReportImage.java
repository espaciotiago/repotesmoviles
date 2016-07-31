package utilities;

import java.io.Serializable;

/**
 * Created by smartin on 27/06/2016.
 */
public class ReportImage implements Serializable {
    private String reportId,image;

    public ReportImage(String reportId, String image) {
        this.reportId = reportId;
        this.image = image;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
