package tickide.lexin.com.tickide.DataApi;

/**
 * Created by xushun on 2016/11/14.
 */

public class ReturnHex {

    /**
     * output : ddd
     * size : 2782
     * success : true
     * time : 0.068188190460205
     */
    private String output;
    private String size;
    private boolean success;
    private double time;

    public void setOutput(String output) {
        this.output = output;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getOutput() {
        return output;
    }

    public String getSize() {
        return size;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getTime() {
        return time;
    }
}
