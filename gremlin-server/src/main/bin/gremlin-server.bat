:: Windows launcher script for Gremlin Server
@echo off

set work=%CD%

if [%work:~-3%]==[bin] cd ..

set LIBDIR=lib

set JAVA_OPTIONS=-Xms32m -Xmx512m

:: Launch the application
java -Dlog4j.configuration=../config/log4j-server.properties %JAVA_OPTIONS% %JAVA_ARGS% -cp %LIBDIR%/*; com.tinkerpop.gremlin.server.GremlinServer %* 