package visitors;

public class CustomCCJSQlparserDefaultVisitor /*extends CCJSqlParserDefaultVisitor*/ {

   /* private Map<Table, FiltersColumns> tablesFilterColumn;
    private final beans.Table unknownTable;

    public CustomCCJSQlparserDefaultVisitor(Map<beans.Table, FiltersColumns> tablesFilterColumn, beans.Table unknownTable) {
        this.tablesFilterColumn = tablesFilterColumn;
        this.unknownTable = unknownTable;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        if (node.getId() == CCJSqlParserTreeConstants.JJTCOLUMN) {
            String[] tableColumn = node.jjtGetValue().toString().split("\\.");
            String tableAlias = null, column;
            Table t = unknownTable;
            if (tableColumn.length == 2) {
                tableAlias = tableColumn[0];
                column = tableColumn[1];
            } else {
                column = tableColumn[0];
            }
            if (tableAlias != null && !"".equals(tableAlias)) {
                t = new beans.Table();
                t.setAlias(tableAlias);
            }
            tablesFilterColumn.get(t).addColumn(column);
            return super.visit(node, data);
        } else {
            return super.visit(node, data);
        }
    }*/

}
