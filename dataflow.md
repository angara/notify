# Angara.Met: data flow structures

## Redis keys

- `event_queue`  
  inbound event queue

- `event_queue_seq`  
  eid sequence counter

- `notify_user_queue`  

- `notify_user_queue_deleyed`  

- `notify_user_data:<user_id>`  
  lst of user related notifications

### Event structure

Basic fields:

```javascript
{
  eid:  "seq++",
  ts:   "milliseconds",
  type: "event_type",
  // payload: { ... }
}
```

#### Forum message event

```javascript
{
  eid,
  ts,
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
  eid,
  ts,
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

#### Private message notify

```javascript
{
  eid, ts,
  type: "private_message",
  private_message: {
    user_id: "\d+",
    to_id: "\d+",
    text: "...."
  }
}
```
