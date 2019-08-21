package ldh.common.testui.demo;

import lombok.Data;

@Data
public class TAddress<T> {

    private T bean;
    private String name;

    public TAddress(T a) {
        this.bean = a;
    }
}
