@ECHO OFF
SET APP_HOME=%~dp0
IF EXIST "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" (
  java -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
  EXIT /B %ERRORLEVEL%
)
ECHO gradle-wrapper.jar mangler. Aapne prosjektet i Android Studio eller kjoer: gradle wrapper --gradle-version 9.6.0
EXIT /B 1
