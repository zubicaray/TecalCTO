SELECT [NumZone] ,CodeZone     ,NbrPostes
      ,[SecuritePonts]
  FROM [ANODISATION].[dbo].[Zones]
  where numzone in (12,13,14,15,16,17);


update [ANODISATION].[dbo].[Zones]
  set NbrPostes=3
  where numzone in (15);

  update [ANODISATION].[dbo].[Zones]
  set SecuritePonts=1
  where numzone in (12,13,17);


    update [ANODISATION].[dbo].[Zones]
  set SecuritePonts=0
  where numzone in (12);