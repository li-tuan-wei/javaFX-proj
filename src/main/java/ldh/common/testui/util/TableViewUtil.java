package ldh.common.testui.util;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Callback;
import ldh.common.testui.cell.ObjectTableCellFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ldh on 2018/12/13.
 */
public class TableViewUtil {

    private static final Logger LOGGER = Logger.getLogger(TableViewUtil.class.getName());

    public static void alignment(Pos alignment, TableView tableView, String... excludeTableColumnNames) {
        ObservableList<TableColumn> tableColumns = tableView.getColumns();
        for (TableColumn tableColumn : tableColumns) {
            boolean isExclude = false;
            for (String excludeTableColumnName : excludeTableColumnNames) {
                if (tableColumn.getText().equals(excludeTableColumnName)) {
                    isExclude = true;
                    break;
                }
            }
            if (!isExclude) {
                tableColumn.setCellFactory(new ObjectTableCellFactory(e -> e, alignment));
            }
        }
    }

    /**
     * Auto-sizes table view columns to fit its contents.
     *
     * @param tableView The table view in which to resize all columns.
     * @note This is not a column resize policy and does not prevent manual
     * resizing after this method has been called.
     */
    public static void autoSizeTableViewColumns(final TableView<?> tableView) {
        autoSizeTableViewColumns(tableView, -1, -1);
    }

    /**
     * Auto-sizes table view columns to fit its contents.
     *
     * @param tableView The table view in which to resize all columns.
     * @param minWidth  Minimum desired width of text for all columns.
     * @param maxWidth  Maximum desired width of text for all columns.
     * @note This is not a column resize policy and does not prevent manual
     * resizing after this method has been called.
     */
    public static void autoSizeTableViewColumns(final TableView<?> tableView, int minWidth, int maxWidth) {
        TableViewSkin<?> skin = (TableViewSkin<?>) tableView.getSkin();

        if (skin == null) {
            LOGGER.warning(tableView + " skin is null.");
            return;
        }

        TableHeaderRow headerRow = skin.getTableHeaderRow();
        NestedTableColumnHeader rootHeader = headerRow.getRootHeader();
        for (Node node : rootHeader.getChildrenUnmodifiable()) {
            if (node instanceof TableColumnHeader) {
                TableColumnHeader columnHeader = (TableColumnHeader) node;
                try {
                    autoSizeTableViewColumn(columnHeader, minWidth, maxWidth, -1);
                } catch (Throwable e) {
                    e = e.getCause();
                    LOGGER.log(Level.WARNING, "Unable to automatically resize tableView column.", e);
                }
            }
        }
    }

    /**
     * Auto-sizes table view columns to fit its contents.
     *
     * @param column   The column to resize.
     * @param minWidth Minimum desired width of text for this column. Use -1 for no minimum
     *                 width.
     * @param maxWidth Maximum desired width of text for this column. Use -1 for no maximum
     *                 width.
     * @param maxRows  Maximum number of rows to examine for auto-resizing. Use -1
     *                 for all rows.
     * @note This is not a column resize policy and does not prevent manual
     * resizing after this method has been called.
     */
    public static void autoSizeTableViewColumn(TableColumn<?, ?> column, int minWidth, int maxWidth, int maxRows) {
        TableView<?> tableView = column.getTableView();
        TableViewSkin<?> skin = (TableViewSkin<?>) tableView.getSkin();

        if (skin == null) {
            LOGGER.warning(tableView + " skin is null.");
            return;
        }

        TableHeaderRow headerRow = skin.getTableHeaderRow();
        NestedTableColumnHeader rootHeader = headerRow.getRootHeader();
        for (Node node : rootHeader.getChildrenUnmodifiable()) {
            if (node instanceof TableColumnHeader) {
                TableColumnHeader columnHeader = (TableColumnHeader) node;
                if (columnHeader.getTableColumn().equals(column)) {
                    autoSizeTableViewColumn(columnHeader, minWidth, maxWidth, maxRows);
                }
            }
        }
    }

