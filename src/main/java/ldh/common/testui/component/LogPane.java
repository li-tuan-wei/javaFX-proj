package ldh.common.testui.component;

import com.google.gson.reflect.TypeToken;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import ldh.common.testui.constant.TestLogType;
import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TestLogData;
import ldh.common.testui.transition.BounceInDownTransition;
import ldh.common.testui.transition.BounceInRightTransition;
import ldh.common.testui.transition.BounceInUpTransition;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.RegionUtil;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlColumnData;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.StyleClassDecoration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class LogPane extends ScrollPane {

    private VBox contentPane = new VBox();
    private int firstSub = 1;
    private int secondSub = 1;
    private int thirdSub = 1;
    private int fourSub = 1;

    public LogPane() {
        this.getStyleClass().add("log-pane");
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setContent(contentPane);
    }

    public void addTitle(String title, GlyphIcon icon) {
        Label titleLabel = new Label(title);
        if (icon != null) titleLabel.setGraphic(icon);
        titleLabel.getStyleClass().add("title");
        HBox hBox = new HBox(titleLabel);
        hBox.setAlignment(Pos.CENTER);
        contentPane.getChildren().add(hBox);
        contentPane.getChildren().add(new Separator());
        BounceInUpTransition transition = new BounceInUpTransition(titleLabel);
        transition.playFromStart();
    }

    public void addData(TestLog testLog) {
        secondSub = 1;
        Label seqLabel = new Label(firstSub++ + "");
        Label titleLabel = new Label(testLog.getName());
        Label headerIcon = new Label();
        GlyphIcon view = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.STEELBLUE);
        headerIcon.setGraphic(view);
        headerIcon.setUserData("1");

        Region region = new Region();

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("header");
//        hBox.setOpacity(0);
        HBox.setHgrow(region, Priority.ALWAYS);
        hBox.getChildren().addAll(seqLabel, titleLabel, region, headerIcon);

        contentPane.getChildren().add(hBox);
//        inUp(hBox, true);
    }

    public void addData(TestLogData testLogData) {
        Label seqLabel = new Label(firstSub + "." + secondSub++ + "");
        Label titleLabel = new Label(testLogData.getName());
        Label headerIcon = new Label();
        GlyphIcon view = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.STEELBLUE);
        headerIcon.setGraphic(view);
        headerIcon.setUserData("1");

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("header-sub");
//        hBox.setOpacity(0);

        hBox.getChildren().addAll(seqLabel, titleLabel, region, headerIcon);

        contentPane.getChildren().add(hBox);
//        inUp(hBox, true);

        headerIcon.setOnMouseClicked(e->{
            VBox vBox = new VBox();
            vBox.setSpacing(5);
            Node node = buildContent(testLogData);
            vBox.getChildren().addAll(node);
            vBox.getStyleClass().addAll("second-content");

            int t = contentPane.getChildren().indexOf(hBox);
            if (t < 0) return;
            if (headerIcon.getUserData().toString().equals("1")) {
                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.MINUS_CIRCLE), 26, Color.STEELBLUE);
                headerIcon.setGraphic(icon);
                headerIcon.setUserData("2");

                contentPane.getChildren().add(t+1, vBox);

//                vBox.setOpacity(0);
//                inRight(vBox, false);
            } else {
                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.STEELBLUE);
                headerIcon.setGraphic(icon);
                headerIcon.setUserData("1");
                contentPane.getChildren().remove(t+1);
            }
        });
    }

    public void addData(TestLogData testLogData, TestLogData testLogData2) {
        secondSub = 1;
        Label seqLabel = new Label(firstSub++ + "");
        Label titleLabel = new Label(testLogData.getName());
        Label headerIcon = new Label();
        GlyphIcon view = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.STEELBLUE);
        headerIcon.setGraphic(view);
        headerIcon.setUserData("1");

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("header");
//        hBox.setOpacity(0);
        hBox.getChildren().addAll(seqLabel, titleLabel, region, headerIcon);

        contentPane.getChildren().add(hBox);
