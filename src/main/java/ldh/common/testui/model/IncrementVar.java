package ldh.common.testui.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ldh on 2018/3/27.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncrementVar {

    private Integer id;
    private String name;
    private Long value;
    private Integer step;

}
