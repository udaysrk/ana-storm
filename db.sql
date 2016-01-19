CREATE DATABASE facebook_analytics;

USE facebook_analytics;

CREATE TABLE `facebook_analytics_service_facebookpage` (
  `id` varchar(128) NOT NULL,
  `name` varchar(128) NOT NULL,
  `link` varchar(255) NOT NULL,
  `is_published` tinyint(1) NOT NULL,
  `likes` int(11) DEFAULT NULL,
  `talking_about_count` int(11) DEFAULT NULL,
  `cover` varchar(500) DEFAULT NULL,
  `picture` varchar(500) DEFAULT NULL,
  `page_access_token` varchar(256) DEFAULT NULL,
  `last_crawled_opengraph` datetime DEFAULT NULL,
  `last_discovered_adaccounts` datetime DEFAULT NULL,
  `username` varchar(128),
  `should_crawl` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `facebook_analytics_service_fa_should_crawl_2c92143954dbeafa_uniq` (`should_crawl`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;