package org.tecal.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.tecal.scheduler.data.SQL_DATA;

import com.formdev.flatlaf.json.ParseException;

@SuppressWarnings("serial")
public class CPO_LOGS_GANT extends JPanel {
    private final Date dDate;
    private Comparable<?> focusedSeriesKey = null;

    public CPO_LOGS_GANT(Date dDate) {
        this.dDate = dDate;
        initialize();
    }

    public static Map<Integer, String> loadZoneLabels() {
        Map<Integer, String> zoneLabels = new HashMap<>();

        String query = """
                SELECT DISTINCT numzone, codezone
                FROM zones
                ORDER BY numzone;
                """;

        try (
             PreparedStatement stmt = SQL_DATA.getInstance().getPreparedStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int numZone = rs.getInt("numzone");
                String codeZone = rs.getString("codezone");
                zoneLabels.put(numZone, codeZone);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return zoneLabels;
    }

    private void initialize() {

	Map<Integer, String> zoneLabels=loadZoneLabels();
    // Créer le dataset
    IntervalXYDataset dataset = getDatasetFromDatabase();

    // Créer le graphique
    JFreeChart chart = ChartFactory.createXYBarChart(
            "Diagramme de Gantt - LOGS_CPO", // Titre
            "Heures",                        // Axe des X (temps)
            false,                          // Axe des X n'est pas une date
            "NumZone",                      // Axe des Y (zones)
            dataset,                        // Données
            org.jfree.chart.plot.PlotOrientation.VERTICAL,  // Orientation
            true,                           // Inclure la légende
            true,                           // Infobulles
            false                           // Pas d'URLs
    );

    // Personnaliser le plot
    XYPlot plot = (XYPlot) chart.getPlot();
    XYBarRenderer renderer = new XYBarRenderer();

    // Configuration du renderer
    renderer.setUseYInterval(true);
    renderer.setShadowVisible(false);

    //renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
    renderer.setBarPainter(new StandardXYBarPainter());

    renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator() {
        private static final long serialVersionUID = 1L;

        @Override
        public String generateToolTip(XYDataset dataset, int series, int item) {
            // Récupérer les données d'entrée et de sortie
            XYIntervalSeriesCollection collection = (XYIntervalSeriesCollection) dataset;
            XYIntervalSeries xySeries = collection.getSeries(series);
            String label = xySeries.getKey().toString();
            // Obtenir les valeurs associées à cet item
            int start = (int) xySeries.getXLowValue(item);
            int end = (int) xySeries.getXHighValue(item);

            // Convertir les valeurs de temps
            String formattedStart = formatTime(new Time(start * 1000L));
            String formattedEnd = formatTime(new Time(end * 1000L));

            // Retourner le texte de l'infobulle
            return String.format("<html><b>%s</b><br>Entrée : %s<br>Sortie : %s</html>", label, formattedStart, formattedEnd);
        }
    });
    plot.setRenderer(renderer);

    // Configuration de l'axe des X
    ValueAxis domainAxis = plot.getDomainAxis();
    domainAxis.setLabel("HH:mm:ss");
    //domainAxis.setRange(0, 24 * 3600); // Plage horaire en secondes

    // Appliquer un format personnalisé à l'axe des X
    NumberAxis numberAxis = (NumberAxis) domainAxis;
    numberAxis.setNumberFormatOverride(new NumberFormat() {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
            int totalSeconds = (int) number;
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return toAppendTo.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }

        @Override
        public Number parse(String source, java.text.ParsePosition parsePosition) throws ParseException {
            throw new UnsupportedOperationException("Parsing not implemented");
        }

		@Override
		public StringBuffer format(long arg0, StringBuffer arg1, FieldPosition arg2) {
			// TODO Auto-generated method stub
			return null;
		}
    });

    // Configuration de l'axe des Y (remplacement de NumberAxis par SymbolAxis)
    String[] labels = new String[zoneLabels.size() + 1]; // Ajouter un libellé pour chaque zone
    for (Map.Entry<Integer, String> entry : zoneLabels.entrySet()) {
        labels[entry.getKey()] = entry.getValue();
    }

    labels[0] = "";

    SymbolAxis rangeAxis = new SymbolAxis("Zones", labels);
    rangeAxis.setRange(0, zoneLabels.size());
    rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
    rangeAxis.setInverted(false); // Affiche les zones dans l'ordre croissant
    plot.setRangeAxis(rangeAxis);

