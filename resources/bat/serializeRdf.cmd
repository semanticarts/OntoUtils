@echo off
if "%1" == "/?" goto :help
if "%1" == "/help" goto :help
if "%1" == "-?" goto :help
if "%1" == "--help" goto :help
goto :start

:help
echo Converts any RDF to RDF-XML.
echo Follows UNIX filter conventions
echo List as many input files as you like on the command line.
echo All output will go to stdout.
echo Source file format is deduced from input filename extension.
echo You can make source format explicit with "--source-format=xxx" at the END of the command line.
goto :end

:start
REM Create a timestamp for use in the temporary filename. Gosh, this is one line in bash!
For /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
For /f "tokens=1-3 delims=: " %%a in ('time /t') do (set mytime12=%%a%%b%%c)
set TEMPFILE=%TEMP%\serializeRdf-%mydate%T%mytime12%%RANDOM%

java -jar %RDF_TOOLKIT_JAR% --source %* --target %TEMPFILE% --target-format rdf-xml --use-dtd-subset -ibn -ibi -sdt explicit
if %ERRORLEVEL% GEQ 1 goto :end
findstr "^" %TEMPFILE%
del %TEMPFILE%

:end
