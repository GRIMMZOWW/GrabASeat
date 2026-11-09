@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Optional ENV vars
@REM   JAVA_HOME - location of a JDK home dir
@REM   MAVEN_BATCH_ECHO - set to 'on' to enable batch echo
@REM   MAVEN_BATCH_PAUSE - set to 'on' to run with pause
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM ----------------------------------------------------------------------------

@IF "%MAVEN_BATCH_ECHO%"=="on" echo %MAVEN_BATCH_ECHO%

@REM Set %HOME% to equivalent of $HOME
@IF "%HOME%"=="" (SET "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
@IF EXIST "%USERPROFILE%\mavenrc_pre.bat" CALL "%USERPROFILE%\mavenrc_pre.bat" %*
@IF EXIST "%HOME%\mavenrc_pre.bat" CALL "%HOME%\mavenrc_pre.bat" %*

@REM find the project base directory, i.e. the directory that contains the folder ".mvn".
set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    SET MVNW_REPOURL=https://repo.maven.apache.org/maven2
    IF NOT "%MVNW_REPOURL%"=="" (
        SET MVNW_REPOURL=%MVNW_REPOURL%
    )
    SET DOWNLOAD_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
    FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
        IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
    )
    %JAVA_HOME%\bin\java -cp "" org.apache.maven.wrapper.MavenWrapperMain %* 2>NUL
    IF ERRORLEVEL 1 (
        IF "%MVNW_VERBOSE%"=="true" echo "Couldn't use Java to download maven-wrapper.jar; downloading with PowerShell..."
        powershell -Command "&{"^
            "$webclient = new-object System.Net.WebClient;"^
            "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
            "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
            "}"^
            "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar')"^
            "}"
        IF "%MVNW_VERBOSE%"=="true" (
            echo Finished downloading maven-wrapper.jar
        )
    )
)

@REM Start JAVA_HOME detection
SET JAVA_HOME_TEMP=%JAVA_HOME%
IF "%JAVA_HOME%"=="" (
    SET JAVA_HOME_TEMP=%JAVA_HOME%
)

SET MAVEN_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
FOR %%i IN (java.exe) DO (
    IF EXIST "%%~$PATH:i" SET MAVEN_JAVA_EXE="%%~$PATH:i"
)

set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set DOWNLOAD_URL=

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

%MAVEN_JAVA_EXE% ^
  -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %MAVEN_OPTS% ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
IF ERRORLEVEL 1 GOTO error
GOTO end

:error
SET ERROR_CODE=1

:end
@ENDLOCAL & SET ERROR_CODE=%ERROR_CODE%

IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRcPost
@REM check for post script, once with legacy .bat ending and once with .cmd ending
IF EXIST "%USERPROFILE%\mavenrc_post.bat" CALL "%USERPROFILE%\mavenrc_post.bat"
IF EXIST "%HOME%\mavenrc_post.bat" CALL "%HOME%\mavenrc_post.bat"
IF EXIST "%USERPROFILE%\mavenrc_post.cmd" CALL "%USERPROFILE%\mavenrc_post.cmd"
IF EXIST "%HOME%\mavenrc_post.cmd" CALL "%HOME%\mavenrc_post.cmd"
:skipRcPost

IF "%MAVEN_BATCH_PAUSE%"=="on" PAUSE

EXIT /B %ERROR_CODE%

