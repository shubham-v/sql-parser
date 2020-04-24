package beans;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Filter {
    String table;
    String column;
    String operator;
    Object value;

    public Filter(String table, String col, String operator, Object right) {
        this.table = table;
        this.column = col;
        this.operator = operator;
        this.value = right;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "Table=" + table + ", Column="+ column + ", Operator=" + operator + ", value=" + value + ";";
    }

    public static Filter newInstance(String key, String operator, Object value) {
        String[] tableColumn = key.split("\\.");
        String table = null, column = null;
        if (tableColumn.length == 2) {
            table = tableColumn[0];
            column = tableColumn[1];
        } else {
            column = tableColumn[0];
        }
        Filter f = new Filter(table, column, operator, value);
        return f;
    }

}

