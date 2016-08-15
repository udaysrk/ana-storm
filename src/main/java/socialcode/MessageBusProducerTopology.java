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

import backtype.storm.spout.SpoutOutputCollector;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MessageBusProducerTopology {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBusProducerTopology.class);

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

    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("1", new TestDocumentSpout());
        // subscribe to all streams on the TestDocumentSpout
        builder.setBolt("2", new MessageBusWriterBolt()).shuffleGrouping("1");

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
