package ldh.common.testui.assist.convert;

import ldh.common.testui.util.DateUtil;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by ldh on 2018/3/23.
 */
public class ConvertFactory {

    private static final ConvertFactory instance = new ConvertFactory();

    private Map<String, Convert> convertMap = new HashMap<>();

    private ConvertFactory() {
        convertMap.putIfAbsent("Boolean", Boolean::valueOf);
        convertMap.putIfAbsent("boolean", Boolean::valueOf);
        convertMap.putIfAbsent("Integer", Integer::valueOf);
        convertMap.putIfAbsent("int", Integer::valueOf);
        convertMap.putIfAbsent("Short", Short::valueOf);
        convertMap.putIfAbsent("short", Short::valueOf);
        convertMap.putIfAbsent("String", String::valueOf);
        convertMap.putIfAbsent("Long", Long::valueOf);
        convertMap.putIfAbsent("long", Long::valueOf);
        convertMap.putIfAbsent("Double", Double::valueOf);
        convertMap.putIfAbsent("double", Double::valueOf);
        convertMap.putIfAbsent("Float", Float::valueOf);
        convertMap.putIfAbsent("float", Float::valueOf);
        convertMap.putIfAbsent("Byte", Byte::valueOf);
        convertMap.putIfAbsent("byte", Byte::valueOf);
        convertMap.putIfAbsent("Json",  new JsonConvert());
        convertMap.putIfAbsent("Date(yyyy-MM-dd)", (str)-> DateUtil.parse(str));
        convertMap.putIfAbsent("Date", (str)-> DateUtil.parse(str, "yyyy-MM-dd HH:mm:ss"));
        convertMap.putIfAbsent("Enum",  new EnumConvert());
        convertMap.putIfAbsent("Class",  new ClassConvert());
        convertMap.putIfAbsent("Bean",  object->object);
        convertMap.putIfAbsent("Null",  (str)->null);
        convertMap.putIfAbsent("BigDecimal", BigDecimal::new);
    }

    public static ConvertFactory getInstance() {
        return instance;
    }

    public Convert put(String key, Convert convert) {
        return  convertMap.putIfAbsent(key, convert);
    }

    public Convert get(String key) {
        return convertMap.get(key);
    }

    public Map<String, Convert> getConvertMap() {
        return convertMap;
    }

}
