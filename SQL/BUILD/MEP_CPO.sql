
CREATE TABLE [dbo].[TempsDeplacements](
	[depart] [smallint] NOT NULL,
	[arrivee] [smallint] NOT NULL,
	[lent] [smallint] NOT NULL,
	[normal] [smallint] NOT NULL,
	[rapide] [smallint] NOT NULL
) ON [PRIMARY]




CREATE TABLE [dbo].[CalibrageTempsGammes](
	[NumGamme] [nchar](6) NOT NULL,
	[NumFicheProduction] [nchar](10) NOT NULL,
	[date] [date] NOT NULL,
 CONSTRAINT [PK_CalibrageTempsGammes] PRIMARY KEY CLUSTERED 
(
	[NumGamme] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

DROP  TABLE IF EXISTS [dbo].[Zones];

CREATE TABLE [dbo].[Zones](
	[ClePrimaire] [int] NOT NULL,
	[NumZone] [smallint] NOT NULL,
	[CodeZone] [varchar](15) NOT NULL,
	[LibelleZone] [varchar](50) NOT NULL,
	[NumPremierPoste] [smallint] NOT NULL,
	[NomPremierPoste] [varchar](6) NOT NULL,
	[NumDernierPoste] [smallint] NOT NULL,
	[NomDernierPoste] [varchar](6) NOT NULL,
	[NbrPostes] [smallint] NOT NULL,
	[derive] [smallint] NULL,
	[SecuritePonts] [smallint] NULL
) ON [PRIMARY]
GO
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (1, 1, N'CHGT1 à CHGT2', N'Postes de chargement', 1, N'CHGT1', 2, N'CHGT2', 2, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (7, 2, N'C00', N'Dégraissage ', 5, N'C00', 5, N'C00', 1, 200, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (8, 3, N'DEC', N'Décapage', 6, N'DEC', 6, N'DEC', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (9, 4, N'SAT', N'Satinage', 7, N'SAT', 7, N'SAT', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (10, 5, N'C03', N'Rinçage soude', 8, N'C03', 8, N'C03', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (11, 6, N'C04', N'Rinçage soude/dégraissage ', 9, N'C04', 9, N'C04', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (12, 7, N'C05', N'Dégraissage acide', 10, N'C05', 10, N'C05', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (13, 8, N'C06', N'Rinçage Mt brillantage', 11, N'C06', 11, N'C06', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (14, 9, N'C07', N'Brillantage', 12, N'C07', 12, N'C07', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (15, 10, N'C08', N'Rinçage brillantage', 13, N'C08', 13, N'C08', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (16, 11, N'C09', N'Rinçage brillantage', 14, N'C09', 14, N'C09', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (17, 12, N'C10', N'Blanchiment', 15, N'C10', 15, N'C10', 1, 150, 1)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (18, 13, N'C11', N'Rinçage', 16, N'C11', 16, N'C11', 1, 0, 1)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (19, 14, N'C12', N'Rinçage', 17, N'C12', 17, N'C12', 1, 0, 1)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (21, 15, N'C13 à C15', N'Anodisation', 18, N'C13', 20, N'C15', 3, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (22, 16, N'C17', N'Rinçage anodisation', 22, N'C17', 22, N'C17', 1, 0, 1)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (23, 17, N'C18', N'Rinçage anodisation', 23, N'C18', 23, N'C18', 1, 0, 1)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (24, 18, N'C19', N'Spectrocoloration', 24, N'C19', 24, N'C19', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (25, 19, N'C20', N'Rinçage ', 25, N'C20', 25, N'C20', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (26, 20, N'C21', N'Rinçage ', 26, N'C21', 26, N'C21', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (27, 21, N'C22', N'Coloration or', 27, N'C22', 27, N'C22', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (28, 22, N'C23', N'Coloration orange', 28, N'C23', 28, N'C23', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (29, 23, N'C24', N'RESERVE 2', 29, N'C24', 29, N'C24', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (30, 24, N'C25', N'Imprégnation à froid', 30, N'C25', 30, N'C25', 1, 120, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (31, 25, N'C26', N'Rinçage imprégnation', 31, N'C26', 31, N'C26', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (32, 26, N'C27', N'Imprégnation à froid', 32, N'C27', 32, N'C27', 1, 120, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (33, 27, N'C28', N'Coloration noire', 33, N'C28', 33, N'C28', 1, 180, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (34, 28, N'C29', N'Rinçage noir', 34, N'C29', 34, N'C29', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (35, 29, N'C30', N'Eau dure / imprégnation', 35, N'C30', 35, N'C30', 1, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (36, 30, N'C31', N'Colmatage à chaud', 36, N'C31', 36, N'C31', 1, 800, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (37, 31, N'C32', N'Colmatage chaud', 37, N'C32', 37, N'C32', 1, 800, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (38, 32, N'C31 à C32', N'Colmatage chaud (permutation)', 36, N'C31', 37, N'C32', 2, 800, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (39, 33, N'C33', N'Conversion chimique', 38, N'C33', 38, N'C33', 1, 800, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (40, 34, N'C35', N'Rinçage totale', 40, N'C35', 40, N'C35', 1, 180, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (41, 35, N'D1 à D2', N'Postes de déchargement ', 41, N'D1 ', 42, N' D2', 2, 0, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (42, 36, N'C34', N'Rinçage totale', 39, N'C34', 39, N'C34', 1, 180, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (44, 37, N'C37', N'Etuve', 43, N'C37', 43, N'C37', 1, 1800, 0)
INSERT [dbo].[Zones] ([ClePrimaire], [NumZone], [CodeZone], [LibelleZone], [NumPremierPoste], [NomPremierPoste], [NumDernierPoste], [NomDernierPoste], [NbrPostes], [derive], [SecuritePonts]) VALUES (45, 38, N'C38', N'Basculeur', 44, N'C38', 44, N'C38', 1, 0, 0)



