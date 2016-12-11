import java.io.Serializable;

/**
 * Created by marro on 07/12/2016.
 */
public class Topic implements Serializable {

    private String code;

    public Topic(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return (Character.toUpperCase(code.charAt(0)) + code.substring(1).replace('-', ' '));
    }
}
