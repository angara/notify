package anga;

import java.net.URI;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

public class RedisEventQueue 
{
  Jedis jedis;
  String queueName;
  String queueSeq;
  
  public RedisEventQueue(String uri, String queueName) throws Exception 
  {
    this.jedis     = new Jedis(new URI(uri));
    this.queueName = queueName;
    this.queueSeq  = queueName+"_seq";
  }

  public long getSeq() 
  {
    return jedis.incr(this.queueSeq);
  }

  public String send(String type, JSONObject data) 
  {
    String eid = Long.toString(this.getSeq());
    data.put("eid", eid);
    data.put("ts", System.currentTimeMillis());
    data.put("type", type);
    return eid + " " +jedis.lpush(this.queueName, data.toString());
  }
}

//.