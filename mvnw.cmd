@echo off
setlocal
set "BASE_DIR=%~dp0"
set "PROJECT_DIR=%BASE_DIR:~0,-1%"
set "WRAPPER_JAR=%BASE_DIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
if not exist "%WRAPPER_JAR%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
  if errorlevel 1 exit /b 1
)
java "-Dmaven.multiModuleProjectDirectory=%PROJECT_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %errorlevel%
