package ldh.common.testui.vo;

import com.google.gson.annotations.Expose;
import ldh.common.testui.constant.MethodType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/3/23.
 */
@Data
@AllArgsConstructor
public class MethodData {

    private Integer id;
    private String key;
    private String data;
    private MethodType methodType;
    private String className;
    private String convert;
}
