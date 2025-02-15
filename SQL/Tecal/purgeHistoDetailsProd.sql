USE [ANODISATION]
GO
/****** Object:  StoredProcedure [dbo].[purgeDetailsProd]    Script Date: 20/12/2023 23:44:24 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[purgeDetailsProd] 
	
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	declare @numfiche varchar(50)
	select @numfiche=max(NumFicheProduction) from DetailsFichesProduction 
	where DateEntreePoste <  DATEADD(month,-1,GETDATE())
	

	delete from  DetailsGammesProduction where NumFicheProduction <@numfiche
	delete from  DetailsChargesProduction where NumFicheProduction <@numfiche
	delete from  DetailsPhasesProduction where NumFicheProduction <@numfiche
	delete from  DetailsFichesProduction where NumFicheProduction <@numfiche
	
END
