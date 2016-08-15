package socialcode;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MessageBusWriterBolt implements IRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(MessageBusWriterBolt.class);

    private OutputCollector _collector;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }
    
    @Override
    public void execute(Tuple tuple) {
        String doc = tuple.getString(0);
        LOG.info("*************** MessageBusWriter execute " + doc);
        _collector.ack(tuple);
    }
    
    @Override
    public void cleanup() {}
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("dc"));
    }
    
    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
