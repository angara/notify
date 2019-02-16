# Angara.Met: data flow structures

## Redis keys

- `event_stream`  
  inbound event queue

- `event_stream_seq`  
  eid sequence counter

- `user_notify`  
  key: "user:<user_id>"
  ttl?

### Event structure

Basic fields:

```javascript
{
  eid:  "seq++",
  ts:   "timestamp",
  type: "event_type",
  // payload: { ... }
}
```

#### Forum message event

```javascript
{
  eid, ts,
  type: "forum_msg",
  forum_msg: {
    msg_id: "\d+",
    user_id: "\d+",
    topic_id: "\d+",
    text: "...",
    file?: "???
  }
}
```

#### Forum topic event

```javascript
{
  eid, ts,
  type: "forum_topic",
  forum_topic: {
    user_id: "\d+",
    topic_id: "\d+",
    group_id: "\d+",
    title: "..."
    text: "...."
  }
}
```
