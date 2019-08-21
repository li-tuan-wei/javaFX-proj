package ldh.common.testui.component;

import com.google.gson.reflect.TypeToken;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import ldh.common.testui.cell.ObjectTreeCell;
import ldh.common.testui.constant.BeanType;
import ldh.common.testui.constant.TestLogType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.TestLogDao;
import ldh.common.testui.dao.TestLogDataDao;
import ldh.common.testui.model.*;
import ldh.common.testui.transition.BounceInUpTransition;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.RegionUtil;
import ldh.common.testui.vo.MethodData;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlCheckData;
import ldh.common.testui.vo.SqlColumnData;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.StyleClassDecoration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by ldh on 2018/12/28.
 */
public class TestLogPane extends BorderPane {

    private VBox contentPane = new VBox();
    private int firstSub = 0;
    private int secondSub = 0;
    private int thirdSub = 0;
    private int fourSub = 0;

    private ScrollPane scrollPane = new ScrollPane();
    private TreeView<TreeValue> treeView = new TreeView<>();
    private TreeItem<TreeValue> root = new TreeItem<>();

    private int type = 1;

    private Map<Integer, Double> titlePosMap = new HashMap();
    private Map<Integer, Node> titleNodeMap = new HashMap();

    public TestLogPane(TestLog testLog) throws SQLException {
        this.getStyleClass().add("log-pane");
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(contentPane);
        this.setCenter(scrollPane);

        treeView.setRoot(root);
        treeView.setShowRoot(false);
        treeView.setCellFactory(new ObjectTreeCell<>(value->{
            return value.getName();
        }));
        treeView.setOnMouseClicked(e->{
            if(e.getClickCount() != 2) return;
            TreeItem<TreeValue> treeItem = treeView.getSelectionModel().getSelectedItem();
            if (treeItem == null) return;
            TreeValue treeValue = treeItem.getValue();
            Double height = titlePosMap.get(treeValue.getId());
            if (height == null) return;
            Double vValue = (height + 100)/(contentPane.getLayoutY() + contentPane.getLayoutBounds().getMaxY());
            scrollPane.setVvalue(vValue);
        });

        this.setLeft(treeView);

        if (testLog.getType().equals(TreeNodeType.Case.name())) {
            type = 2;
        } else if (testLog.getType().equals(TreeNodeType.Test.name())) {
            type = 3;
        } else if (testLog.getType().equals(TreeNodeType.Method.name()) || testLog.getType().equals(TreeNodeType.Http.name())) {
            type = 4;
        }

        handleTestLog(testLog, true, root);
    }

    private void handleTestLog(TestLog testLog, boolean isFirst, TreeItem<TreeValue> treeItem) throws SQLException {
        if (isFirst) {
            addTitle(testLog.getName(), null);
        }
        //处理子节点
        List<TestLog> testLogs = TestLogDao.getByParentId(testLog.getId());
        for(TestLog log : testLogs) {
            TreeItem<TreeValue> child = new TreeItem<>(new TreeValue(log.getName(), log.getId()));
            treeItem.getChildren().add(child);
            addData(log);
            handleTestLog(log, false, child);
        }

        // 处理本节点下面的数据
        List<TestLogData> testLogDataList = TestLogDataDao.getByTestLogId(testLog.getId());
        for (int i=0, l=testLogDataList.size(); i<l; i++) {
            TestLogData d1 = testLogDataList.get(i);
            if (i != l-1) {
                if (testLogDataList.get(i+1).getName().startsWith(d1.getName())
                        && (d1.getType() == TestLogType.http)) {
                    TestLogData d2 = testLogDataList.get(i+1);
                    TreeItem<TreeValue> tt = new TreeItem<>(new TreeValue(d1.getName(), d1.getId()));
                    treeItem.getChildren().add(tt);

                    addData(testLog, d1, d2);
                    i++;
                    continue;
                }
            }
//            TreeItem<String> tt = new TreeItem<>(d1.getName());
//            treeItem.getChildren().add(tt);
            addData(d1);
        }
    }

    private void addTitle(String title, GlyphIcon icon) {
        Label titleLabel = buildLabel(title);
        if (icon != null) titleLabel.setGraphic(icon);
        titleLabel.getStyleClass().add("title");
        HBox hBox = new HBox(titleLabel);
        hBox.setAlignment(Pos.CENTER);
        contentPane.getChildren().add(hBox);
        contentPane.getChildren().add(new Separator());
    }

