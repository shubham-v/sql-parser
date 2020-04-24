package parsers;

import beans.Filter;
import beans.Table;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SQLParser1 {

    public static void main(String[] args) throws IOException {
        String jsonDump = executeScript("select * from db.table1 t2 where t2.xyz=? and t2.abc=?");

        Map<String, Object> json = new Gson().fromJson(jsonDump, LinkedHashMap.class);

        List<Object> tables = (List) json.get("from");
        Map<String, Object> where = (Map) json.get("where");

        Map<Table, List<Filter>> tablesMap = new HashMap<>();

        tables.stream().map(t -> (Map) t).forEach(t -> {
            String[] dbTable = ((String) t.get("value")).split("\\.");
            String db = null, table = "";
            if (dbTable.length == 2) {
                db = dbTable[0];
                table = dbTable[1];
            } else {
                table = dbTable[0];
            }
            String alias = (String) t.get("name");
            Table table1 = new Table(db, table, alias);
            tablesMap.putIfAbsent(table1, new ArrayList<>());
        });

        List<Filter> filters = getFiltersFromStatement(where);

        filters.forEach(filter -> {
            String table = filter.getTable();
            Table t = new Table();
            t.setAlias(table);
            List<Filter> fs = tablesMap.get(t);
            fs.add(filter);
        });

        System.out.println(tablesMap);

    }

    static List<Filter> getFiltersFromStatement(Map<String, Object> filterStatment) {
        List<Filter> result = new ArrayList<>();
        String key = filterStatment.keySet().stream().findFirst().get();
        if ("and".equalsIgnoreCase(key) || "or".equalsIgnoreCase(key)) {
            result.addAll(getFromList((List) filterStatment.get(key)));
        } else {
            result.add(getFilter(filterStatment));
        }
        return result;
    }


    static List<Filter> getFromList(List<Object> filterList) {
        List<Filter> result = new ArrayList<>();
        filterList.stream().map(e -> (Map<String, Object>) e).forEach(k -> {
            String key = k.keySet().stream().findFirst().get();
            if ("and".equalsIgnoreCase(key) || "or".equalsIgnoreCase(key)) {
                result.addAll(getFromList((List) k.get(key)));
            } else {
                result.add(getFilter(k));
            }
        });
        return result;
    }

    static Filter getFilter(Map<String, Object> filter) {
        String key = filter.keySet().stream().findFirst().get();
        List<String> keyValue = (List) filter.get(key);
        String column = keyValue.get(0);
        String[] tableCol = column.split("\\.");
        String table = tableCol[0];
        String col = tableCol[1];
        Object val = keyValue.get(1);
        return new Filter(table, col, key, val);
    }


    static String executeScript(String sql) throws IOException {
        String[] command = {
                "python3",
                "sql_parser.py",
                "--sql",
                sql
        };
        System.out.println(String.join(" ", command));
        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
            return s;
        }


        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
        return null;
    }

}
