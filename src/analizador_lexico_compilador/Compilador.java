package analizador_lexico_compilador;

/**
 * UNED - COMPILADORES - PROYECTO #1
 * FLORICELA ARGUEDAS Z
 */
public class Compilador {

    public static void main(String[] args) {

        //LECTURA DEL ARCHIVO**
        //Validar que el argumento no esté vacio
        if (args.length == 0) {
            System.out.println("Debe brindar la ruta del archivo");
            return;
        }
        String PathFile = args[0];
        //Se valida el tipo de archivo
        if (!Validador.ValidarTipoArchivo(PathFile)) {
            return; //retorno si es false
        }
        //Análisis del archivo
        AnalizadorLexico analizador = new AnalizadorLexico();
        analizador.analizar_archivo(PathFile);
        
        System.out.println("Analizador Ejecutado con éxito");
        //**FIN DEL PROCESO**
    }
}
