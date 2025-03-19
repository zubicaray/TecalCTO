USE ANODISATION;
DROP TABLE vitesse_haut;
DROP TABLE vitesse_bas;

CREATE  TABLE vitesse_haut (
    id INT PRIMARY KEY,
    valeur INT NOT NULL,
    libelle varchar(30) NOT NULL
);

insert into vitesse_haut (id,valeur,libelle) values(0,17,'lente');
insert into vitesse_haut (id,valeur,libelle) values(1,0,'normale');
insert into vitesse_haut (id,valeur,libelle) values(2,-6,'rapide');


CREATE TABLE vitesse_bas (
    id INT PRIMARY KEY,
    valeur INT NOT NULL,
    libelle varchar(30) NOT NULL
);

    insert into vitesse_bas (id,valeur,libelle) values(0,17,'lente');
    insert into vitesse_bas (id,valeur,libelle) values(1,0,'normale');
    insert into vitesse_bas (id,valeur,libelle) values(2,-3,'rapide');


select distinct  
    DF.numficheproduction as [N° OF], 	DC.NumGammeANodisation as [gamme ],
    DC.NumBarre as  [barre]  , 
    VB.libelle as descente,
    VH.libelle as montee,
    dbo.hasBadCalibrage (DF.numficheproduction) as BAD_CALIB 
from  
    [DetailsFichesProduction] DF
    INNER JOIN  DetailsChargesProduction DC 	
        on DC.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS 
        and DC.numligne=1 and DF.NumLigne=1 
    INNER JOIN vitesse_bas VB on VB.id=DC.vitesse_bas
    INNER JOIN vitesse_haut VH on VH.id=DC.vitesse_haut
WHERE		
    DF.DateEntreePoste >=  '20250318'  and DF.DateEntreePoste < '20250319'      
order by DF.numficheproduction ,DC.NumBarre

