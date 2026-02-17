package analizador_lexico_compilador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author fargu
 */
public class Registrador {

    /*FUNCION PARA CREAR UN ARCHIVO PARA LOS LOGS*/
    public String CrearArchivoLogs(String OriginalPath) {

        try {
            /*Extraigo la información del archivo original */
            File originalFile = new File(OriginalPath);
            String FileName = originalFile.getName();
            String ShortName = FileName.substring(0, FileName.length() - 3);
            String FolderFile = originalFile.getParent();

            /*Creo el duplicado*/
            String FileLogsName = ShortName + "-errores.log";
            String FileLogsPath;
            if (FolderFile != null) {
                FileLogsPath = FolderFile + File.separator + FileLogsName;
            } else {
                FileLogsPath = FileLogsName;
                /*En la carpeta actual del .jar*/
            }

            File LogsFile = new File(FileLogsPath);

            if (LogsFile.createNewFile()) {
                System.out.println("Se creo correctamente el archivo de logs");
            } else {
                System.out.println("El archivo de logs, ya existe.");
            }

            return FileLogsPath;
        } catch (IOException e) {
            System.out.println("Error creando el archivo de logs: " + e.getMessage());
            return null;
        }
    }

    /*FUNCION PARA HACER COPIA DEL ARCHIVO ORIGINAL, EN EL DE LOGS*/
    public boolean CrearDuplicadoLogs(String OriginalPath, String PathLogs) {

        try (BufferedReader Reader = new BufferedReader(new FileReader(OriginalPath));
            BufferedWriter Writer = new BufferedWriter(new FileWriter(PathLogs, false))){
            
            /*Leer el archivo original y escribir en el archivo copia*/
        
            String line;
            int numLine = 1;

            while ((line = Reader.readLine()) != null) {

                String Prefix = String.format("%04d", numLine);

                Writer.write(Prefix + " " + line);
                Writer.newLine();
                numLine++;
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error inicializando el archivo log: " + e.getMessage());
            return false;
        }
    }

}



