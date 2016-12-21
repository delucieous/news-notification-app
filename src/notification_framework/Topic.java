package notification_framework;

import java.io.Serializable;

/*
A class to model a topic that a source can represent. Must be serializable as it will be passed around
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

    //Override equals so we can ensure two separate Topic objects are treated the same if they have the same code
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Topic)) {
            return false;
        }
        else {
            return ((Topic) o).getCode().equals(this.getCode());
        }
    }

    //Hash on the topic code - this corresponds to the way we overrode equals()
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
