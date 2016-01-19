package socialcode;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.task.OutputCollector;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.topology.base.BaseRichSpout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;


public class App {
    public static class TestDocumentSpout extends BaseRichSpout {
        SpoutOutputCollector _collector;

        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            _collector = collector;
        }

        public void nextTuple() {
            Utils.sleep(1000);
            String doc = "{'$schema': 'http://schemas.socialcodedev.com/facebook/pages.1.json', 'data': {'username': 'AveenoCanada', 'picture': 'https://scontent.xx.fbcdn.net/hprofile-xpt1/v/t1.0-1/10456259_802068543163417_6097620934072419937_n.png?oh=728afbe7f11f798fe645676249932087&oe=571E2D60', 'is_published': true, 'link': 'https://www.facebook.com/AveenoCanada/', 'name': 'Aveeno Canada', 'talking_about_count': 133, 'cover': 'https://scontent.xx.fbcdn.net/hphotos-xtf1/v/t1.0-9/s720x720/11046195_816752981694973_6836084116923669429_n.png?oh=b909f55ead9666c0a6105142b5b5452b&oe=5710E295', 'id': '196989827004628', 'likes': 105861}, 'metadata': {'environment': 'facebook', 'guid': '0ca4bf46-afa8-11e5-9774-22000b2687d8', 'crawl_id': '0c52e914-afa8-11e5-aca1-22000b4084ee', 'crawl_time': '2015-12-31T10:20:00+00:00'}}";
            _collector.emit(new Values(doc));
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("doc"));
        }
    }

    public static class AnalyticsBolt extends BaseBasicBolt {
        private static final Logger LOG = LoggerFactory.getLogger(App.class);
        private static transient Connection con = null;

        public AnalyticsBolt() throws ClassNotFoundException, SQLException {
            super();
            // explicitly loads the driver class
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/facebook_analytics?user=root");
        }

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            try {
                JSONObject docObj = new JSONObject(tuple.getString(0));
                JSONObject dataObj = docObj.getJSONObject("data");

                ArrayList<Object> fieldValues = new ArrayList<Object>();
                fieldValues.add(dataObj.getString("id"));
                fieldValues.add(dataObj.getString("name"));
                fieldValues.add(dataObj.getString("username"));
                fieldValues.add(dataObj.getString("link"));
                fieldValues.add(dataObj.getBoolean("is_published"));
                fieldValues.add(dataObj.getInt("likes"));
                fieldValues.add(dataObj.getInt("talking_about_count"));
                fieldValues.add(dataObj.getString("cover"));
                fieldValues.add(dataObj.getString("picture"));
                fieldValues.add(true); // default NON NULL should_crawl to true

                String stmt = "INSERT INTO facebook_analytics_service_facebookpage (id, name, username, link, is_published, likes, talking_about_count, cover, picture, should_crawl) VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id=VALUES(id), name=VALUES(name), username=VALUES(username), link=VALUES(link), is_published=VALUES(is_published), likes=VALUES(likes), talking_about_count=VALUES(talking_about_count), cover=VALUES(cover), picture=VALUES(picture), should_crawl=VALUES(should_crawl)";
                LOG.info(stmt);
                PreparedStatement prepstmt = con.prepareStatement(stmt);

                ListIterator<Object> it = fieldValues.listIterator();
                while (it.hasNext()) {
                    prepstmt.setObject(it.nextIndex() + 1, it.next());
                }
                prepstmt.executeUpdate();
            } catch (JSONException | SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout("1", new TestDocumentSpout());
            builder.setBolt("2", new AnalyticsBolt()).shuffleGrouping("1");

            Config conf = new Config();
            conf.setDebug(true);

            System.out.println("Building local cluster, may take some time..");
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(10000);
            cluster.killTopology("test");
            cluster.shutdown();
        } finally {
            // always exit with 0, weird docker bug
            System.exit(0);
        }
    }

}
