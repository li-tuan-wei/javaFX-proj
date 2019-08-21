package ldh.common.testui.constant;

/**
 * Created by ldh123 on 2017/6/21.
 */
public enum ContentType {
    Text("text"),
    TextPlain("text/plain"),
    Json("application/json"),
    ;

    private String content;

    private ContentType(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
