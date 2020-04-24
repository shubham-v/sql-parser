package visitors;

import beans.Filter;
import beans.FiltersColumns;
import beans.Range;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;

import java.util.Map;

public class CustomExpressionVisiterAdapter extends ExpressionVisitorAdapter {

    private Map<beans.Table, FiltersColumns> tablesFilterColumn;
    private final beans.Table unknownTable;

    public CustomExpressionVisiterAdapter(Map<beans.Table, FiltersColumns> tablesFilterColumn, beans.Table unknownTable) {
        this.tablesFilterColumn = tablesFilterColumn;
        this.unknownTable = unknownTable;
    }

    @Override
    public void visit(AndExpression expr) {
        Expression e = expr;
        while (e != null && ((AndExpression)e).getLeftExpression() != null) {
            ((AndExpression)e).getRightExpression().accept(newBinaryExpressionVisitor());
            e = ((AndExpression)e).getLeftExpression();
            if (e instanceof AndExpression) continue;
            else if (e instanceof BinaryExpression) {
                e.accept(newBinaryExpressionVisitor());
                e = null;
            }
        }

    }

    @Override
    public void visit(OrExpression expr) {
        Expression e = expr;
        while (e != null && ((OrExpression)e).getLeftExpression() != null) {
            ((OrExpression)e).getRightExpression().accept(newBinaryExpressionVisitor());
            e = ((OrExpression)e).getLeftExpression();
            if (e instanceof OrExpression) continue;
            else if (e instanceof BinaryExpression) {
                e.accept(newBinaryExpressionVisitor());
                e = null;
            }
        }

    }

    @Override
    public void visit(Between expr) {
        String[] tableColumn = expr.getLeftExpression().toString().split("\\.");
        String table = null, column = null;
        if (tableColumn.length == 2) {
            table = tableColumn[0];
            column = tableColumn[1];
        } else {
            column = tableColumn[0];
        }
        String operator = "BETWEEN";
        String start = expr.getBetweenExpressionStart().toString();
        String end = expr.getBetweenExpressionEnd().toString();
        Range range = new Range(start, end);
        Filter filter = new Filter(table, column, operator, range);
        beans.Table t = unknownTable;
        if (table != null && !"".equals(table)) {
            t = new beans.Table();
            t.setAlias(table);
        }
        tablesFilterColumn.get(t).addFilter(filter);
    }


    ExpressionVisitorAdapter newBinaryExpressionVisitor() {
        return new ExpressionVisitorAdapter() {
            @Override
            protected void visitBinaryExpression(BinaryExpression expr) {
                String[] tableColumn = expr.getLeftExpression().toString().split("\\.");
                String table = null, column = null;
                if (tableColumn.length == 2) {
                    table = tableColumn[0];
                    column = tableColumn[1];
                } else {
                    column = tableColumn[0];
                }
                String operator = expr.getStringExpression();
                Object value = expr.getRightExpression().toString().trim();
                Filter filter = new Filter(table, column, operator, value);
                beans.Table t = unknownTable;
                if (table != null && !"".equals(table)) {
                    t = new beans.Table();
                    t.setAlias(table);
                }
                tablesFilterColumn.get(t).addFilter(filter);
            }
        };
    }
}
