@echo off
setlocal
setlocal ENABLEEXTENSIONS
if "%1" == "/?" goto :help
if "%1" == "/help" goto :help
if "%1" == "-?" goto :help
if "%1" == "--help" goto :help
goto :start

:help
echo "Serializes" and possibly also converts a list of files to RDF-XML.
echo List as many input files as you like on the command line.
echo Source file format is deduced from input filename extension.
echo Output files created in the current directory.
echo If format being changed, output file will have a ".owl" extension.
echo WARNING! If a source file is in the current directory and has extension .owl, it will be replaced with the output!
goto :end

:start
  set I=%1
  if "%I%"=="" goto :end
  for %%a in ("%I%") do (
    java -jar %RDF_TOOLKIT_JAR% --source "%%a" --target "%%~na%.owl" --target-format rdf-xml --use-dtd-subset -ibn -ibi -sdt explicit
  )
  shift
  goto :start

:end
