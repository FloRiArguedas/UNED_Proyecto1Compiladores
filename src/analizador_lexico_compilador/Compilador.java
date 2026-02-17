package analizador_lexico_compilador;

/**
 * UNED - COMPILADORES - PROYECTO #1
 * FLORICELA ARGUEDAS Z
 */
public class Compilador {

    public static void main(String[] args) {

        //**FASE 1 - LECTURA DEL ARCHIVO**
        //Validar que el argumento no esté vacio
        if (args.length == 0) {
            System.out.println("Debe brindar la ruta del archivo");
            return;
        }
        String PathFile = args[0];
        //Se valida el tipo de archivo
        Validador validador = new Validador();
        if (!validador.ValidarTipoArchivo(PathFile)) {
            return; //retorno si es false
        }
        //Lectura del archivo
        AnalizadorLexico analizador = new AnalizadorLexico();
        analizador.leer_archivo(PathFile);
        //**FIN FASE 1**

        //**FASE 2 - CREAR ARCHIVO LOGS**
        //Se crea el archivo logs
        Registrador logs = new Registrador();
        String LogsFilePath = logs.CrearArchivoLogs(PathFile);

        //Se copia el archivo logs
        if (LogsFilePath != null) {

            boolean LogsFile = logs.CrearDuplicadoLogs(PathFile, LogsFilePath);

            if (LogsFile) {
                System.out.println("El arhivo para logs, se copio correctamente.");
            } else {
                System.out.println("No se pudo crear la copia numerada.");
            }
        }
        //**FIN FASE 2**
    }
}
