#!/bin/bash
echo "Eliminando metadatos corruptos de Maven..."
rm -rf "$HOME/.m2/repository/com/driagon/services/spring-boot-error-handler-starter"
echo "Archivos eliminados. Ahora ejecuta 'mvn clean install' nuevamente."
