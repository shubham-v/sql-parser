package visitors;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;

import java.util.Set;

public class FetchColumnExpressionVisiterAdapter extends ExpressionVisitorAdapter {

    private Set<String> spektrDates;
    private String name;

    public FetchColumnExpressionVisiterAdapter(Set<String> spektrDates) {
        this.spektrDates = spektrDates;
    }

    @Override
    protected void visitBinaryExpression(BinaryExpression expr) {
        super.visitBinaryExpression(expr);
        String[] tableColumn = expr.getLeftExpression().toString().split("\\.");
        String table = null, column = null;
        if (tableColumn.length == 2) {
            table = tableColumn[0];
            column = tableColumn[1];
        } else {
            column = tableColumn[0];
        }
        String operator = expr.getStringExpression().toString();
        String value = expr.getRightExpression().toString();

        if ("spektr_date".equals(column)) {
            System.out.println(
                    "spektr_date: " + value
            );
            spektrDates.add(value.substring(1, value.length() - 1));
        }
    }

}
