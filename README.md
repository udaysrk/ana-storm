Demo storm bolt (AnalyticsBolt) which inserts documents to a local mysql instance

Docker
------------
To run the app using docker do:
```
docker build -t ana-storm . && docker run ana-storm
```

You should see a document being written to the database:

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