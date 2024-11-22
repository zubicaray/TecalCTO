#!/bin/bash

# Fichier d'entrée et fichier de sortie
INPUT_FILE="exportDetailsProd.sql"
OUTPUT_FILE="detailsWithTransac.sql"

# Nombre de lignes par transaction
LINES_PER_TRANSACTION=10000

# Variables pour le traitement
line_count=0
transaction_count=0

# Vérification que le fichier d'entrée existe
if [[ ! -f "$INPUT_FILE" ]]; then
    echo "Erreur : Le fichier $INPUT_FILE n'existe pas."
    exit 1
fi

# Création ou réinitialisation du fichier de sortie
echo "-- Script généré avec transactions" > "$OUTPUT_FILE"

# Lecture ligne par ligne du fichier d'entrée
while IFS= read -r line || [[ -n "$line" ]]; do
    # Si c'est le début d'une transaction, ajouter la commande BEGIN TRANSACTION
    if (( line_count % LINES_PER_TRANSACTION == 0 )); then
        echo "BEGIN TRANSACTION;" >> "$OUTPUT_FILE"
        ((transaction_count++))
    fi

    # Ajouter la ligne courante au fichier de sortie
    echo "$line" >> "$OUTPUT_FILE"

    # Si c'est la fin d'une transaction, ajouter la commande COMMIT
    if (( (line_count + 1) % LINES_PER_TRANSACTION == 0 )); then
        echo "COMMIT;" >> "$OUTPUT_FILE"
    fi

    # Incrémenter le compteur de lignes
    ((line_count++))
done < "$INPUT_FILE"

# Si le fichier ne termine pas sur un multiple exact, ajouter un dernier COMMIT
if (( line_count % LINES_PER_TRANSACTION != 0 )); then
    echo "COMMIT;" >> "$OUTPUT_FILE"
fi

# Afficher le résultat
echo "Traitement terminé. Fichier généré : $OUTPUT_FILE"
echo "Transactions ajoutées : $transaction_count"