    private void addData(TestLog testLog) {
        handleSub(testLog);
        Label seqLabel = buildLabel(seq(testLog));
        Label titleLabel = buildLabel(testLog.getName());

        Region region = new Region();

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add(cssClass(testLog));
        HBox.setHgrow(region, Priority.ALWAYS);

        hBox.getChildren().addAll(seqLabel, titleLabel, region);
        contentPane.getChildren().add(hBox);
    }

    private void addData(TestLogData testLogData) {
        int sub = 2;
        sub = testLogData.getType() == TestLogType.method || testLogData.getType() == TestLogType.http ? 1 : sub;
        handleSub(sub);
        Label seqLabel = buildLabel(seqSub(sub));
        Label titleLabel = buildLabel(testLogData.getName());
        Label headerIcon = buildLabel("");
        GlyphIcon view = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6));
        headerIcon.setGraphic(view);
        headerIcon.setUserData("1");

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add(cssClass(sub));
//        hBox.setOpacity(0);

        hBox.getChildren().addAll(seqLabel, titleLabel, region);

        Node node = buildContent(testLogData);
        node.getStyleClass().addAll(dataCssClass(2));
        contentPane.getChildren().addAll(hBox, node);
//        inUp(hBox, true);

//        headerIcon.setOnMouseClicked(e->{
//            VBox vBox = new VBox();
//            vBox.setSpacing(5);
//            Node node = buildContent(testLogData);
//            vBox.getChildren().addAll(node);
//            vBox.getStyleClass().addAll(dataCssClass(2));
//
//            int t = contentPane.getChildren().indexOf(hBox);
//            if (t < 0) return;
//            if (headerIcon.getUserData().toString().equals("1")) {
//                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.MINUS_CIRCLE), 26, Color.gray(0.6));
//                headerIcon.setGraphic(icon);
//                headerIcon.setUserData("2");
//
//                contentPane.getChildren().add(t+1, vBox);
//            } else {
//                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6));
//                headerIcon.setGraphic(icon);
//                headerIcon.setUserData("1");
//                contentPane.getChildren().remove(t+1);
//            }
//        });
    }

    private void addData(TestLog testLog, TestLogData testLogData, TestLogData testLogData2) {
        handleSub(1);
        Label seqLabel = buildLabel(seqSub(1));
        Label titleLabel = buildLabel(testLogData.getName());

        Label headerIcon = buildLabel("");
        GlyphIcon view = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6));
        headerIcon.setGraphic(view);
        headerIcon.setUserData("1");

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add(cssClass(1));
        hBox.getChildren().addAll(seqLabel, titleLabel, region);

        VBox vBox = new VBox();
        vBox.setSpacing(5);
        Node d1 = buildContent(testLogData);
        Node d2 = buildContent(testLogData2);
        vBox.getStyleClass().addAll(dataCssClass(1));

        vBox.getChildren().addAll(d1, d2);

        contentPane.getChildren().addAll(hBox, vBox);
        titleNodeMap.put(testLogData.getId(), hBox);