    /**
     * Auto-sizes a table view column to fit its contents.
     *
     * @param header   The column to resize.
     * @param minWidth Minimum desired width of text for this column. Use -1 for no minimum
     *                 width.
     * @param maxWidth Maximum desired width of text for this column. Use -1 for no maximum
     *                 width.
     * @param maxRows  Maximum number of rows to examine for auto-resizing. Use -1
     *                 for all rows.
     * @note This is not a column resize policy and does not prevent manual
     * resizing after this method has been called.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void autoSizeTableViewColumn(TableColumnHeader header, int minWidth, int maxWidth, int maxRows) {
        TableColumn<?, ?> col = (TableColumn<?, ?>) header.getTableColumn();
        if (col != null) {
            List<?> items = col.getTableView().getItems();
            if (items == null || items.isEmpty())
                return;

            Callback cellFactory = col.getCellFactory();
            if (cellFactory == null)
                return;

            TableCell cell = (TableCell) cellFactory.call(col);
            if (cell == null)
                return;

            // set this property to tell the TableCell we want to know its actual
            // preferred width, not the width of the associated TableColumn
            cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);

            // determine cell padding
            double padding = 10;
            Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
            if (n instanceof Region) {
                Region r = (Region) n;
                padding = r.getInsets().getLeft() + r.getInsets().getRight();
            }

            int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);

            double desiredWidth = 0;

            // Check header
            Label headerLabel = (Label) header.lookup(".label");
            String headerText = headerLabel.getText();
            if (!headerLabel.getContentDisplay().equals(ContentDisplay.GRAPHIC_ONLY) && headerText != null) {
                Text text = new Text(headerLabel.getText());
                text.setFont(headerLabel.getFont());
                desiredWidth += text.getLayoutBounds().getWidth() + headerLabel.getLabelPadding().getLeft() + headerLabel.getLabelPadding().getRight();
            }

            Node headerGraphic = headerLabel.getGraphic();
            if ((headerLabel.getContentDisplay().equals(ContentDisplay.LEFT) || headerLabel.getContentDisplay().equals(ContentDisplay.RIGHT)) && headerGraphic != null) {
                desiredWidth += headerGraphic.getLayoutBounds().getWidth();
            }

            // Handle minimum width calculations
            // Use a "w" because it is typically the widest character
            Text minText = new Text(StringUtils.repeat("W", Math.min(0, minWidth)));
            minText.setFont(headerLabel.getFont());

            // Check rows
            double minPxWidth = 0;
            for (int row = 0; row < rows; row++) {
                cell.updateTableColumn(col);
                cell.updateTableView(col.getTableView());
                cell.updateIndex(row);

                // Handle minimum width calculations
                // Just do this once
                if (row == 0) {
                    String oldText = cell.getText();
                    // Use a "w" because it is typically the widest character
                    cell.setText(StringUtils.repeat("W", Math.max(0, minWidth)));

                    header.getChildrenUnmodifiable().add(cell);
//                    cell.impl_processCSS(false);
                    minPxWidth = cell.prefWidth(-1);
                    header.getChildrenUnmodifiable().remove(cell);

                    cell.setText(oldText);
                }

                if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
                    header.getChildrenUnmodifiable().add(cell);
//                    cell.impl_processCSS(false);
                    desiredWidth = Math.max(desiredWidth, cell.prefWidth(-1));
                    desiredWidth = Math.max(desiredWidth, minPxWidth);
                    header.getChildrenUnmodifiable().remove(cell);
                }
            }

            desiredWidth = desiredWidth + padding;

            if (maxWidth > 0) {
                desiredWidth = Math.min(maxWidth, desiredWidth);
            }

            col.impl_setWidth(desiredWidth);
        }
    }

    public static void autoResizeColumns(TableView<?> table) {
        //Set the right policy
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().stream().forEach((column) -> {
            //Minimal width = columnheader
            double prefWidth = column.getPrefWidth();
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                //cell must not be empty
                if (column.getCellData(i) != null) {

                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            max = Math.max(max + 30.0d, prefWidth);
            max = Math.min(max, 500);
            //set the new max-widht with some extra space
            column.setPrefWidth(max);
        });
    }
}
