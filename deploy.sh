#!/bin/bash

# -------------------------------
# Variables
# -------------------------------
JAR_NAME="framework.jar"
SRC_DIR="src/main/java"
BUILD_DIR="build_jar"

# Chemin vers l'API Servlet pour pouvoir compiler
SERVLET_API_JAR="../framework_test/lib/servlet-api.jar"

# -------------------------------
# 1. Nettoyage et création des dossiers
# -------------------------------
echo "🧹 Nettoyage des anciens dossiers de build..."
rm -rf $BUILD_DIR
rm -f $JAR_NAME
mkdir -p $BUILD_DIR

# -------------------------------
# 2. Compilation des fichiers Java
# -------------------------------
echo "⚙️ Compilation du code source Java..."
# Trouve tous les fichiers .java dans le dossier src/main/java
find $SRC_DIR -name "*.java" > sources.txt

# Compile les fichiers présents dans sources.txt directement vers build_jar
javac -cp "$SERVLET_API_JAR" -d $BUILD_DIR @sources.txt
rm sources.txt

# -------------------------------
# 3. Création du fichier .jar
# -------------------------------
echo "📦 Compression des fichiers .class en $JAR_NAME..."
cd $BUILD_DIR || exit

# Crée le fichier .jar à la racine du projet à partir des .class compilés
jar -cvf ../$JAR_NAME *
cd ..

echo "--------------------------------------------------"
echo "✅ Succès ! Votre bibliothèque est prête : ./$JAR_NAME"
echo "--------------------------------------------------"