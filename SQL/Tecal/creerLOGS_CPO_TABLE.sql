use ANODISATION;
--select * from [ANODISATION].[dbo].[LOGS_CPO];


SELECT Z.NumZone,idbarre, Z.CodeZone, label, entree, sortie,
 CONVERT(CHAR(8), entree, 108) AS Entree2,
    CONVERT(CHAR(8), sortie, 108) AS Sortie2
FROM   Zones Z   INNER JOIN LOGS_CPO L
ON L.NumZone = Z.NumZone --and CAST(date_log AS DATE) ='2024-12-02'
ORDER BY idbarre

select distinct numzone,codezone from zones order by numzone;

ALTER TABLE ANODISATION.dbo.LOGS_CPO
  ADD derive INT NOT NULL DEFAULT(1) ;

  ALTER TABLE ANODISATION.dbo.LOGS_CPO
  ADD descente INT NOT NULL DEFAULT(1) ;

  
  ALTER TABLE ANODISATION.dbo.LOGS_CPO
  ADD montee INT NOT NULL DEFAULT(1) ;


truncate table ANODISATION.dbo.LOGS_CPO;
--DROP  TABLE LOGS_CPO ;

CREATE TABLE LOGS_CPO (
    date_log DATETIME NOT NULL,               -- Clé primaire (partie 1)
    idbarre NUMERIC(10, 0) NOT NULL,      -- Clé primaire (partie 2)
    label VARCHAR(255) NULL,              -- Champ texte
    idZone INT NOT NULL,  
    NumZone INT NOT NULL,                     -- Champ entier
    entree DATETIME NOT NULL,                     -- Heure d'entrée
    sortie DATETIME NOT NULL,                     -- Heure de sortie,
    PRIMARY KEY (date_log, idbarre,idZone)       -- Clé primaire composée
);

-- Index supplémentaire (facultatif, car idbarre fait déjà partie de la clé primaire)
CREATE INDEX idx_label ON LOGS_CPO (label);



SELECT 
    l1.NumZone AS NumZone1,
    l2.NumZone AS NumZone2,
    l1.entree AS Entree1,
    l1.sortie AS Sortie1,
    l2.entree AS Entree2,
    l2.sortie AS Sortie2
FROM 
    LOGS_CPO l1
JOIN 
    LOGS_CPO l2
ON 
    l1.NumZone = l2.NumZone -- Même NumZone
    AND l1.date_log = l2.date_log -- Même jour
    AND l1.idbarre < l2.idbarre -- Éviter les doublons et comparer différentes barres
    --AND    DATEDIFF (SECOND ,l1.entree ,l2.sortie) >0 -- Vérifie le chevauchement
    AND
    Sum(Left(l1.entree,2) * 3600 + substring(l1.entree, 4,2) * 60 + substring(l1.entree, 7,2))  
    > Sum(Left(l2.sortie,2) * 3600 + substring(l2.sortie, 4,2) * 60 + substring(l2.sortie, 7,2))
    
WHERE     CAST(l1.date_log AS DATE) = '2024-11-30' -- Filtrer par date
ORDER BY 
    l1.NumZone, l1.entree, l2.entree;

    WITH LogsInSeconds AS (
    SELECT 
        NumZone,
        idbarre,
        entree,sortie,
        CAST(date_log AS DATE) AS DateLog,
        DATEDIFF(SECOND, '00:00:00', entree) AS EntreeInSeconds,
        DATEDIFF(SECOND, '00:00:00', sortie) AS SortieInSeconds
    FROM LOGS_CPO
)
SELECT 
    l1.NumZone AS NumZone1,
    l2.NumZone AS NumZone2,
    l1.entree AS Entree1,
    l1.sortie AS Sortie1,
    l2.entree AS Entree2,
    l2.sortie AS Sortie2
    /*
    l1.EntreeInSeconds AS Entree1,
    l1.SortieInSeconds AS Sortie1,
    l2.EntreeInSeconds AS Entree2,
    l2.SortieInSeconds AS Sortie2
    */
FROM 
    LogsInSeconds l1
JOIN 
    LogsInSeconds l2
ON 
    l1.NumZone = l2.NumZone -- Même zone
    AND l1.DateLog = l2.DateLog -- Même jour
    AND l1.idbarre < l2.idbarre -- Comparer uniquement des paires différentes
    AND (
        -- Logique des chevauchements en secondes
        l1.EntreeInSeconds <= l2.SortieInSeconds 
        AND 
        l1.SortieInSeconds >= l2.EntreeInSeconds
    )
WHERE 
    l1.DateLog = '2024-11-30' -- Filtrer par date
ORDER BY 
    l1.NumZone, l1.EntreeInSeconds, l2.EntreeInSeconds;