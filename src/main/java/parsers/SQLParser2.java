package parsers;

import beans.FiltersColumns;
import beans.Table;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

public class SQLParser2 {


    static String sql = "select * from db.table1 t1 inner join db.table2 t2 on t1.id=t2.id where t1.xyz=? and t2.abc=?";
    public static void main(String[] args) throws ParseException {
//        SqlParser parser = new SqlParser();
//        // String sql = "select * from xyz where x=y group by x order by y limit 10";
//        Query query = (Query)parser.createStatement(sql, new ParsingOptions());
//        QuerySpecification body = (QuerySpecification)query.getQueryBody();
//
//        Select select = body.getSelect();
//        Map<Table, FiltersColumns> context = new HashMap<>();
//        System.out.println("Columns = " + select.getSelectItems());
//
//        Relation r = body.getFrom().get();
//        r.accept(new AstVisitor<String, Map<Table, FiltersColumns>>() {
//            @Override
//            protected String visitRelation(Relation node, Map<Table, FiltersColumns> context) {
//                node.accept(new AstVisitor<String, Map>() {
//                    @Override
//                    protected String visitJoin(Join node, Map context) {
//
//                        return super.visitJoin(node, context);
//                    }
//                }, context);
//                return super.visitRelation(node, context);
//            }
//        }, context);
//
//
//        Optional<Expression> where = body.getWhere();
//        System.out.println("Where = " + where.get().getChildren().get(0));
//        System.out.println("Group by = " + body.getGroupBy());
//        System.out.println("Order by = " + body.getOrderBy());
//        System.out.println("Limit = " + body.getLimit().get());

        Date date = new Date();
        System.out.println(date +" :: " + date.getTime());


        SimpleDateFormat utcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        utcFormatter.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

        System.out.println(utcFormatter.parse(utcFormatter.format(date)).getTime());


    }

}
