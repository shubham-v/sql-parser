package parsers;

import beans.FiltersColumns;
import beans.Table;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import utils.JavaType;
import visitors.*;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class SQLParser3 {

    private final Map<String, Map<String, Map<String, Class<?>>>> structure = new HashMap<>();

    public SQLParser3() throws JSQLParserException {
    }

    private Map<beans.Table, FiltersColumns> createAndGetTablesFilterMap(String sql) throws JSQLParserException {
        Map<beans.Table, FiltersColumns> tablesFilterColumns = new LinkedHashMap<>();
        beans.Table unknownTable = new beans.Table("", "", "");
        tablesFilterColumns.put(unknownTable, new FiltersColumns());
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = newTablesNameFinder(tablesFilterColumns);
        tablesNamesFinder.getTableList(selectStatement);
        System.out.println(tablesFilterColumns.keySet());

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        Expression expression = ps.getWhere();
        expression.accept(newExpressionVisitorAdapter(tablesFilterColumns, unknownTable));
        ps.getSelectItems().forEach(i -> i.accept(new SelectItemVisitor() {
            @Override
            public void visit(AllColumns allColumns) {}
            @Override
            public void visit(AllTableColumns allTableColumns) {}
            @Override
            public void visit(SelectExpressionItem selectExpressionItem) {
                String alias = selectExpressionItem.getAlias().getName();
                selectExpressionItem.getExpression().accept(new ExpressionVisitorAdapter() {
                    @Override
                    public void visit(Column column) {
                        super.visit(column);
                        String table = column.getTable().getName();
                        String columnName = column.getColumnName();
                        Table t = unknownTable;
                        if (table != null) {
                            t = new Table();
                            t.setAlias(table);
                        }
                        try {
                            tablesFilterColumns.get(t).addColumn(columnName);
                        } catch (Exception e) {
                            System.out.println(selectExpressionItem.getExpression());
                        }
                    }
                });
            }
        }));


//        SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
//        node.jjtAccept(newCCJsqlParserDefaultVisitor(tablesFilterColumns, unknownTable), null);

        return tablesFilterColumns;
    }

    private ExpressionVisitor newExpressionVisitorAdapter(Map<beans.Table, FiltersColumns> tablesFilterColumn, beans.Table unknownTable) {
        return new CustomExpressionVisiterAdapter(tablesFilterColumn, unknownTable);
    }

    private TablesNamesFinder newTablesNameFinder(Map<beans.Table, FiltersColumns> tablesFilterColumn) {
        return new CustomTablesNameFinder(tablesFilterColumn);
    }

//    private CCJSqlParserDefaultVisitor newCCJsqlParserDefaultVisitor(
//            Map<beans.Table, FiltersColumns> tablesFilterColumn, beans.Table unknownTable) {
//        return new CustomCCJSQlparserDefaultVisitor(tablesFilterColumn, unknownTable);
//    }

    private void addTable(String createQuery) throws JSQLParserException {

        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(createQuery);
        String database = createTable.getTable().getSchemaName();
        String table = createTable.getTable().getName();

        structure.putIfAbsent(database, new HashMap<>());
        Map<String, Map<String, Class<?>>> tables =  structure.get(database);
        tables.putIfAbsent(table, new LinkedHashMap<>());
        Map<String, Class<?>> columns = tables.get(table);

        createTable.getColumnDefinitions().forEach(columnDefinition -> {
            String columnName = columnDefinition.getColumnName();
            String dataType = columnDefinition.getColDataType().getDataType();
            Class<?> dataTypeClazz = JavaType.get(dataType);
            columns.put(columnName, dataTypeClazz);
        });
    }

    private String convertSpektrDate(String sql) throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect ps = (PlainSelect) select.getSelectBody();
        Expression expression = ps.getWhere();
        Set<String> dateStrings = new HashSet<>();
        expression.accept(new FetchColumnExpressionVisiterAdapter(dateStrings));
        System.out.println(dateStrings);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date maxDate = Date.from(Instant.EPOCH);
        for (String dateString : dateStrings) {
            try {
                Date d = df.parse(dateString);
                maxDate = d.after(maxDate) ? d : maxDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println("MaxDate: " + maxDate);
        expression.accept(new UpdateColumnValueExpressionVisiterAdapter("spektr_date", maxDate));
        return select.toString();
    }

//    public static void main(String[] args) throws JSQLParserException {
//
//        Statements stmts = CCJSqlParserUtil.parseStatements("SELECT * FROM tab1; SELECT * FROM tab2");
//
//
////        String sql = "select * from db.table1 t1 inner join db.table2 t2 on t1.id=t2.id where t1.xyz=? and t2.abc=?";
//
//        String sql =
//                "SELECT t1.column_name c1\n" +
//                "FROM db1.table_name t1\n" +
//                "WHERE (t1.col2 BETWEEN value1 AND value2) AND t1.col2 = 2;";
//        Statement statement = CCJSqlParserUtil.parse(sql);
//        Select selectStatement = (Select) statement;
//        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder() {
//            @Override
//            public void visit(Table t) {
//                super.visit(t);
//                String database = t.getSchemaName();
//                System.out.println("Databse Name:"+database);
//                String tableName = t.getName();
//                System.out.println("beans.Table Name :"+tableName);
//                String name = t.getAlias().getName();
//                System.out.println("Alias Name:" + name);
//            }
//        };
//        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
//
//        System.out.println(tableList);
//
//
//        CCJSqlParserManager parserManager = new CCJSqlParserManager();
//        Select select = (Select) parserManager.parse(new StringReader(sql));
//
//        PlainSelect ps = (PlainSelect) select.getSelectBody();
//
//
//        String whereClause = /*"t1.xyz=? or t2.abc=?";*/ ps.getWhere().toString();
//        Expression expr = ps.getWhere(); /*CCJSqlParserUtil.parseCondExpression(whereClause);*/
//
//        expr.accept(new ExpressionVisitorAdapter() {
//
//            @Override
//            protected void visitBinaryExpression(BinaryExpression expr) {
//                super.visitBinaryExpression(expr);
//                if (expr instanceof ComparisonOperator) {
//                    System.out.println("column=" + expr.getLeftExpression() + "  op=" +  expr.getStringExpression() + "  value=" + expr.getRightExpression());
//                    String[] tableColumn = expr.getLeftExpression().toString().split("\\.");
//                    String table = null, column = null;
//                    if (tableColumn.length == 2) {
//                        table = tableColumn[0];
//                        column = tableColumn[1];
//                    } else {
//                        column = tableColumn[0];
//                    }
//                    String operator = expr.getStringExpression().toString();
//                    String right = expr.getRightExpression().toString();
//                    Filter filter = new Filter(table, column, operator, right);
//                    if (table == null || "".equals(table)) {
////                        tablesFilterMap.get(UNKNOWN_TABLE).add(filter);
//                    }
//                }
//            }
//
//            @Override
//            public void visit(Between expr) {
//                System.out.println(expr.getLeftExpression());
//                String[] tableColumn = expr.getLeftExpression().toString().split("\\.");
//                String table = null, column = null;
//                if (tableColumn.length == 2) {
//                    table = tableColumn[0];
//                    column = tableColumn[1];
//                } else {
//                    column = tableColumn[0];
//                }
//                String operator = "BETWEEN";
//                String start = expr.getBetweenExpressionStart().toString();
//                String end = expr.getBetweenExpressionEnd().toString();
//                Range range = new Range(start, end);
//                Filter filter = new Filter(table, column, operator, range);
//                if (table == null || "".equals(table)) {
////                    tablesFilterMap.get(UNKNOWN_TABLE).add(filter);
//                }
//            }
//
//
//        });
//
//        String sql = "SELECT * FROM  ( ( SELECT TBL.ID AS rRowId, TBL.NAME AS name, TBL.DESCRIPTION as description, TBL.TYPE AS type, TBL1.SHORT_NAME AS shortName  FROM ROLE_TBL TBL WHERE ( TBL.TYPE = 'CORE' OR  TBL1.SHORT_NAME = 'TNG' AND  TBL.IS_DELETED <> 1  ) ) MINUS ( SELECT TBL.ID AS rRowId, TBL.NAME AS name, TBL.DESCRIPTION as description, TBL.TYPE AS type, TBL3.SHORT_NAME AS shortName,TBL3.NAME AS tenantName FROM ROLE_TBL TBL INNER JOIN TYPE_ROLE_TBL TBL1 ON TBL.ID=TBL1.ROLE_FK LEFT OUTER JOIN TNT_TBL TBL3 ON TBL3.ID = TBL.TENANT_FK LEFT OUTER JOIN USER_TBL TBL4 ON TBL4.ID = TBL1.USER_FK WHERE ( TBL4.ID =771100 AND  TBL.IS_DELETED <> 1  ) ) ) ORDER BY name ASC";
//
//        SimpleNode node = (SimpleNode) CCJSqlParserUtil.parseAST(sql);
//
//        node.jjtAccept(new CCJSqlParserDefaultVisitor() {
//            @Override
//            public Object visit(SimpleNode node, Object data) {
//                if (node.getId() == CCJSqlParserTreeConstants.JJTCOLUMN) {
//                    System.out.println(node.jjtGetValue());
//                    return super.visit(node, data);
//                } else {
//                    return super.visit(node, data);
//                }
//            }
//        }, null);
//    }


    public static void main(String[] args) throws JSQLParserException {

//        String sql = "select * from db.table1 t1 inner join db.table2 t2 on t1.id=t2.id where t1.xyz=? and t2.abc=?";
//        String sql = "SELECT /*+ REPARTITION(200) */ clicks.qualifier as qualifier , clicks.log_id_hash as log_id_hash , clicks.ad_index as ad_index , clicks.session_id as session_id , clicks.customer_id as customer_id , clicks.device_type as device_type , clicks.user_agent as user_agent , clicks.pre_impr_http_request_id as pre_impr_http_request_id , clicks.pre_impr_http_child_request_id as pre_impr_http_child_request_id , clicks.http_request_id as http_request_id , clicks.postal_code as postal_code , clicks.country_code as country_code , clicks.marketplace_id as marketplace_id , clicks.regional_offer as regional_offer , clicks.pre_impr_timestamp as pre_impr_timestamp , clicks.hit_datetime_gmt as hit_datetime_gmt , clicks.hit_day_loc as hit_day_loc , clicks.clicked_time as clicked_time , clicks.click_datetime_gmt as click_datetime_gmt , clicks.click_day_loc as click_day_loc , clicks.business_program_id as business_program_id , clicks.ad_program_type as ad_program_type , clicks.traffic_attributes as traffic_attributes , clicks.advertiser_id as advertiser_id , clicks.campaign_id as campaign_id , clicks.ad_group_id as ad_group_id , clicks.ad_id as ad_id , clicks.ad_types_long as ad_types_long , clicks.asin as asin , clicks.parent_asin as parent_asin , clicks.sku as sku , clicks.gl_product_group as gl_product_group , CONVERT_GL_PRODUCT_GROUP_TO_CODE(clicks.gl_product_group) as gl_product_group_code , clicks.targeting_clause_id as targeting_clause_id , clicks.matched_keyword_type as matched_keyword_type , clicks.matched_keyword as matched_keyword , clicks.decoration_id as decoration_id , clicks.is_image_present as is_image_present , clicks.portfolio_id as portfolio_id , clicks.placement_slot as placement_slot , clicks.placement_name as placement_name , clicks.placement_id as placement_id , clicks.slot_position as slot_position , clicks.slot_size as slot_size , clicks.slot_id as slot_id , clicks.slot_placement as slot_placement , clicks.slot_name as slot_name , clicks.impression_position as impression_position , clicks.impression_rank_on_asin as impression_rank_on_asin , clicks.impression_index as impression_index , clicks.rank_offset as rank_offset , clicks.page_number as page_number , clicks.page_type as page_type , clicks.sub_page_type as sub_page_type , clicks.page_asin as page_asin , clicks.page_gl_product_group as page_gl_product_group , CONVERT_GL_PRODUCT_GROUP_TO_CODE(clicks.page_gl_product_group) as page_gl_product_group_code , clicks.page_layout as page_layout , clicks.search_index as search_index , clicks.search_query as search_query , clicks.normalized_search_query as normalized_search_query , clicks.report_search_query as report_search_query , clicks.q_id as q_id , clicks.number_of_columns as number_of_columns , clicks.weblab_treatments as weblab_treatments , clicks.channel_id as channel_id , clicks.publisher_id as publisher_id , clicks.app_id as app_id , clicks.site_uid as site_uid , clicks.source_search_engine as source_search_engine , clicks.source_type as source_type , clicks.source_info as source_info , clicks.sourcing_browse_nodes as sourcing_browse_nodes , clicks.context_asins as context_asins , clicks.cpc as cpc , clicks.currency_code as currency_code , clicks.modified_cpc as modified_cpc , clicks.original_cpc as original_cpc , clicks.calculated_cpc as calculated_cpc , clicks.bid_type as bid_type , clicks.max_bid as max_bid , clicks.original_bid as original_bid , clicks.bid_adjustment_coefficient as bid_adjustment_coefficient , clicks.total_offers_available as total_offers_available , clicks.total_offers_requested as total_offers_requested , clicks.total_offers_returned as total_offers_returned , clicks.total_offers_displayed as total_offers_displayed , clicks.ads_requested as ads_requested , clicks.ads_sourced as ads_sourced , clicks.ads_filtered as ads_filtered , clicks.ads_truncated as ads_truncated , clicks.ads_returned as ads_returned , clicks.sp_ads_returned as sp_ads_returned , clicks.no_ad_reason as no_ad_reason , clicks.punt_reason as punt_reason , clicks.rank_treatment_id as rank_treatment_id , clicks.ranking_strategy_code as ranking_strategy_code , clicks.relevance_score as relevance_score , clicks.ecvrm as ecvrm , clicks.ectrm as ectrm , clicks.sampled_ectr as sampled_ectr , clicks.eutility as eutility , clicks.standard_error as standard_error , clicks.acvr as acvr , clicks.winner_evpm as winner_evpm , clicks.winner_erpm as winner_erpm , clicks.winner_eupm as winner_eupm , clicks.loser_evpm as loser_evpm , clicks.loser_erpm as loser_erpm , clicks.loser_eupm as loser_eupm , clicks.tq_valid as tq_valid , clicks.tq_validation_message as tq_validation_message , clicks.tq_validation_message_decoded as tq_validation_message_decoded , clicks.tq_offline_validation_time as tq_offline_validation_time , clicks.validation_decision_time as validation_decision_time , clicks.invalidation_decision_time as invalidation_decision_time , clicks.valid_history as valid_history , clicks.tq_valid_cascade as tq_valid_cascade , clicks.correction_publish_time as correction_publish_time , clicks.rendered as rendered , clicks.render_pixel as render_pixel , clicks.rendered_decision_time as rendered_decision_time , clicks.viewed as viewed , clicks.duplicate as duplicate , clicks.phantom as phantom , clicks.phantom_decision_time as phantom_decision_time , clicks.robot_confidence as robot_confidence , clicks.syndication_traffic as syndication_traffic , clicks.test_traffic as test_traffic , clicks.offer_price as offer_price , clicks.offer_program_type as offer_program_type , clicks.buy_box_ignored as buy_box_ignored , clicks.buybox_forced as buybox_forced , clicks.buybox_valid as buybox_valid , clicks.percolate_rank as percolate_rank , clicks.percolate_score as percolate_score , clicks.cs_ad_id as cs_ad_id , clicks.cs_creative_id as cs_creative_id , clicks.cs_campaign_id as cs_campaign_id , clicks.cs_ad_creative_id as cs_ad_creative_id , clicks.cs_advertiser_id as cs_advertiser_id , clicks.aax_session_id as aax_session_id , clicks.aax_media_cost_cpm as aax_media_cost_cpm , clicks.discriminators as discriminators , clicks.language_tag as language_tag , clicks.pb_eligible as pb_eligible , clicks.client_id as client_id , clicks.source_tag as source_tag , conv.sale_order_id as sale_order_id , conv.sold_asin as sold_asin , conv.sale_time as sale_time , CONVERT_UNIX_TIME_TO_ISOSTRING(cast(conv.sale_time/1000 as long), 'eu') AS sale_datetime_gmt , CONVERT_UNIX_TIME_TO_DATE_STRING(cast(conv.sale_time/1000 as long), 'eu') AS sale_day_loc , conv.sale_quantity as sale_quantity , conv.sale_price as sale_price , conv.sale_currency as sale_currency , conv.advertiser_id as sale_advertiser_id , conv.campaign_id as sale_campaign_id , conv.ad_id as sale_ad_id , conv.creative_id as sale_creative_id , conv.sale_session_id as sale_session_id , conv.sale_customer_id as sale_customer_id , conv.request_tag as request_tag , conv.brand_name as brand_name , conv.aax_bid_id as aax_bid_id , conv.spektr_date as spektr_date , conv.region_id as region_id FROM spektr_ach.d_mads_sb_conversions conv LEFT JOIN spektr_ach.d_sb_ad_clicks clicks ON clicks.http_request_id = conv.click_id WHERE clicks.spektr_date >= DATE_SUB('{$DAG_SCHEDULED_INSTANCE_DATE}', 14) AND clicks.spektr_date <= '{$DAG_SCHEDULED_INSTANCE_DATE}' AND conv.spektr_date = '{$DAG_SCHEDULED_INSTANCE_DATE}' AND clicks.region_id = 2 AND conv.region_id = 2";
        String sql = "SELECT /*+ REPARTITION(200) */ clicks.qualifier as qualifier , clicks.log_id_hash as log_id_hash , clicks.ad_index as ad_index , clicks.session_id as session_id , clicks.customer_id as customer_id , clicks.device_type as device_type , clicks.user_agent as user_agent , clicks.pre_impr_http_request_id as pre_impr_http_request_id , clicks.pre_impr_http_child_request_id as pre_impr_http_child_request_id , clicks.http_request_id as http_request_id , clicks.postal_code as postal_code , clicks.country_code as country_code , clicks.marketplace_id as marketplace_id , clicks.regional_offer as regional_offer , clicks.pre_impr_timestamp as pre_impr_timestamp , clicks.hit_datetime_gmt as hit_datetime_gmt , clicks.hit_day_loc as hit_day_loc , clicks.clicked_time as clicked_time , clicks.click_datetime_gmt as click_datetime_gmt , clicks.click_day_loc as click_day_loc , clicks.business_program_id as business_program_id , clicks.ad_program_type as ad_program_type , clicks.traffic_attributes as traffic_attributes , clicks.advertiser_id as advertiser_id , clicks.campaign_id as campaign_id , clicks.ad_group_id as ad_group_id , clicks.ad_id as ad_id , clicks.ad_types_long as ad_types_long , clicks.asin as asin , clicks.parent_asin as parent_asin , clicks.sku as sku , clicks.gl_product_group as gl_product_group , CONVERT_GL_PRODUCT_GROUP_TO_CODE(clicks.gl_product_group) as gl_product_group_code , clicks.targeting_clause_id as targeting_clause_id , clicks.matched_keyword_type as matched_keyword_type , clicks.matched_keyword as matched_keyword , clicks.decoration_id as decoration_id , clicks.is_image_present as is_image_present , clicks.portfolio_id as portfolio_id , clicks.placement_slot as placement_slot , clicks.placement_name as placement_name , clicks.placement_id as placement_id , clicks.slot_position as slot_position , clicks.slot_size as slot_size , clicks.slot_id as slot_id , clicks.slot_placement as slot_placement , clicks.slot_name as slot_name , clicks.impression_position as impression_position , clicks.impression_rank_on_asin as impression_rank_on_asin , clicks.impression_index as impression_index , clicks.rank_offset as rank_offset , clicks.page_number as page_number , clicks.page_type as page_type , clicks.sub_page_type as sub_page_type , clicks.page_asin as page_asin , clicks.page_gl_product_group as page_gl_product_group , CONVERT_GL_PRODUCT_GROUP_TO_CODE(clicks.page_gl_product_group) as page_gl_product_group_code , clicks.page_layout as page_layout , clicks.search_index as search_index , clicks.search_query as search_query , clicks.normalized_search_query as normalized_search_query , clicks.report_search_query as report_search_query , clicks.q_id as q_id , clicks.number_of_columns as number_of_columns , clicks.weblab_treatments as weblab_treatments , clicks.channel_id as channel_id , clicks.publisher_id as publisher_id , clicks.app_id as app_id , clicks.site_uid as site_uid , clicks.source_search_engine as source_search_engine , clicks.source_type as source_type , clicks.source_info as source_info , clicks.sourcing_browse_nodes as sourcing_browse_nodes , clicks.context_asins as context_asins , clicks.cpc as cpc , clicks.currency_code as currency_code , clicks.modified_cpc as modified_cpc , clicks.original_cpc as original_cpc , clicks.calculated_cpc as calculated_cpc , clicks.bid_type as bid_type , clicks.max_bid as max_bid , clicks.original_bid as original_bid , clicks.bid_adjustment_coefficient as bid_adjustment_coefficient , clicks.total_offers_available as total_offers_available , clicks.total_offers_requested as total_offers_requested , clicks.total_offers_returned as total_offers_returned , clicks.total_offers_displayed as total_offers_displayed , clicks.ads_requested as ads_requested , clicks.ads_sourced as ads_sourced , clicks.ads_filtered as ads_filtered , clicks.ads_truncated as ads_truncated , clicks.ads_returned as ads_returned , clicks.sp_ads_returned as sp_ads_returned , clicks.no_ad_reason as no_ad_reason , clicks.punt_reason as punt_reason , clicks.rank_treatment_id as rank_treatment_id , clicks.ranking_strategy_code as ranking_strategy_code , clicks.relevance_score as relevance_score , clicks.ecvrm as ecvrm , clicks.ectrm as ectrm , clicks.sampled_ectr as sampled_ectr , clicks.eutility as eutility , clicks.standard_error as standard_error , clicks.acvr as acvr , clicks.winner_evpm as winner_evpm , clicks.winner_erpm as winner_erpm , clicks.winner_eupm as winner_eupm , clicks.loser_evpm as loser_evpm , clicks.loser_erpm as loser_erpm , clicks.loser_eupm as loser_eupm , clicks.tq_valid as tq_valid , clicks.tq_validation_message as tq_validation_message , clicks.tq_validation_message_decoded as tq_validation_message_decoded , clicks.tq_offline_validation_time as tq_offline_validation_time , clicks.validation_decision_time as validation_decision_time , clicks.invalidation_decision_time as invalidation_decision_time , clicks.valid_history as valid_history , clicks.tq_valid_cascade as tq_valid_cascade , clicks.correction_publish_time as correction_publish_time , clicks.rendered as rendered , clicks.render_pixel as render_pixel , clicks.rendered_decision_time as rendered_decision_time , clicks.viewed as viewed , clicks.duplicate as duplicate , clicks.phantom as phantom , clicks.phantom_decision_time as phantom_decision_time , clicks.robot_confidence as robot_confidence , clicks.syndication_traffic as syndication_traffic , clicks.test_traffic as test_traffic , clicks.offer_price as offer_price , clicks.offer_program_type as offer_program_type , clicks.buy_box_ignored as buy_box_ignored , clicks.buybox_forced as buybox_forced , clicks.buybox_valid as buybox_valid , clicks.percolate_rank as percolate_rank , clicks.percolate_score as percolate_score , clicks.cs_ad_id as cs_ad_id , clicks.cs_creative_id as cs_creative_id , clicks.cs_campaign_id as cs_campaign_id , clicks.cs_ad_creative_id as cs_ad_creative_id , clicks.cs_advertiser_id as cs_advertiser_id , clicks.aax_session_id as aax_session_id , clicks.aax_media_cost_cpm as aax_media_cost_cpm , clicks.discriminators as discriminators , clicks.language_tag as language_tag , clicks.pb_eligible as pb_eligible , clicks.client_id as client_id , clicks.source_tag as source_tag , conv.sale_order_id as sale_order_id , conv.sold_asin as sold_asin , conv.sale_time as sale_time , CONVERT_UNIX_TIME_TO_ISOSTRING(cast(conv.sale_time/1000 as long), 'eu') AS sale_datetime_gmt , CONVERT_UNIX_TIME_TO_DATE_STRING(cast(conv.sale_time/1000 as long), 'eu') AS sale_day_loc , conv.sale_quantity as sale_quantity , conv.sale_price as sale_price , conv.sale_currency as sale_currency , conv.advertiser_id as sale_advertiser_id , conv.campaign_id as sale_campaign_id , conv.ad_id as sale_ad_id , conv.creative_id as sale_creative_id , conv.sale_session_id as sale_session_id , conv.sale_customer_id as sale_customer_id , conv.request_tag as request_tag , conv.brand_name as brand_name , conv.aax_bid_id as aax_bid_id , conv.spektr_date as spektr_date , conv.region_id as region_id FROM spektr_ach.d_mads_sb_conversions conv LEFT JOIN spektr_ach.d_sb_ad_clicks clicks ON clicks.http_request_id = conv.click_id WHERE clicks.spektr_date >= '2020-04-12' AND clicks.spektr_date <= '2020-04-11' AND conv.spektr_date = '2020-04-15' AND clicks.region_id = 2 AND conv.region_id = 2 AND clicks.spektr_hour = '01'";

        Objects.requireNonNull(sql);

        SQLParser3 parser = new SQLParser3();

        sql = parser.convertSpektrDate(sql);

        Map<beans.Table, FiltersColumns> tableFiltersColumnsMap = parser.createAndGetTablesFilterMap(sql);
        tableFiltersColumnsMap.forEach((k, v) -> {
            System.out.println("Table: " + k);
            System.out.println("Filters: " + v.getFilters());
            System.out.println("Columns: " + v.getColumns());
        });


//        String createTable = "CREATE Table DatabaseName.tableName (id INTEGER NOT NULL,name varchar(100) NOT NULL,CRE_TS TIMESTAMP(0) NOT NULL,UPD_TS TIMESTAMP(0) NOT NULL);";
//
//        parser.addTable(createTable);
//
//        System.out.println(parser.structure);
    }



}
