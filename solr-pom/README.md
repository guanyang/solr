###install到本地仓库：
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=UTF-8 -Dmaven.javadoc.skip=true -U -T 1C -Pprod
###deploy到私服：
mvn clean deploy -Dmaven.test.skip=true -Dfile.encoding=UTF-8 -Dmaven.javadoc.skip=true -U -T 1C -Pprod

###查询jboss日志
find /opt/jboss/domain/servers/ -path '*/server*/*' -name 'server.log.2016-08-04-*'|xargs grep 'ERROR'

###查询业务日志，区分gem-web.log、gem-wap.log、gem-service.log、gem-admin.log
find /opt/logs/gem/ -path '*/*' -name 'gem-admin.log'|xargs grep 'ERROR'
