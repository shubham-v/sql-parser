package visitors;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class UpdateColumnValueExpressionVisiterAdapter extends ExpressionVisitorAdapter {

    private String columnName;
    private Date maxDate;
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static long MILLIS_IN_A_DAY = 86400000l;

    public UpdateColumnValueExpressionVisiterAdapter(String columnName, Date maxDate) {
        this.columnName = columnName;
        this.maxDate = maxDate;
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
        if (columnName.equals(column)) {
            value = value.substring(1, value.length() - 1);
            String expressionValue = "{{$PARTITION_GENERATION_DATE}}";
            try {
                Date d = df.parse(value);
                long diff = maxDate.getTime() - d.getTime();
                Duration duration = Duration.ofMillis(diff);
                int daysDiff = (int) (Math.floor(Double.valueOf(diff / MILLIS_IN_A_DAY)));
                if (diff != 0) {
                    expressionValue = "{{$PARTITION_GENERATION_DATE-" + duration.toString() + "}}";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            expr.setRightExpression(new StringValue(expressionValue));
        }
        if ("spektr_hour".equals(column)) {
            String expressionValue = "{{$PARTITION_GENERATION_HOUR}}";
            expr.setRightExpression(new StringValue(expressionValue));
        }
    }


}