//        headerIcon.setOnMouseClicked(e->{
//            int t = contentPane.getChildren().indexOf(hBox);
//            if (t < 0) return;
//            if (headerIcon.getUserData().toString().equals("1")) {
//                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.MINUS_CIRCLE), 26, Color.gray(0.6));
//                headerIcon.setGraphic(icon);
//                headerIcon.setUserData("2");
//                contentPane.getChildren().add(t+1, vBox);
//            } else {
//                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6));
//                headerIcon.setGraphic(icon);
//                headerIcon.setUserData("1");
//                contentPane.getChildren().remove(t+1);
//            }
//        });
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
            Label keyLabel = buildLabel(entry.getKey());
            Object value = entry.getValue();
            TextField valueLabel = new TextField(value == null ? "null" : value.toString());
            valueLabel.setEditable(false);

            GridPane.setConstraints(keyLabel, column++, row);
            GridPane.setConstraints(valueLabel, column++, row);
            gridPane.getChildren().addAll(keyLabel, valueLabel);
        }
        for(int i=0; i<columnSize; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(200));
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
            Label sqlLabel = buildLabel("sql:");
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
            Label keyLabel = buildLabel(entry.getKey() + ":");
            TextField valueLabel = new TextField(entry.getValue().getValue().toString() + "|"+entry.getValue().getChangedValue().toString());
            Boolean isEqual = entry.getValue().getIsEqual();
            decoratorNode(valueLabel, isEqual);
            valueLabel.setEditable(false);

            Label descLabel = buildLabel(entry.getValue().getDesc());
            GridPane.setConstraints(keyLabel, column++, row);
            GridPane.setConstraints(valueLabel, column++, row);
            GridPane.setConstraints(descLabel, column++, row);
            gridPane.getChildren().addAll(keyLabel, valueLabel, descLabel);
        }
        for(int i=0; i<columnSize; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(200));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 300, Priority.ALWAYS, HPos.LEFT, true));
        }
        return gridPane;
    }

    private GridPane initSqlCheckGridPane(SqlCheck sqlCheck, List<SqlCheckData> sqlCheckDataList, int columnSize) {
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("param-content");
        gridPane.setVgap(5);
        int idx = 0;
        int row =0, column = 0;

        if (sqlCheck != null) {
            Label sqlLabel = buildLabel("sql:");
            TextField sqlTextField = new TextField(sqlCheck.getSql() + ":" + sqlCheck.getArgs());
            sqlTextField.setEditable(false);
            GridPane.setConstraints(sqlLabel, 0, row);
            GridPane.setConstraints(sqlTextField, 1, row, 2, 1);
            gridPane.getChildren().addAll(sqlLabel, sqlTextField);
            row++;column=0;
        }

        int num = 0;
        for (SqlCheckData sqlCheckData : sqlCheckDataList) {
            Map<String, SqlColumnData> dataMap = sqlCheckData.getDataMap();
            Label seqLabel = buildLabel("check " + ++num);
            Separator separator = new Separator();
            StackPane stackPane = new StackPane(separator, seqLabel);
            stackPane.setAlignment(Pos.CENTER_LEFT);
            gridPane.add(stackPane, 0, row++, 3, 1);

            for(Map.Entry<String, SqlColumnData> entry : dataMap.entrySet()) {
                if (idx%columnSize == 0) {
                    column = 0;
                    row++;
                }
                idx++;
                Label keyLabel = buildLabel(entry.getKey() + ":");
                String changedValue = entry.getValue().getChangedValue() == null ? "null" : entry.getValue().getChangedValue().toString();
                String value = entry.getValue().getValue() != null ? entry.getValue().getValue().toString() + "|"+ changedValue : "" + "|"+ changedValue;
                TextField valueLabel = new TextField(value);
                Boolean isEqual = entry.getValue().getIsEqual();
                decoratorNode(valueLabel, isEqual);
                valueLabel.setEditable(false);

                Label descLabel = buildLabel(entry.getValue().getDesc());
                GridPane.setConstraints(keyLabel, column++, row);
                GridPane.setConstraints(valueLabel, column++, row);
                GridPane.setConstraints(descLabel, column++, row);
                gridPane.getChildren().addAll(keyLabel, valueLabel, descLabel);
                row++;
            }
        }

        for(int i=0; i<columnSize; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(200));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 300, Priority.ALWAYS, HPos.LEFT, true));
        }
        return gridPane;
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

            if (varMap != null && varMap.size() > 0) {
                VBox paramBox = new VBox();

                Label title = buildLabel("变量:");
                Label headerIcon = buildLabel("");
                headerIcon.setGraphic(RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6)));
                headerIcon.setUserData("1");
                Region region = new Region();
                HBox.setHgrow(region, Priority.ALWAYS);
                HBox hBox = new HBox(title, region, headerIcon);

                paramBox.getChildren().addAll(hBox);

                vBox.getChildren().addAll(paramBox);

                headerIcon.setOnMouseClicked(e->{
                    GridPane gridPane = initGridPane(varMap, 2);

                    if (headerIcon.getUserData().toString().equals("1")) {
                        GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.MINUS_CIRCLE), 26, Color.gray(0.6));
                        headerIcon.setGraphic(icon);
                        headerIcon.setUserData("2");

                        paramBox.getChildren().add(1, gridPane);
                    } else {
                        GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6));
                        headerIcon.setGraphic(icon);
                        headerIcon.setUserData("1");
                        paramBox.getChildren().remove(1);
                    }

                });
            }
            if (paramMap != null && paramMap.size() > 0) {
                vBox.getChildren().addAll(buildLabel("参数:"), initGridPane(paramMap, 2));
            }
            if (headerMap != null && headerMap.size() > 0) {
                vBox.getChildren().addAll(buildLabel("header:"), initGridPane(headerMap, 2));
            }
            if (cookieMap != null && cookieMap.size() > 0) {
                vBox.getChildren().addAll(buildLabel("cookie:"), initGridPane(cookieMap, 2));
            }
            if (body != null) {
                TextArea textArea = new TextArea(body);
                textArea.setWrapText(true);
                textArea.setPrefHeight(100);
                textArea.setMinHeight(100);
                vBox.getChildren().addAll(buildLabel("body:"), textArea);
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
            vBox.getChildren().addAll(buildLabel("response:"), textArea);
            return vBox;
        } else if (testLogData.getType() == TestLogType.sqlCheck) {
            if (testLogData.getContent().startsWith("[")) {
                List<Map<String, SqlColumnData>> data = JsonUtil.toObject(testLogData.getContent(), new TypeToken<List<Map<String, SqlColumnData>>>(){}.getType());
                VBox vBox = new VBox();
                vBox.getChildren().add(buildLabel("DB验证:"));
                for(Map<String, SqlColumnData> map : data) {
                    GridPane gridPane = initSqlCheckGridPane(null, map, 1);
                    vBox.getChildren().add(gridPane);
                }
                return vBox;
            } else {
                String sqlCheckJson = JsonUtil.getElementFromJson(testLogData.getContent(), "sqlCheck");
                SqlCheck sqlCheck = JsonUtil.toObjectExpose(sqlCheckJson, SqlCheck.class);

                String sqlCheckDataJson = JsonUtil.getElementFromJson(testLogData.getContent(), "sqlCheckData");
                List<SqlCheckData> sqlCheckDataList = JsonUtil.toObjectExpose(sqlCheckDataJson, new TypeToken<List<SqlCheckData>>(){}.getType());
                VBox vBox = new VBox();
                vBox.setSpacing(10);
                vBox.getChildren().add(buildLabel("DB验证:"));

                GridPane gridPane = initSqlCheckGridPane(sqlCheck, sqlCheckDataList, 1);
                vBox.getChildren().add(gridPane);
                return vBox;
            }
        }  else if (testLogData.getType() == TestLogType.method) {
            Map<String, Object> map = JsonUtil.toObject(testLogData.getContent(), new TypeToken<Map<String, Object>>() {}.getType());
            Map<String, Object> paramMap = map.containsKey("param") ? (Map<String, Object>) map.get("param") : null;
            String content = JsonUtil.getElementFromJson(testLogData.getContent(), "data");
            String methodContent = JsonUtil.getElementFromJson(testLogData.getContent(), "method");

            VBox vBox = new VBox();
            vBox.setSpacing(5);

            Node node = createExpandPane("参数", vBox, (Void)->{
                GridPane paramGridPane = initGridPane(paramMap, 2);
                return  paramGridPane;
            });
            List<TestMethodData> testMethodDataList = JsonUtil.toObject(content, new TypeToken<List<TestMethodData>>() {}.getType());
            TestMethod testMethod = JsonUtil.toObject(methodContent, TestMethod.class);
            Label title2 = new Label("测试方法：");
            Region dataMapGridPane = initMethodGridPane(testMethod, testMethodDataList);

            vBox.getChildren().addAll(node, title2, dataMapGridPane);
            return vBox;
        } else if (testLogData.getType() == TestLogType.bean) {
            Map<String, Object> map = JsonUtil.toObject(testLogData.getContent(), new TypeToken<Map<String, Object>>() {}.getType());
            Map<String, Object> paramMap = map.containsKey("param") ? (Map<String, Object>) map.get("param") : null;
            String content = JsonUtil.getElementFromJson(testLogData.getContent(), "_content");
//            System.out.println("content:" + content);
            BeanCheck beanCheck = content != null ? (BeanCheck) JsonUtil.toObject(content, BeanCheck.class) : null;
            VBox vBox = initBeanCheckGridPane(paramMap, beanCheck);
            return vBox;
        }
        return buildLabel("null");
    }

    private VBox initBeanCheckGridPane(Map<String, Object> paramMap, BeanCheck beanCheck) {
        VBox vBox = new VBox();
        vBox.setSpacing(10);

        Node node = createExpandPane("参数:", vBox, (Void)->{
            GridPane paramGridPane = initGridPane(paramMap, 2);
            return paramGridPane;
        });

        Label title2 = new Label("验证对象");
        GridPane gridPane = new GridPane();
        if (beanCheck.getBeanType() == BeanType.Json || beanCheck.getBeanType() == BeanType.EL) {
            Set<BeanData> beanDataSet = beanCheck.getBeanDatas();
            gridPane.getStyleClass().add("param-content");
            gridPane.setVgap(5);
            int row =0, column = 0;

            Label sqlLabel = buildLabel("bean对象:");
            TextField beanTextField = new TextField(beanCheck.getCheckName());
            beanTextField.setEditable(false);

            GridPane.setConstraints(sqlLabel, 0, row);
            GridPane.setConstraints(beanTextField, 1, row, 2, 1);
            gridPane.getChildren().addAll(sqlLabel, beanTextField);
            row++;column=0;

            for (BeanData beanData : beanDataSet) {
                Label keyLabel = buildLabel(beanData.getCheckName() + ":");
                keyLabel.setTooltip(new Tooltip(beanData.getCheckName()));

                TextField valueLabel = new TextField(beanData.getExceptedValue() + "|"+beanData.getValue());
                if (beanCheck.getBeanType() == BeanType.EL) {
                    valueLabel.setText(beanData.getCheckName());
                }

                Boolean isEqual = beanData.getSuccess();
                decoratorNode(valueLabel, isEqual);
                valueLabel.setEditable(false);

                Label descLabel = buildLabel(beanData.getDesc());
                GridPane.setConstraints(keyLabel, column++, row);
                GridPane.setConstraints(valueLabel, column++, row);
                GridPane.setConstraints(descLabel, column++, row);
                gridPane.getChildren().addAll(keyLabel, valueLabel, descLabel);
                row++;
                column=0;
            }

            gridPane.getColumnConstraints().add(new ColumnConstraints(200));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 300, Priority.ALWAYS, HPos.LEFT, true));
        } else if (beanCheck.getBeanType() == BeanType.Object) {
            List<Map<String, BeanData>> beanDataList = beanCheck.getBeanDatasForObject();
            gridPane.getStyleClass().add("param-content");
            gridPane.setVgap(5);
            int row =0, column = 0;

            Label sqlLabel = buildLabel("bean对象:");
            TextField beanTextField = new TextField(beanCheck.getCheckName());
            beanTextField.setEditable(false);

            GridPane.setConstraints(sqlLabel, 0, row);
            GridPane.setConstraints(beanTextField, 1, row, 2, 1);
            gridPane.getChildren().addAll(sqlLabel, beanTextField);
            row++;column=0;

            for (Map<String, BeanData> map : beanDataList) {
                for (Map.Entry<String, BeanData> entry : map.entrySet()) {
                    BeanData beanData = entry.getValue();
                    if (beanData.getExceptedValue() == null || beanData.getExceptedValue().equals("")) {
                        continue;
                    }
                    Label keyLabel = buildLabel(beanData.getCheckName() + ":");
                    TextField valueLabel = new TextField(beanData.getExceptedValue() + "|"+beanData.getValue());
                    Boolean isEqual = beanData.getSuccess();
                    decoratorNode(valueLabel, isEqual);
                    valueLabel.setEditable(false);

                    Label descLabel = buildLabel(beanData.getDesc());
                    GridPane.setConstraints(keyLabel, column++, row);
                    GridPane.setConstraints(valueLabel, column++, row);
                    GridPane.setConstraints(descLabel, column++, row);
                    gridPane.getChildren().addAll(keyLabel, valueLabel, descLabel);
                    row++;
                    column=0;
                }
            }

            gridPane.getColumnConstraints().add(new ColumnConstraints(200));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 300, Priority.ALWAYS, HPos.LEFT, true));
        } else if (beanCheck.getBeanType() == BeanType.String) {
            if (beanCheck.getContent().startsWith("{")) {
                BeanData beanData = JsonUtil.toObject(beanCheck.getContent(), BeanData.class);
                gridPane.getStyleClass().add("param-content");
                gridPane.setVgap(5);
                int row =0, column = 0;

                Label sqlLabel = buildLabel("bean对象:");
                TextField beanTextField = new TextField(beanCheck.getCheckName());
                beanTextField.setEditable(false);

                GridPane.setConstraints(sqlLabel, 0, row);
                GridPane.setConstraints(beanTextField, 1, row, 2, 1);
                gridPane.getChildren().addAll(sqlLabel, beanTextField);
                row++;column=0;

                Label keyLabel = buildLabel(beanData.getCheckName() + ":");
                keyLabel.setTooltip(new Tooltip(beanData.getCheckName()));

                TextField valueLabel = new TextField(beanData.getExceptedValue() + "|"+beanData.getValue());

                Boolean isEqual = beanData.getSuccess();
                decoratorNode(valueLabel, isEqual);
                valueLabel.setEditable(false);

                Label descLabel = buildLabel(beanData.getDesc());
                GridPane.setConstraints(keyLabel, column++, row);
                GridPane.setConstraints(valueLabel, column++, row);
                GridPane.setConstraints(descLabel, column++, row);
                gridPane.getChildren().addAll(keyLabel, valueLabel, descLabel);
                row++;

                gridPane.getColumnConstraints().add(new ColumnConstraints(200));
                gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
                gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 300, Priority.ALWAYS, HPos.LEFT, true));
            }

        }

        vBox.getChildren().addAll(node, title2, gridPane);
        return vBox;
    }

    private VBox initMethodGridPane(TestMethod testMethod, List<TestMethodData> testMethodDataList) {
        VBox vBox = new VBox();
        for (TestMethodData testMethodData : testMethodDataList) {
            Label titleLabel = buildLabel(testMethodData.getTestName());
            vBox.getChildren().add(titleLabel);

            Map<String, MethodData> dataMap = JsonUtil.toObject(testMethodData.getData(), new TypeToken<Map<String, MethodData>>(){}.getType());
            GridPane gridPane = new GridPane();
            gridPane.getStyleClass().add("param-content");
            gridPane.setVgap(5);
            int row=0, column = 0;

            gridPane.add(buildLabel("测试类："), column++, row);
            gridPane.add(new TextField(testMethod.getClassName()), column++, row);
            column=0; row++;

            gridPane.add(buildLabel("测试方法："), column++, row);
            gridPane.add(new TextField(testMethod.getMethodName()), column++, row);
            column=0; row++;

            MethodData desc = dataMap.get("testName");
            if (desc != null) {
                gridPane.add(buildLabel("测试说明："), column++, row);
                gridPane.add(buildLabel(desc.getData()), column++, row);
                column=0; row++;
            }
            Boolean isEqual = null;

            for(Map.Entry<String, MethodData> entry : dataMap.entrySet()) {
                Label label = buildLabel(entry.getValue().getKey());
                if (entry.getValue().getData() == null || entry.getValue().getData().equals("")
                        || entry.getKey().equals("testName") || entry.getKey().equals("check")) continue;
                TextField textField = new TextField(entry.getValue().getData());

                decoratorNode(textField, isEqual);

                gridPane.add(label, column++, row);
                gridPane.add(textField, column++, row);

                row++;
                column=0;
            }
            vBox.getChildren().addAll(gridPane);

            gridPane.getColumnConstraints().add(new ColumnConstraints(200));
            gridPane.getColumnConstraints().add(new ColumnConstraints(100, 150, 3500, Priority.ALWAYS, HPos.LEFT, true));
        }
        return vBox;
    }

    private void decoratorNode(TextField textField, Boolean isEqual) {
        if (isEqual == null) {
            Decorator.addDecoration(textField, new StyleClassDecoration("warn"));
        } else if (isEqual) {
            Decorator.addDecoration(textField, new StyleClassDecoration("success"));
        } else {
            Decorator.addDecoration(textField, new StyleClassDecoration("error"));
        }
    }

    private void handleSub(TestLog testLog) {
        if (testLog == null) return;
        if (type == 1) {
            if (testLog.getType().equals(TreeNodeType.Case.name())) {
                firstSub++;
                secondSub = 0;
                thirdSub = 0;
                fourSub = 0;
            } else if (testLog.getType().equals(TreeNodeType.Test.name())) {
                secondSub++;
                thirdSub = 0;
                fourSub = 0;
            }
        } else if (type == 2) {
            firstSub++;
            secondSub = 0;
            thirdSub = 0;
            fourSub = 0;
        } else if (type == 3) {
            firstSub++;
            secondSub = 0;
        }
    }

    private void handleSub(int level) {
        if (type == 1) {

        } else if (type == 2) {
            if (level == 1) {
                secondSub++;
                thirdSub = 0;
                fourSub = 0;
            } else {
                thirdSub++;
                fourSub = 0;
            }
        } else if (type == 3) {
            if (level == 1) {
                firstSub++;
                secondSub = 0;
            } else {
                secondSub++;
            }
        } else if (type == 4) {
            if (level == 1) {
                firstSub++;
                secondSub = 0;
            } else {
                secondSub++;
            }
        }
    }

    private String seq(TestLog testLog) {
        if (type == 1) {
            if (testLog.getType().equals(TreeNodeType.Case.name())) {
                return firstSub + "";
            } else if (testLog.getType().equals(TreeNodeType.Test.name())) {
                return firstSub + "." + secondSub;
            }
        } else if (type == 2) {
            return firstSub + "";
        }

        return "";
    }

    private String seqSub(int level) {
        if (type == 1) {
            return level == 1 ? firstSub + "." + secondSub + "." + thirdSub : firstSub + "." + secondSub + "." + thirdSub + "." + thirdSub;
        } else if (type == 2) {
            return level == 1 ? firstSub + "." + secondSub : firstSub + "." + secondSub + "." + thirdSub;
        } else if (type == 3) {
            return level == 1 ? firstSub + "" :  firstSub + "." + secondSub;
        } else if (type == 4) {
            return level == 1 ? firstSub + "" :  firstSub + "." + secondSub;
        }
        return null;
    }

    private String cssClass(TestLog testLog) {
        if (type == 1) {
            return "header";
        } else if (type == 2) {
            return "first-sub";
        } else if (type == 3) {
            return "second-sub";
        } else if (type == 4) {
            return "third-sub";
        }
        return "";
    }

    private String cssClass(int level) {
        if (type == 1) {
            return level == 1 ? "first-sub" : "four-sub";
        } else if (type == 2) {
            return level == 1 ? "second-sub" : "third-sub";
        } else if (type == 3) {
            return level == 1 ? "first-sub" : "second-sub";
        } else if (type == 4) {
            return level == 1 ? "first-sub" : "second-sub";
        }
        return "";
    }

    private String dataCssClass(int level) {
        if (type == 1) {
            return level == 1 ? "third-sub" : "four-sub";
        } else if (type == 2) {
            return level == 1 ? "data-second-sub" : "data-third-sub";
        } else if (type == 3) {
            return level == 1 ? "data-first-sub" : "data-second-sub";
        } else if (type == 4) {
            return level == 1 ? "data-first-sub" : "data-second-sub";
        }
        return "";
    }

    private Label buildLabel(String text) {
        Label label = new Label(text);
        if (!StringUtils.isEmpty(text)) {
            label.setTooltip(new Tooltip(text));
        }

        return label;
    }

    private Node createExpandPane(String titleName, Pane pane, Function<?, Node> function) {
        Label title = buildLabel(titleName);
        Label headerIcon = buildLabel("");
        headerIcon.setGraphic(RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6)));
        headerIcon.setUserData("1");
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        HBox hBox = new HBox(title, region, headerIcon);
        headerIcon.setOnMousePressed(e->{
            if (headerIcon.getUserData().toString().equals("1")) {
                Node node = function.apply(null);
                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.MINUS_CIRCLE), 26, Color.gray(0.6));
                headerIcon.setGraphic(icon);
                headerIcon.setUserData("2");

                pane.getChildren().add(1, node);
            } else {
                GlyphIcon icon = RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE), 26, Color.gray(0.6));
                headerIcon.setGraphic(icon);
                headerIcon.setUserData("1");
                pane.getChildren().remove(1);
            }
        });
        return hBox;
    }

    public void calcPos() {
        if (titlePosMap.size() > 0) return;
        titleNodeMap.forEach((key, value)->{
            Double height = value.getLayoutY() + value.getLayoutBounds().getMinY();
            titlePosMap.put(key, height);
        });
    }

    @Data
    private class TreeValue {
        private String name;
        private Integer id;
        public TreeValue(String name, Integer id) {
            this.name = name;
            this.id = id;
        }
    }

}
