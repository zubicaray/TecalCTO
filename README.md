# TecalCPO
Ordonnanceur pour anodisation

## ALGO GENERAL

1.  **TecalOrdo.preparesZones**: préparation des zones de chaque job 
	1. **JobType.computeCoords**: 	organise les indexes des zones par pont
		- identification de l'indexe anodisation et indexe colmatage
	2. **JobType.addIntervalForModel()**
		- on créé les zones temporelles avec leur temps de déplacement, égouttage, etc ...
		- on crée les contraintes de cumul pour les zones à machines virtuellement multiples (ex anodisation = C13->C15, machine de capacité 3)
		- gestion de machine virtuellement unique mais pouvant appartenir à une multiple:
		    ex: un zone comme C32  doit ajouter un demande sur la containtes de cumuls de la zone C31->C32
	3. **JobType.simulateBridgesMoves()**
		- pour chaque job/gamme on créé les zones **"mvts"** englobant les mouvements des ponts -> mécaniquement les autres mouvements "inter jobs" pourront se faire en dehors de ces mvts
	4. **JobType.makeSafetyBetweenBridges()**		
		- regroupement des **"zones sécurités"** qui pourraient être trop proches de zones d'autre jobs sur le pont adverse 
2. **TecalOrdo.jobConstraints()**:
	non croisement des taches de chaque Job et étaliblessement de zones de "cumuls" avec leurs capacités respectives	
	
3. **TecalOrdo.jobsPrecedence()**:
	pour chaque job:
    - chaque tâches est exécutée l'une après l'autre en respectant l'ordre de la gamme    
    - le debut de la zone suivante doit être compris entre le début et la fin de la dérive + le temps incomprésible du déplacement du pont
		
4. **TecalOrdo.bridgesConstraints()**:
	toutes les zones "mvts" des job ne peuvent se croiser
5. **TecalOrdo.brigesSecurity()**:	
	toutes les zones "zones sécurités" ne peuvent se croiser

## Gestion des barres en cours
une fois qu'une barre est démarrée, *ie* le curseur a passé, la fin de la zone de chargement:
- toutes ses zones sont loggées dans la table CPO_LOGS
- son job passe de mJobsFuturs à mJobsEnCours, la struture des jobs dont les zones sont fixés une fos pour toutes dans le temps
- ses données temporelles (**AssignedTask**), calculées grace à Google OR, passent de **mAssignedTasksByBarreId** à **mPassedTasksByBarreId**:
    avec, on pourra ainsi recalculer des **FixeIntervalVar** pour le job en cours à chaque fois que l'on va générer d'autres ordonnancements
    	
  	
