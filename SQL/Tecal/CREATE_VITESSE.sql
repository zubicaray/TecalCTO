USE ANODISATION;
DROP TABLE vitesse_haut;
DROP TABLE vitesse_bas;

CREATE  TABLE vitesse_haut (
    id INT PRIMARY KEY,
    valeur INT NOT NULL
);

insert into vitesse_haut (id,valeur) values(0,17);
insert into vitesse_haut (id,valeur) values(1,0);
insert into vitesse_haut (id,valeur) values(2,-6);


CREATE TABLE vitesse_bas (
    id INT PRIMARY KEY,
    valeur INT NOT NULL
);

insert into vitesse_bas (id,valeur) values(0,17);
insert into vitesse_bas (id,valeur) values(1,0);
insert into vitesse_bas (id,valeur) values(2,-3);


