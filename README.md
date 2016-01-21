Demo storm bolt (FacebookAnalyticsMysqlBolt) which inserts platform data from the data pipeline to a local mysql database

FacebookAnalyticsMysqlBolt
------------

At its essence `FacebookAnalyticsMysqlBolt` transforms platform data into sql INSERT statements:

```json
{
  "$schema":"http://schemas.socialcodedev.com/facebook/pages.1.json",
  "data":{
    "username":"AveenoCanada",
    "name":"Aveeno Canada",
    "picture":"https://scontent.xx.fbcdn.net/hprofile-xpt1/v/t1.0-1/10456259_802068543163417_6097620934072419937_n.png?oh=728afbe7f11f798fe645676249932087&oe=571E2D60",
    "talking_about_count":133,
    "cover":"https://scontent.xx.fbcdn.net/hphotos-xtf1/v/t1.0-9/s720x720/11046195_816752981694973_6836084116923669429_n.png?oh=b909f55ead9666c0a6105142b5b5452b&oe=5710E295",
    "link":"https://www.facebook.com/AveenoCanada/",
    "likes":105861,
    "id":"196989827004628",
    "is_published":true
  },
  "metadata":{
    "environment":"facebook",
    "crawl_id":"0c52e914-afa8-11e5-aca1-22000b4084ee",
    "guid":"0ca4bf46-afa8-11e5-9774-22000b2687d8",
    "crawl_time":"2015-12-31T10:20:00+00:00"
  }
}
```

```sql
INSERT INTO facebook_analytics_service_facebookpage (id, name, username, link, is_published, likes, talking_about_count,
                                                     cover, picture, should_crawl)
VALUES (?,?,?,?,?,?,?,?,?,TRUE)
ON DUPLICATE KEY UPDATE id=VALUES(id), name=VALUES(name), username=VALUES(username), link=VALUES(link),
                        is_published=VALUES(is_published), likes=VALUES(likes), talking_about_count=VALUES(talking_about_count),
                        cover=VALUES(cover), picture=VALUES(picture), should_crawl=VALUES(should_crawl)
```


Docker
------------
To run the demo storm cluster using docker do:
```
docker build -t ana-storm . && docker run ana-storm
```

You should see a document being written to the mysql database:

```
...
32002 [main] INFO  backtype.storm.testing - Done shutting down in process zookeeper
32003 [main] INFO  backtype.storm.testing - Deleting temporary path /tmp/b493a85a-ab46-475f-9185-cecb2909de94
32006 [main] INFO  backtype.storm.testing - Deleting temporary path /tmp/69cdcb41-3c73-4524-8ac9-3d4be0e2e3de
32008 [main] INFO  backtype.storm.testing - Deleting temporary path /tmp/df40f324-d711-432e-aa41-20e28f03b50c
32011 [main] INFO  backtype.storm.testing - Deleting temporary path /tmp/a1f7e127-cb10-4aa8-b595-a2fd8ff655f0
*************************** 1. row ***************************
                        id: 196989827004628
                      name: Aveeno Canada
                      link: https://www.facebook.com/AveenoCanada/
              is_published: 1
                     likes: 105861
       talking_about_count: 133
                     cover: https://scontent.xx.fbcdn.net/hphotos-xtf1/v/t1.0-9/s720x720/11046195_816752981694973_6836084116923669429_n.png?oh=b909f55ead9666c0a6105142b5b5452b&oe=5710E295
                   picture: https://scontent.xx.fbcdn.net/hprofile-xpt1/v/t1.0-1/10456259_802068543163417_6097620934072419937_n.png?oh=728afbe7f11f798fe645676249932087&oe=571E2D60
         page_access_token: NULL
    last_crawled_opengraph: NULL
last_discovered_adaccounts: NULL
                  username: AveenoCanada
              should_crawl: 1
```