This is a super-cool Vert.x demo project!

```mermaid
flowchart BT
	classDef ext font-size:x-small, color:#888
	app[fas:fa-bolt app]
	demo-core-ext[fas:fa-link +
		grails-datastore-gorm-hibernate5
		groovy
		groovy-dateutil
		groovy-json
		groovy-yaml]:::ext
	demo-core[fas:fa-book demo-core]
	domain-ext[fas:fa-link +
		h2
		jackson-databind
		log4j-over-slf4j
		logback-classic
		vertx-core
		vertx-hazelcast
		vertx-health-check
		vertx-web]:::ext
	domain[fas:fa-book domain]
	mothership[fas:fa-icons mothership]
	time-service[fas:fa-bolt time-service]
	weather-service-ext[fas:fa-link vertx-web-client]:::ext
	weather-service[fas:fa-bolt weather-service]
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
