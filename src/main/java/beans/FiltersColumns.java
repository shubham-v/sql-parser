package beans;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class FiltersColumns {

    List<Filter> filters;
    Set<String> columns;

    public static FiltersColumns newInstance() {
        FiltersColumns filtersColumns = new FiltersColumns();
        filtersColumns.filters = new ArrayList<>();
        filtersColumns.columns = new HashSet<>();
        return filtersColumns;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Set<String> getColumns() {
        return columns;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    @Override
    public String toString() {
        return "Filters: " + StringUtils.join(filters, ";") + "\n Columns: " + StringUtils.join(columns, ";");
    }
}
