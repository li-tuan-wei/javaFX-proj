package ldh.common.testui.model;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import ldh.common.testui.constant.BeanType;
import ldh.common.testui.util.JsonUtil;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class BeanCheck {

    @Expose
    private Integer id;
    @Expose
    private String checkName;
    @Expose
    private BeanType beanType;
    @Expose
    private String otherInfo;
    @Expose
    private Integer treeNodeId;
    @Expose
    private String columns;
    @Expose
    private String content;

    public List<String> getColumnNames() {
        if (columns != null) {
            return Arrays.stream(columns.split(",")).collect(Collectors.toList());
        }
        return new ArrayList();
    }

    public Set<BeanData> getBeanDatas() {  // json
        if (content != null && content.startsWith("[")) {
            Set<BeanData> result = JsonUtil.toObject(content, new TypeToken<Set<BeanData>>() {}.getType());
            result = result.stream().sorted(Comparator.comparing(BeanData::getIndex, (x, y)->{
                return x.compareTo(y);
            })).collect(Collectors.toSet());
            return result;
        }
        return new TreeSet<BeanData>();
    }

    public List<Map<String, BeanData>> getBeanDatasForObject() {
        if (content != null && content.startsWith("[")) {
            List<Map<String, BeanData>> lists = JsonUtil.toObject(content, new TypeToken<List<Map<String, BeanData>>>() {}.getType());
            lists.sort(new Comparator<Map<String, BeanData>>() {
                @Override
                public int compare(Map<String, BeanData> o1, Map<String, BeanData> o2) {
                    int index1 = o1.values().iterator().next().getIndex();
                    int index2 = o2.values().iterator().next().getIndex();
                    return index1 - index2;
                }
            });
            return lists;
        }
        return new ArrayList<Map<String, BeanData>>();
    }

    public Integer getNextIndexForBeanData(Collection<BeanData> beanDatas) {
        int maxIndex = 0;
        for (BeanData beanData : beanDatas) {
            if (maxIndex < beanData.getIndex()) {
                maxIndex = beanData.getIndex();
            }
        }
        return maxIndex + 1;
    }

    public Integer getNextIndexForObject(List<Map<String, BeanData>> beanDatas) {
        int maxIndex = 0;
        for (Map<String, BeanData> beanData : beanDatas) {
            if (maxIndex < beanData.values().iterator().next().getIndex()) {
                maxIndex = beanData.values().iterator().next().getIndex();
            }
        }
        return maxIndex + 1;
    }

    public int removeObjectData(List<Map<String, BeanData>> datas,  int index) {
        Map<String, BeanData> data = null;
        int idx = -1;
        for (Map<String, BeanData> map : datas) {
            idx++;
            if (map.values().iterator().next().getIndex() == index) {
                data = map;
                break;
            }
        }
        if (data == null) {
            throw new RuntimeException("数据错误");
        }
        datas.remove(data);
        return idx;
    }
}