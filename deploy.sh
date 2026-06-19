#!/bin/bash

# Variables
FRAMEWORK_NAME="framework.jar"
SRC_DIR="src/main/java"
BUILD_DIR="build_jar"
TEST_APP_LIB="../framework_test/lib" # Chemin vers le dossier lib de ton app de test

echo "🧹 Nettoyage du dossier de build du framework..."
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

echo "⚙️ Compilation des classes du framework..."
# On compile en incluant le servlet-api dans le classpath pour éviter les erreurs d'import jakarta.servlet.*
javac -cp "lib/servlet-api.jar" -d $BUILD_DIR $(find $SRC_DIR -name "*.java")

if [ $? -eq 0 ]; then
    echo "📦 Création du fichier $FRAMEWORK_NAME..."
    cd $BUILD_DIR || exit
    jar -cvf ../$FRAMEWORK_NAME .
    cd ..

    echo "🚚 Copie du nouveau JAR vers l'application de test..."
    mkdir -p $TEST_APP_LIB
    cp $FRAMEWORK_NAME $TEST_APP_LIB/
    
    echo "✅ Framework JAR généré et copié avec succès !"
else
    echo "❌ Erreur lors de la compilation du framework."
    exit 1
fi