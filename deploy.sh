#!/bin/bash

# Variables
FRAMEWORK_JAR="target/framework-1.0-SNAPSHOT.jar"
DEST_JAR_NAME="framework.jar"
TEST_APP_LIB="../framework_test/lib" # Chemin vers le dossier lib de ton app de test

echo "🧹 ⚙️ Nettoyage, téléchargement des dépendances et compilation via Maven..."
mvn clean package

if [ $? -eq 0 ]; then
    echo "🚚 Copie du nouveau JAR Maven vers l'application de test..."
    mkdir -p $TEST_APP_LIB
    
    # On copie le jar généré par Maven dans la cible en le renommant "framework.jar"
    cp $FRAMEWORK_JAR $TEST_APP_LIB/$DEST_JAR_NAME
    
    # On garde aussi une copie à la racine si besoin
    cp $FRAMEWORK_JAR ./$DEST_JAR_NAME
    
    echo "✅ Framework JAR généré par Maven et copié avec succès !"
else
    echo "❌ Erreur lors de la compilation Maven du framework."
    exit 1
fi