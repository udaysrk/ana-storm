INSERT INTO facebook_analytics_service_facebookpage (id, name, username, link, is_published, likes, talking_about_count,
                                                     cover, picture, should_crawl)
VALUES (?,?,?,?,?,?,?,?,?,TRUE)
ON DUPLICATE KEY UPDATE id=VALUES(id), name=VALUES(name), username=VALUES(username), link=VALUES(link),
                        is_published=VALUES(is_published), likes=VALUES(likes), talking_about_count=VALUES(talking_about_count),
                        cover=VALUES(cover), picture=VALUES(picture), should_crawl=VALUES(should_crawl)