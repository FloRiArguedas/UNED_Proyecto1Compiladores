package analizador_lexico_compilador;

/**
 *
 * @author fargu
 */
public class Validador {

    /* VALIDACION #1 TIPO ARCHIVO vb */
    public boolean ValidarTipoArchivo(String archivo) {
        /* Verifico el final del string */
        if (!archivo.endsWith(".vb")) {
            System.out.println("Error, solo se aceptan archivos tipo .vb");
            return false;
        }
        return true;
    }
}
