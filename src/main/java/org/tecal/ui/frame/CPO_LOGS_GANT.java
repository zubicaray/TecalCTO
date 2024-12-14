package org.tecal.ui.frame;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
        Map<Integer, String> zoneLabels = loadZoneLabels();

        // Créer le dataset
        IntervalXYDataset dataset = getDatasetFromDatabase();

        // Créer le graphique
        JFreeChart chart = ChartFactory.createXYBarChart(
                "Diagramme de Gantt - LOGS_CPO", 
                "Heures", 
                false, 
                "NumZone", 
                dataset, 
                org.jfree.chart.plot.PlotOrientation.VERTICAL,  
                true, true, false);

        // Personnaliser le plot
        XYPlot plot = (XYPlot) chart.getPlot();
        XYBarRenderer renderer = new XYBarRenderer();

        // Configuration du renderer
        renderer.setUseYInterval(true);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardXYBarPainter());

        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public String generateToolTip(XYDataset dataset, int series, int item) {
                XYIntervalSeriesCollection collection = (XYIntervalSeriesCollection) dataset;
                XYIntervalSeries xySeries = collection.getSeries(series);
                String label = xySeries.getKey().toString();
                int start = (int) xySeries.getXLowValue(item);
                int end = (int) xySeries.getXHighValue(item);
                String formattedStart = formatTime(new Time(start * 1000L).toLocalTime());
                String formattedEnd = formatTime(new Time(end * 1000L).toLocalTime());
                return String.format("<html><b>%s</b><br>Entrée : %s<br>Sortie : %s</html>", label, formattedStart, formattedEnd);
            }
        });
        plot.setRenderer(renderer);

        // Configuration de l'axe des X
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabel("Temps (jours et heures)");

        // Étendre la plage pour couvrir plusieurs jours (par exemple, 3 jours)
        //int numberOfDays = 1; // Modifier cette valeur selon vos besoins
        //domainAxis.setRange(0, numberOfDays * 24 * 3600); 

        NumberAxis numberAxis = (NumberAxis) domainAxis;
        numberAxis.setNumberFormatOverride(new NumberFormat() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                int totalSeconds = (int) number;
                int days = totalSeconds / (24 * 3600);
                int hours = (totalSeconds % (24 * 3600)) / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;

                if (days > 0) {
                    return toAppendTo.append(String.format("Jour %d, %02d:%02d:%02d", days + 1, hours, minutes, seconds));
                } else {
                    return toAppendTo.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            }

            @Override
            public Number parse(String source, java.text.ParsePosition parsePosition) {
                throw new UnsupportedOperationException("Parsing not implemented");
            }

            @Override
            public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                return format((double) number, toAppendTo, pos);
            }
        });

        // Configuration de l'axe des Y
        String[] labels = new String[zoneLabels.size() + 1]; 
        for (Map.Entry<Integer, String> entry : zoneLabels.entrySet()) {
            labels[entry.getKey()] = entry.getValue();
        }
        labels[0] = "";
        SymbolAxis rangeAxis = new SymbolAxis("Zones", labels);
        rangeAxis.setRange(0, zoneLabels.size());
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.setRangeAxis(rangeAxis);

        // Ajouter le graphique au panneau
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof LegendItemEntity) {
                    LegendItemEntity legendItem = (LegendItemEntity) entity;
                    Comparable<?> clickedSeriesKey = legendItem.getSeriesKey();
                    XYPlot plot = (XYPlot) chart.getPlot();
                    boolean isRefocusing = clickedSeriesKey.equals(focusedSeriesKey);

                    if (isRefocusing) {
                        for (int i = 0; i < plot.getDatasetCount(); i++) {
                            XYDataset dataset = plot.getDataset(i);
                            for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
                                plot.getRenderer(i).setSeriesVisible(seriesIndex, true);
                            }
                        }
                        focusedSeriesKey = null;
                    } else {
                        for (int i = 0; i < plot.getDatasetCount(); i++) {
                            XYDataset dataset = plot.getDataset(i);
                            for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {
                                boolean isTargetSeries = dataset.getSeriesKey(seriesIndex).equals(clickedSeriesKey);
                                plot.getRenderer(i).setSeriesVisible(seriesIndex, isTargetSeries);
                            }
                        }
                        focusedSeriesKey = clickedSeriesKey;
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {}
        });

        this.setLayout(new BorderLayout());
        this.add(chartPanel, BorderLayout.CENTER);
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return "N/A";
        }
        int hours = time.getHour();
        int minutes = time.getMinute();
        int seconds = time.getSecond();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private XYIntervalSeriesCollection getDatasetFromDatabase() {
        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();

        try (PreparedStatement stmt = SQL_DATA.getInstance().getPreparedStatement("""
                SELECT         		     
				    Z.NumZone,idbarre,Z.CodeZone,label,     
				     entree,sortie
				FROM ANODISATION.dbo.Zones Z
				INNER JOIN ANODISATION.dbo.LOGS_CPO L 
				    ON L.NumZone = Z.NumZone 
				    AND CAST(date_log AS DATE) = ?
				ORDER BY idbarre, label, entree, Z.NumZone
                """)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            stmt.setString(1, sdf.format(dDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int numZone = rs.getInt("NumZone");
                String label = rs.getString("label");
                LocalDateTime entree = rs.getTimestamp("entree").toLocalDateTime();
                LocalDateTime sortie = rs.getTimestamp("sortie").toLocalDateTime();
                
                LocalDateTime now = LocalDateTime.now();
                ZoneId zone = ZoneId.of("Europe/Paris");
                ZoneOffset zoneOffSet = zone.getRules().getOffset(now);

                if (entree != null && sortie != null) {
                    long start = entree.toEpochSecond(zoneOffSet);
                    long end = sortie.toEpochSecond(zoneOffSet);

                    XYIntervalSeries series;
                    if (dataset.indexOf(label) == -1) {
                        series = new XYIntervalSeries(label);
                        dataset.addSeries(series);
                    } else {
                        series = dataset.getSeries(dataset.indexOf(label));
                    }

                    series.add(start, start, end, numZone, numZone - 0.4, numZone + 0.4);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataset;
    }



    public static void main(String[] args) {
        LocalDate localDate = LocalDate.of(2024, 12, 11);
        Date today = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        CPO_LOGS_GANT panel = new CPO_LOGS_GANT(today);

        JFrame frame = new JFrame("Diagramme de Gantt");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(panel);
        frame.setVisible(true);
    }
}
