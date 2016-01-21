package socialcode;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import org.apache.commons.io.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


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

    public static class FacebookAnalyticsMysqlBolt extends BaseBasicBolt {
        private static final Logger LOG = LoggerFactory.getLogger(FacebookAnalyticsMysqlBolt.class);
        private static transient Connection _connection;
        private static String _pagesPrepStmnt;

        public FacebookAnalyticsMysqlBolt() throws ClassNotFoundException, SQLException, IOException {
            super();
            // explicitly load the driver class
            Class.forName("com.mysql.jdbc.Driver");
            _connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/facebook_analytics?user=root");
            _pagesPrepStmnt = IOUtils.toString(getClass().getResourceAsStream("/pages_prepstmnt.sql"));
            LOG.info(_pagesPrepStmnt);
        }

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            try {
                JSONObject docObj = new JSONObject(tuple.getString(0));
                JSONObject dataObj = docObj.getJSONObject("data");

                PreparedStatement prepStmnt = _connection.prepareStatement(_pagesPrepStmnt);
                prepStmnt.setObject(1, dataObj.getString("id"));
                prepStmnt.setObject(2, dataObj.getString("name"));
                prepStmnt.setObject(3, dataObj.getString("username"));
                prepStmnt.setObject(4, dataObj.getString("link"));
                prepStmnt.setObject(5, dataObj.getBoolean("is_published"));
                prepStmnt.setObject(6, dataObj.getInt("likes"));
                prepStmnt.setObject(7, dataObj.getInt("talking_about_count"));
                prepStmnt.setObject(8, dataObj.getString("cover"));
                prepStmnt.setObject(9, dataObj.getString("picture"));
                prepStmnt.executeUpdate();
            } catch (JSONException | SQLException e) {
                LOG.error("Error processing document", e);
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("1", new TestDocumentSpout());
        // subscribe to all streams on the TestDocumentSpout
        builder.setBolt("2", new FacebookAnalyticsMysqlBolt()).shuffleGrouping("1");

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
