@echo off
echo Eliminando metadatos corruptos de Maven...
rd /s /q "C:\Users\gadri\.m2\repository\com\driagon\services\spring-boot-error-handler-starter"
echo Archivos eliminados. Ahora ejecuta 'mvn clean install' nuevamente.
pause
