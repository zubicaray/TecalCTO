USE [ANODISATION_SECOURS]
GO

/****** Object:  Table [dbo].[CalibrageTempsGammes]    Script Date: 04/06/2024 10:57:34 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[CalibrageTempsGammes](
	[NumGamme] [nchar](6) NOT NULL,
	[NumFicheProduction] [nchar](10) NOT NULL,
	[date] [date] NOT NULL,
 CONSTRAINT [PK_CalibrageTempsGammes] PRIMARY KEY CLUSTERED 
(
	[NumGamme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

