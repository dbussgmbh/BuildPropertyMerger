import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class genBuildProperty {
    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Bitte geben Sie die Pfade für die Neue BuildProperty Datei, die Vorgänger Build Property und die zu erstellende Build-Property Datei als Kommandozeilenparameter an.");
            System.err.println("Beispiel: java BuildPropertyMerger NewBuildProperty OldBuildProperty GenerateBuildProperty");
            System.exit(1);
        }

        String originalDateiPfad = args[0];
        String altDateiPfad = args[1];
        String neueDateiPfad = args[2];


       // String originalDateiPfad = "/Users/micha/Library/CloudStorage/OneDrive-Persönlich/DBUSS_Cloud/bp_new.txt";
       // String altDateiPfad = "/Users/micha/Library/CloudStorage/OneDrive-Persönlich/DBUSS_Cloud/bp_old.txt";;
       // String neueDateiPfad = "/Users/micha/Library/CloudStorage/OneDrive-Persönlich/DBUSS_Cloud/build.properties_merge.txt";

        try {
            Map<String, String> altDaten = ladeAltDatei(altDateiPfad);
            verarbeiteOriginalDatei(originalDateiPfad, altDaten, neueDateiPfad);
            System.out.println("----------------------------------------------");
            System.out.println("Datei wurde erstellt: " + neueDateiPfad);
        } catch (IOException e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
        }
    }

    private static Map<String, String> ladeAltDatei(String altDateiPfad) throws IOException {

        Map<String, String> altDaten = new HashMap<>();
        Path altPath = Paths.get(altDateiPfad);

        if (Files.exists(altPath)) {
            try (BufferedReader reader = Files.newBufferedReader(altPath, java.nio.charset.StandardCharsets.ISO_8859_1)) {
                String zeile;
                while ((zeile = reader.readLine()) != null) {
                    if (!zeile.startsWith("#") && zeile.contains("=")) {
                        String[] teile = zeile.split("=", 2);
                        if (teile.length == 2) {
                            altDaten.put(teile[0].trim(), zeile.trim());
                        }
                    }
                }
            }
        }

        return altDaten;
    }

    private static void verarbeiteOriginalDatei(String originalDateiPfad, Map<String, String> altDaten, String neueDateiPfad) throws IOException {
        Path originalPath = Paths.get(originalDateiPfad);
        Path neuePath = Paths.get(neueDateiPfad);


        try (BufferedReader reader = Files.newBufferedReader(originalPath, java.nio.charset.StandardCharsets.ISO_8859_1);
             BufferedWriter writer = Files.newBufferedWriter(neuePath, java.nio.charset.StandardCharsets.ISO_8859_1)) {

            int lineNumber = 1;

            String zeile;
            Scanner scanner = new Scanner(System.in);

            while ((zeile = reader.readLine()) != null) {
                if (zeile.startsWith("#")) {
                    writer.write(zeile);
                    writer.newLine();
                } else if (zeile.contains("=")) {
                    String[] teile = zeile.split("=", 2);
                    if (teile.length == 2) {
                        String variable = teile[0].trim();
                        String neuerWert = teile[1].trim();

                        if (altDaten.containsKey(variable)) {

                            String altZeile = altDaten.get(variable);
                            String alterWert = altZeile.split("=", 2)[1].trim();

                            if (!neuerWert.equals(alterWert)) {
                                System.out.println("############# Zeile: " + lineNumber + "  ###############");
                                System.out.println("Variable: " + variable);
                                System.out.println("Zeile in der Alt-Datei: " + altZeile);
                                System.out.println("Zeile in der Original-Datei: " + zeile);

                                System.out.print("Welchen Zeile soll übernommen werden? (a)lte / (n)eue Version? ");
                                String auswahl = scanner.nextLine().trim().toLowerCase();

                                if (auswahl.equals("a")) {
                                    //writer.write(variable + "=" + alterWert);
                                    writer.write(altZeile);
                                } else {
                                    //writer.write(variable + "=" + neuerWert);
                                    writer.write(zeile);
                                }
                                writer.newLine();
                            } else {
                                writer.write(zeile);
                                writer.newLine();
                            }

                            //Aus alteDaten die gefundene Variable entfernen:
                            altDaten.remove(variable);

                        } else {
                            System.out.println("############# Zeile: " + lineNumber + "  ###############");
                            System.out.println("Info: Bisher nicht vorhandene Variable hinzugefügt: " + zeile);
                            writer.write(zeile);
                            writer.newLine();
                        }
                    }
                } else {
                    writer.write(zeile);
                    writer.newLine();
                }
                lineNumber++;
            }

            if (altDaten.size() > 0)   //Wenn in der Liste mit bisherigen Properties Einträge verblieben sind, dann diese ausgeben:
            {
                System.out.println();
                System.out.println("Achtung: Folgende bisher definierte Properties sind in der neuen Version nicht mehr vorhanden:");
                for (Map.Entry<String, String> entry : altDaten.entrySet()) {
                    System.out.println("-> " + entry.getValue());

                }
            }


        }



    }
}