package socialcode;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.tuple.ITuple;
import backtype.storm.utils.Utils;

import org.apache.commons.io.IOUtils;

import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.bolt.JdbcInsertBolt;
import org.apache.storm.jdbc.common.Column;
import org.apache.storm.jdbc.mapper.JdbcMapper;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class AnalyticsDemoTopology {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsDemoTopology.class);

    public static class TestDocumentSpout extends BaseRichSpout {
        private static SpoutOutputCollector _collector;
        private static String _document;

        public TestDocumentSpout() throws IOException {
            super();
            _document = IOUtils.toString(getClass().getResourceAsStream("/pages.json"));
        }

        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            _collector = collector;
        }

        public void nextTuple() {
            // emit the same document every second
            Utils.sleep(1000);
            _collector.emit(new Values(_document));
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("doc"));
        }
    }

    public static class AnalyticsJdbcMapper implements JdbcMapper {

        @Override
        public List<Column> getColumns(ITuple tuple) {
            try {
                JSONObject docObj = new JSONObject(tuple.getString(0));
                JSONObject dataObj = docObj.getJSONObject("data");
                List<Column> columns = new ArrayList<Column>();
                columns.add(new Column("id", dataObj.getString("id"), Types.VARCHAR));
                columns.add(new Column("name", dataObj.getString("name"), Types.VARCHAR));
                columns.add(new Column("username", dataObj.getString("username"), Types.VARCHAR));
                columns.add(new Column("link", dataObj.getString("link"), Types.VARCHAR));
                columns.add(new Column("is_published", dataObj.getBoolean("is_published"), Types.BIT));
                columns.add(new Column("likes", dataObj.getInt("likes"), Types.INTEGER));
                columns.add(new Column("talking_about_count", dataObj.getInt("talking_about_count"), Types.INTEGER));
                columns.add(new Column("cover", dataObj.getString("cover"), Types.VARCHAR));
                columns.add(new Column("picture", dataObj.getString("picture"), Types.VARCHAR));
                return columns;
            } catch (JSONException e) {
                throw new RuntimeException("Failed to parse JSON", e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Map hikariConfigMap = new HashMap<String, Object>();
        hikariConfigMap.put("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariConfigMap.put("dataSource.url", "jdbc:mysql://localhost:3306/facebook_analytics");
        hikariConfigMap.put("dataSource.user", "root");
        HikariCPConnectionProvider connectionProvider = new HikariCPConnectionProvider(hikariConfigMap);

        String pagesPrepStmnt = IOUtils.toString(AnalyticsDemoTopology.class.getResourceAsStream("/pages_prepstmnt.sql"));

        JdbcInsertBolt userPersistanceBolt = new JdbcInsertBolt(connectionProvider, new AnalyticsJdbcMapper())
                                            .withInsertQuery(pagesPrepStmnt)
                                            .withQueryTimeoutSecs(30);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("1", new TestDocumentSpout());
        // subscribe to all streams on the TestDocumentSpout
        builder.setBolt("2", userPersistanceBolt).shuffleGrouping("1");

        Config conf = new Config();
        conf.setDebug(true);

        LOG.info("Building local cluster, may take some time...");
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());
        Utils.sleep(10000);
        cluster.killTopology("test");
        cluster.shutdown();
    }
}
