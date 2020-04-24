package visitors;

import beans.FiltersColumns;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.Map;

public class CustomTablesNameFinder extends TablesNamesFinder {

    private  Map<beans.Table, FiltersColumns> tablesFilterColumns;

    public CustomTablesNameFinder(Map<beans.Table, FiltersColumns> tablesFilterColumns) {
        this.tablesFilterColumns = tablesFilterColumns;
    }

    @Override
    public void visit(Table table) {
        super.visit(table);
        String alias = table.getAlias().getName();
        if (alias == null || "".equals(alias)) {
            alias = table.getName();
        }
        beans.Table table1 = new beans.Table(table.getSchemaName(), table.getName(), alias);
        tablesFilterColumns.putIfAbsent(table1, FiltersColumns.newInstance());
    }

}
