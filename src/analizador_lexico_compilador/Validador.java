package analizador_lexico_compilador;

/**
 *
 * @author fargu
 */
public class Validador {

    private boolean BanderaReservada = false;

    /* VALIDACION #1 TIPO ARCHIVO vb */
    public boolean ValidarTipoArchivo(String archivo) {
        /* Verifico el final del string */
        if (!archivo.endsWith(".vb")) {
            System.out.println("Error, solo se aceptan archivos tipo .vb");
            return false;
        }
        return true;
    }

    //VALIDACION #2 USO INCORRECTO PALABRAS RESERVADAS
    public boolean ValidarReservadas(String token, TablaSimbolos.tokentype type, int linenum) {

        // ESPERAR UN IDENT, LUEGO DE UN DIM
        //Paso 1: Verifico si la bandera está activa.
        if (BanderaReservada) {
            if (type == TablaSimbolos.tokentype.Reservada) {
                System.out.println("Linea " + linenum + ": ERROR 100. " + "La Palabra " + token + " no puede usarse como identificador");
            }
            BanderaReservada = false;
            return true; //Envio error a log
        }

        // Paso 2: Activar bandera si token es dim
        if (type == TablaSimbolos.tokentype.Reservada && token.equalsIgnoreCase("dim")) {
            BanderaReservada = true;
        }
        return false;
    }

}
