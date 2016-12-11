import java.io.Serializable;
import java.util.Date;

/**
 * Created by marro on 06/12/2016.
 */
public interface NotifiableEvent extends Serializable {

    Date getTime();
    Topic getTopic();

}