    // Ajouter le graphique au panneau
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setMouseWheelEnabled(true);

	 // Ajouter un écouteur de souris au panneau
	 chartPanel.addChartMouseListener(new ChartMouseListener() {
	     @Override
	     public void chartMouseClicked(ChartMouseEvent event) {
	         ChartEntity entity = event.getEntity();
	         if (entity instanceof LegendItemEntity) {
	             LegendItemEntity legendItem = (LegendItemEntity) entity;

	             // Récupérer la clé de la série associée à l'élément de légende
	             Comparable<?> clickedSeriesKey = legendItem.getSeriesKey();

	             XYPlot plot = (XYPlot) chart.getPlot();
	             boolean isRefocusing = clickedSeriesKey.equals(focusedSeriesKey);

	             if (isRefocusing) {
	                 // Si on clique à nouveau sur la même légende, tout réafficher
	                 for (int i = 0; i < plot.getDatasetCount(); i++) {
	                     XYDataset dataset = plot.getDataset(i);
	                     for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
	                         plot.getRenderer(i).setSeriesVisible(seriesIndex, true);
	                     }
	                 }
	                 focusedSeriesKey = null; // Réinitialiser l'état de focus
	             } else {
	                 // Sinon, afficher uniquement la série cliquée
	                 for (int i = 0; i < plot.getDatasetCount(); i++) {
	                     XYDataset dataset = plot.getDataset(i);
	                     for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
	                         boolean isTargetSeries = dataset.getSeriesKey(seriesIndex).equals(clickedSeriesKey);
	                         plot.getRenderer(i).setSeriesVisible(seriesIndex, isTargetSeries);
	                     }
	                 }
	                 focusedSeriesKey = clickedSeriesKey; // Mettre à jour la série actuellement en focus
	             }
	         }
	     }
		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
			// TODO Auto-generated method stub

		}




	 });

    this.setLayout(new BorderLayout());
    this.add(chartPanel, BorderLayout.CENTER);
}

    private String formatTime(Time time) {
    if (time == null) {
        return "N/A"; // Si l'heure est nulle
    }
    int hours = time.getHours();
    int minutes = time.getMinutes();
    int seconds = time.getSeconds();
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
}

    private XYIntervalSeriesCollection getDatasetFromDatabase() {
        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();




        try (PreparedStatement stmt = SQL_DATA.getInstance().getPreparedStatement("""
                SELECT Z.NumZone, idbarre,Z.CodeZone, label, entree, sortie
                FROM Zones Z
                INNER 	 JOIN LOGS_CPO L ON L.NumZone = Z.NumZone AND CAST(date_log AS DATE) = ?
                ORDER BY idbarre,Z.NumZone  desc
                """)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            stmt.setString(1, sdf.format(dDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int numZone = rs.getInt("NumZone");
               // int idbarre = rs.getInt("idbarre");
                String label = rs.getString("label");
                Time entree = rs.getTime("entree");
                Time sortie = rs.getTime("sortie");

                // Convertir heures en secondes depuis minuit
                if (entree != null && sortie != null) {
                    int start = timeToSeconds(entree);
                    int end = timeToSeconds(sortie);

                    // Chercher ou créer une série
                    XYIntervalSeries series = null;  // Cherche la série par nom
                    if (dataset.indexOf(label) == -1) {
                        series = new XYIntervalSeries(label);               // Créer une nouvelle série si elle n'existe pas
                        dataset.addSeries(series);
                    }
                    else {
                        series = dataset.getSeries(dataset.indexOf(label));
                    }

                    // Ajouter la tâche (intervalle) à la série
                    series.add(start, start, end, numZone, numZone - 0.4, numZone + 0.4);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataset;
    }

    private int timeToSeconds(Time time) {
        return ((time.getHours() * 3600) + (time.getMinutes() * 60) + time.getSeconds());
    }


    public static void main(String[] args) {
        // Créer un LocalDate
        LocalDate localDate = LocalDate.of(2024, 12, 1);

        // Convertir en java.util.Date si nécessaire
        Date today = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        CPO_LOGS_GANT panel = new CPO_LOGS_GANT(today);

        // Créer la fenêtre
        JFrame frame = new JFrame("Diagramme de Gantt");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(panel);
        frame.setVisible(true);
    }
}