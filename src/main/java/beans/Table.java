package beans;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class Table {
    String database;
    String table;
    String alias;

    public Table() {}
    public Table (String db, String tb, String alias) {
        this.database = db;
        this.table = tb;
        this.alias = alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table1 = (Table) o;
        return Objects.equals(alias, table1.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    public String toString() {
        return table + " as " + alias;
    }

    public static Table newInstance(String table, String alias) {
        String[] dbTable = table.split("\\.");
        String db =null;
        if (dbTable.length == 2) {
            db = dbTable[0];
            table = dbTable[1];
        }
        if (alias == null || alias.equals("")) {
            alias = table;
        }
        return new Table(db, table, alias);
    }
}
