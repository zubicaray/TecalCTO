#!/bin/bash

# Nombre de lignes par transaction#!/bin/bash
# Variables
INPUT_FILE="exportDetailsProd.sql"    # Nom du fichier d'entrée
OUTPUT_DIR="splitted_files"      # Répertoire pour les fichiers splittés
LINES_PER_FILE=1000             # Nombre de lignes par sous-fichier
SQL_SERVER="localhost"           # Nom ou adresse du serveur SQL
DATABASE="ANODISATION"    # Nom de la base de données
USERNAME="sa"      # Nom d'utilisateur SQL
PASSWORD="Jeff_nenette"       # Mot de passe SQL

# Vérification que le fichier d'entrée existe
if [[ ! -f "$INPUT_FILE" ]]; then
    echo "Erreur : Le fichier $INPUT_FILE n'existe pas."
    exit 1
fi

if [[  $1 = 'PURGE' ]]; then
    echo "PURGE DES TABLES AVANT INSERTS"
    sqlcmd -S "$SQL_SERVER" -d "$DATABASE" -U "$USERNAME" -P "$PASSWORD" -i ../TRUNCATE_DETAILS.sql -C
    exit 1
fi


# Création ou nettoyage du répertoire de sortie
if [[ -d "$OUTPUT_DIR" ]]; then
    rm -rf "$OUTPUT_DIR"/*
else
    mkdir "$OUTPUT_DIR"
fi

# Diviser le fichier en sous-fichiers de 10 000 lignes
split -l "$LINES_PER_FILE" "$INPUT_FILE" "$OUTPUT_DIR/part_"

# Vérifier les fichiers splittés
split_files=("$OUTPUT_DIR"/part_*)
if [[ ${#split_files[@]} -eq 0 ]]; then
    echo "Erreur : Aucun fichier n'a été généré lors du split."
    exit 1
fi

# Exécuter chaque sous-fichier avec sqlcmd
for file in "${split_files[@]}"; do
    echo "Exécution de $file..."
    sqlcmd -S "$SQL_SERVER" -d "$DATABASE" -U "$USERNAME" -P "$PASSWORD" -i "$file" -C
    
    if [[ $? -ne 0 ]]; then
        echo "Erreur lors de l'exécution du fichier $file. Arrêt du script."
        exit 1
    fi
done

# Résultat
echo "Toutes les parties ont été exécutées avec succès."