//        inUp(hBox, true);

        headerIcon.setOnMouseClicked(e->{
            VBox vBox = new VBox();
            vBox.setSpacing(5);
            Node d1 = buildContent(testLogData);
            Node d2 = buildContent(testLogData2);
            vBox.getStyleClass().addAll("second-content");

            vBox.getChildren().addAll(d1, d2);
            int t = contentPane.getChildren().indexOf(hBox);
            if (t < 0) return;
            if (headerIcon.getUserData().toString().equals("1")) {
                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.MINUS_CIRCLE), 26, Color.STEELBLUE);
                headerIcon.setGraphic(icon);
                headerIcon.setUserData("2");

                contentPane.getChildren().add(t+1, vBox);

//                vBox.setOpacity(0);
//                inRight(vBox, false);
            } else {
                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.STEELBLUE);
                headerIcon.setGraphic(icon);
                headerIcon.setUserData("1");
                contentPane.getChildren().remove(t+1);
            }
        });
    }

    private void inUp(Node node, boolean isScroll) {
        BounceInUpTransition transition = new BounceInUpTransition(node);
        transition.playFromStart();
        if (!isScroll) return;
        transition.setOnFinished(e->{
            this.setVvalue(1);
        });
    }

    private void inDown(Node node, boolean isScroll) {
        BounceInDownTransition transition = new BounceInDownTransition(node);
        transition.playFromStart();
        if (!isScroll) return;
        transition.setOnFinished(e->{
            this.setVvalue(1);
        });
    }

    private void inRight(Node node, boolean isScroll) {
        node.setOpacity(0);
        BounceInRightTransition transition = new BounceInRightTransition(node);
        transition.playFromStart();
        if (!isScroll) return;
        transition.setOnFinished(e->{
            this.setVvalue(1);
        });
    }

    private GridPane initGridPane(Map<String, Object> paramMap, int columnSize) {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("param-content");
        gridPane.setVgap(5);
        int idx = 0;
        int row =0, column = 0;
        for(Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if (idx%columnSize == 0) {
                column = 0;
                row++;
            }
            idx++;
            Label keyLabel = new Label(entry.getKey());
            TextField valueLabel = new TextField(entry.getValue().toString());
            valueLabel.setEditable(false);

            GridPane.setConstraints(keyLabel, column++, row);
            GridPane.setConstraints(valueLabel, column++, row);
            gridPane.getChildren().addAll(keyLabel, valueLabel);
        }
        for(int i=0; i<columnSize; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(120));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 1000, Priority.ALWAYS, HPos.LEFT, true));
        }
        return gridPane;
    }

    private GridPane initSqlCheckGridPane(SqlCheck sqlCheck, Map<String, SqlColumnData> paramMap, int columnSize) {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("param-content");
        gridPane.setVgap(5);
        int idx = 0;
        int row =0, column = 0;

        if (sqlCheck != null) {
            Label sqlLabel = new Label("sql:");
            TextField sqlTextField = new TextField(sqlCheck.getSql() + ":" + sqlCheck.getArgs());
            sqlTextField.setEditable(false);
            GridPane.setConstraints(sqlLabel, 0, row);
            GridPane.setConstraints(sqlTextField, 1, row, 2, 1);
            gridPane.getChildren().addAll(sqlLabel, sqlTextField);
            row++;column=0;
        }

        for(Map.Entry<String, SqlColumnData> entry : paramMap.entrySet()) {
            if (idx%columnSize == 0) {
                column = 0;
                row++;
            }
            idx++;
            Label keyLabel = new Label(entry.getKey() + ":");
            TextField valueLabel = new TextField(entry.getValue().getValue().toString() + "|"+entry.getValue().getChangedValue().toString());
            Boolean isEqual = entry.getValue().getIsEqual();
            if (isEqual == null) {
                Decorator.addDecoration(valueLabel, new StyleClassDecoration("warning"));
            } else if (isEqual) {
                Decorator.addDecoration(valueLabel, new StyleClassDecoration("success"));
            } else {
                Decorator.addDecoration(valueLabel, new StyleClassDecoration("error"));
            }
            valueLabel.setEditable(false);

            Label descLabel = new Label(entry.getValue().getDesc());
            GridPane.setConstraints(keyLabel, column++, row);
            GridPane.setConstraints(valueLabel, column++, row);
            GridPane.setConstraints(descLabel, column++, row);
            gridPane.getChildren().addAll(keyLabel, valueLabel, descLabel);
        }
        for(int i=0; i<columnSize; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(120));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 300, Priority.ALWAYS, HPos.LEFT, true));
        }
        return gridPane;
    }

    public void delay(double time, Supplier<Object> function) {
        new Thread(()->{
            try {
                Thread.sleep((long)(time * 1000L));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(()->{
                function.get();
            });
        }).start();
    }

    private Node buildContent(TestLogData testLogData) {
        if (testLogData.getType() == TestLogType.http) {
            VBox vBox = new VBox();
            vBox.setSpacing(5);
            Map<String, Object> data = JsonUtil.toObject(testLogData.getContent(), new TypeToken<Map<String, Object>>(){}.getType());
            Map<String, Object> paramMap = data.containsKey("param") ? (Map<String, Object>) data.get("param") : null;
            Map<String, Object> varMap = data.containsKey("var") ? (Map<String, Object>) data.get("var") : null;
            Map<String, Object> headerMap = data.containsKey("header") ? (Map<String, Object>) data.get("header") : null;
            Map<String, Object> cookieMap = data.containsKey("cookie") ? (Map<String, Object>) data.get("cookie") : null;
            String body = data.containsKey("body") ? data.get("body").toString() : null;

            if (paramMap != null && paramMap.size() > 0) {
                Label title = new Label("参数:");
                vBox.getChildren().addAll(title, initGridPane(paramMap, 2));
            }
            if (varMap != null && varMap.size() > 0) {
                vBox.getChildren().addAll(new Label("变量:"), initGridPane(varMap, 2));
            }
            if (headerMap != null && headerMap.size() > 0) {
                vBox.getChildren().addAll(new Label("header:"), initGridPane(headerMap, 2));
            }
            if (cookieMap != null && cookieMap.size() > 0) {
                vBox.getChildren().addAll(new Label("cookie:"), initGridPane(cookieMap, 2));
            }
            if (body != null) {
                TextArea textArea = new TextArea(body);
                textArea.setWrapText(true);
                textArea.setPrefHeight(100);
                textArea.setMinHeight(100);
                vBox.getChildren().addAll(new Label("body:"), textArea);
            }
            return vBox;
        } else if (testLogData.getType() == TestLogType.response) {
            VBox vBox = new VBox();
            String str = testLogData.getContent();
            if (str.startsWith("{") || str.startsWith("[")) {
                str = JsonUtil.parseJson(str);
            }
            TextArea textArea = new TextArea(str);
            textArea.setWrapText(true);
            textArea.setPrefHeight(100);
            textArea.setMinHeight(100);
            vBox.getChildren().addAll(new Label("response:"), textArea);
            return vBox;
        } else if (testLogData.getType() == TestLogType.sqlCheck) {
            if (testLogData.getContent().startsWith("[")) {
                List<Map<String, SqlColumnData>> data = JsonUtil.toObject(testLogData.getContent(), new TypeToken<List<Map<String, SqlColumnData>>>(){}.getType());
                VBox vBox = new VBox();
                vBox.getChildren().add(new Label("DB验证:"));
                for(Map<String, SqlColumnData> map : data) {
                    GridPane gridPane = initSqlCheckGridPane(null, map, 1);
                    vBox.getChildren().add(gridPane);
                }
                return vBox;
            } else {
                String sqlCheckJson = JsonUtil.getElementFromJson(testLogData.getContent(), "sqlCheck");
                SqlCheck sqlCheck = JsonUtil.toObjectExpose(sqlCheckJson, SqlCheck.class);

                String sqlCheckDataJson = JsonUtil.getElementFromJson(testLogData.getContent(), "sqlCheckData");
                List<Map<String, SqlColumnData>> sqlCheckData = JsonUtil.toObject(sqlCheckDataJson, new TypeToken<List<Map<String, SqlColumnData>>>(){}.getType());
                VBox vBox = new VBox();
                vBox.getChildren().add(new Label("DB验证:"));

                for(Map<String, SqlColumnData> map : sqlCheckData) {
                    GridPane gridPane = initSqlCheckGridPane(sqlCheck, map, 1);
                    vBox.getChildren().add(gridPane);
                }
                return vBox;
            }
        }
        return new Label("null");
    }
}
