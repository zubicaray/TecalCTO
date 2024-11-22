

update [ANODISATION_SECOURS].[dbo].[Zones] 
set nonchevauchementpont=0;


-- C00 dérive  60 sec
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=600 where numzone=2;
--DEC 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=30 where numzone=3;
--SAT 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=30 where numzone=4;
----------------------------------------------------------------------------------------------
-- C03 dérive  60 sec
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=60 where numzone=5;
-- C04 4mn
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=240 where numzone=6;
----------------------------------------------------------------------------------------------
--C05 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=60 where numzone=7
----------------------------------------------------------------------------------------------
--C06
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=180 where numzone=8;
--C07
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=20 where numzone=9;
--C08
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=0 where numzone=10;

--C09 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=0 where numzone=11;
----------------------------------------------------------------------------------------------
--C10 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=120 where numzone=12;
----------------------------------------------------------------------------------------------
--C11 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=0 where numzone=13;
--C12
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=1,derive=0 where numzone=14;
----------------------------------------------------------------------------------------------
--C13-15 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=30 where numzone=15;
----------------------------------------------------------------------------------------------

--C17
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=60 where numzone=16;
--C18
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=60 where numzone=17;
--C19 ???????????
--C20
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=60 where numzone=19;
--C21
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=180 where numzone=20;

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------
--C26 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=180 where numzone=25;
--C30
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=120 where numzone=29;
------------------------------------------------------------------------------------------------
--29
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=120 where numzone=28;
------------------------------------------------------------------------------------------------
--C34
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=180 where numzone=36;
--C35
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=2,derive=180 where numzone=34;
----------------------------------------------------------------------------------------------
-- CHEVAUCHEMENT POSSIBLE
------------------------------------------------------------------------------------------------
--C22 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=15 where numzone=21;
--C23 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=15 where numzone=22;
--C25 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=180 where numzone=24;
--C27 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=180 where numzone=26;
--C28 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=180 where numzone=27;
--C31 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=600 where numzone=30;
--C32 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=600 where numzone=31;
--C31-32 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=600 where numzone=32;
--C33 
update [ANODISATION_SECOURS].[dbo].[Zones] set  nonchevauchementpont=0,derive=30 where numzone=33;

------------------------------------------------------------------------------------------------


select numzone,codezone,libellezone from [ANODISATION_SECOURS].[dbo].[Zones] 
order by numzone

select * from [ANODISATION_SECOURS].[dbo].[Zones] 
order by numzone