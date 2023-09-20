This is a super-cool Vert.x demo project!

```mermaid

flowchart BT
	classDef ext font-size:x-small, color:#888
	app[#128498; app]
	demo-core-ext[#128279; +<br>grails-datastore-gorm-hibernate5<br>groovy<br>groovy-dateutil<br>groovy-json<br>groovy-yaml]:::ext
	demo-core[#128218; demo-core]
	domain-ext[#128279; +<br>h2<br>jackson-databind<br>log4j-over-slf4j<br>logback-classic<br>spock-core<br>vertx-core<br>vertx-hazelcast<br>vertx-health-check<br>vertx-web]:::ext
	domain[#128218; domain]
	mothership[#128760; mothership]
	time-service[#128498; time-service]
	weather-service-ext[#128279; vertx-web-client]:::ext
	weather-service[#128498; weather-service]
	app --> domain
	demo-core .-> demo-core-ext
	domain --> demo-core
	domain .-> domain-ext
	mothership --> domain
	mothership --> app
	mothership --> time-service
	mothership --> weather-service
	time-service --> domain
	weather-service --> domain
	weather-service .-> weather-service-ext
```
