package analizador_lexico_compilador;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fargu
 */
public class Validador {

    //Constructor (Tomado de la IA) Prompt #X
    private Registrador registrador; //Atributo

    public Validador(Registrador r) {
        this.registrador = r;
    }

    //Banderas
    private boolean BanderaIdent = false;
    private boolean BanderaTD = false;
    private boolean EstaModule = false;
    //Diccionario para guardar variables nombre-tipo
    private final Map<String, String> variablesDeclaradas = new HashMap<>();


    /* VALIDACION #1 TIPO ARCHIVO vb */
    public static boolean ValidarTipoArchivo(String archivo) {
        /* Verifico el final del string */
        if (!archivo.endsWith(".vb")) {
            System.out.println("Error, solo se aceptan archivos tipo .vb");
            return false;
        }
        return true;
    }

    //VALIDACION #2 USO INCORRECTO PALABRAS RESERVADAS (ERRORES 100)
    public boolean ValidarReservadas(String token, TablaSimbolos.tokentype type, int linenum) {

        //Paso 1: Verificar si las banderas están activas
        // ESPERAR UN IDENT, LUEGO DE UN DIM
        if (BanderaIdent) {
            BanderaIdent = false;
            if (type == TablaSimbolos.tokentype.Reservada) {
                String MensajeError = "ERROR 100: Una palabra reservada no puede usarse como identificador";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return true; //Envio error a log
            }
            //Valido que el IDENT sea correcto luego de DIM (ERROR 205 FORMATO VARIABLES)
            if (type != TablaSimbolos.tokentype.Identificador) {
                String MensajeError = "ERROR 205: El identificador posee un formato inválido.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return true; //Envio error a log
            }
        }

        // ESPERAR UN TIPO DATO, LUEGO DE UN AS
        if (BanderaTD) {
            BanderaTD = false;
            if (type == TablaSimbolos.tokentype.Reservada) {
                String MensajeError = "ERROR 101: Se espera un tipo de dato. No puede usar palabras reservadas.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return true;
            }
        }

        // Paso 2: Activar banderas  - (Tomado de la IA) Prompt #X
        if (type == TablaSimbolos.tokentype.Reservada && token.equalsIgnoreCase("dim")) {
            BanderaIdent = true;
        }

        if (type == TablaSimbolos.tokentype.Reservada && token.equalsIgnoreCase("as")) {
            BanderaTD = true;
        }
        return false;
    }

    //VALIDACION #3 FORMATO DECLARACION DE VARIABLES (ERRORES 200)
    public void ValidarDeclaracionDim(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //Valido si la linea empieza con module
        if (linea.get(0).equalsIgnoreCase("module")) {
            EstaModule = true;
            return;
        }

        //Verifico si la linea comienza con la palabra dim
        if (linea.get(0).equalsIgnoreCase("dim")) {
            //Verifico que dim aparezca luego de module
            if (!EstaModule) {
                String MensajeError = "ERROR 204: Module debe aparecer antes de Dim";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
            //Llamo a la Funcion Validar para ver que tipo de expresion es
            TablaExpresiones.expresiones TipoExpresion = TablaExpresiones.validar(tokentypes);
            //Si la linea no coincide con una expresion, envio error
            if (TipoExpresion == null) {
                String MensajeError = "ERROR 200: La declaracion de variable no coincide con el formato adecuado";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
            }

            //VALIDACIONES ADICIONALES PARA FORMATO 3
            //VERIFICAR SI HAY VARIABLES DECLARADAS Y SUS TIPOS
            //Guardo la variable y su tipo en el diccionario (dim x as y)
            String NombreVariable = linea.get(1); //X
            String tipoDato = linea.get(3); //Y
            //las ingreso al diccionario
            variablesDeclaradas.put(
                    NombreVariable.toLowerCase(),
                    tipoDato.toLowerCase()
            );

            //VERIFICAR OPERANDOS NUMERICOS
            if (TipoExpresion == TablaExpresiones.expresiones.DIM_F3_II
                    || TipoExpresion == TablaExpresiones.expresiones.DIM_F3_IN
                    || TipoExpresion == TablaExpresiones.expresiones.DIM_F3_NI
                    || TipoExpresion == TablaExpresiones.expresiones.DIM_F3_NN) {

                //Obtengo el indice donde esta el =
                int indiceAsignacion = obtenerIndiceAsignacion(tokentypes);
                //Si existe el igual entonces valido si los operandos son correctos
                if (indiceAsignacion != -1) {

                    // Verificacion Operando1 (dim x as y = O1 + O2) posicion 5
                    ValidarOperando(linea, tokentypes, linenum, indiceAsignacion + 1);

                    // Verificacion Operando2 (dim x as y = O1 + O2) posicion 7
                    ValidarOperando(linea, tokentypes, linenum, indiceAsignacion + 3);
                }
            }

            //Si la linea coincide con una expresion lo imprimo. ****TEMPORAL****
            System.out.println("Linea " + linenum + ": Dim válido, la expresión es de tipo: = " + TipoExpresion);
        }
    }

    // VALIDACION #3 - FUNCIONES PARA VERIFICAR LOS OPERANDOS DEL FORMATO DE VARIABLES F3
    //Funcion para encontrar la posición de la asignacion "="
    private int obtenerIndiceAsignacion(List<TablaSimbolos.tokentype> tokentypes) {

        for (int i = 0; i < tokentypes.size(); i++) {

            if (tokentypes.get(i) == TablaSimbolos.tokentype.Asignacion) {
                return i; // Retorno la posición del = 
            }
        }
        return -1; // si no existe retorno -1
    }

    // Validación de los operandos - (Tomada de la IA y modificada) Prompt #X
    private void ValidarOperando(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum, int indiceOperando) {

        //Verifico que existan los dos operandos después del =
        if (indiceOperando >= tokentypes.size()) {
            String MensajeError = "ERROR 206: Formato erroneo. Operando faltante después del '='.";
            System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            return;
        }

        //Extraigo el tipo del operando que voy a evaluar
        TablaSimbolos.tokentype TipoOperando = tokentypes.get(indiceOperando);

        // Si el operando es numerico - OK
        if (TipoOperando == TablaSimbolos.tokentype.Numero) {
            return;
        }

        //Si el operando es un identificador verificarla
        if (TipoOperando == TablaSimbolos.tokentype.Identificador) {

            String nombreVar = linea.get(indiceOperando).toLowerCase();

            // Primero Verificar si la variable está declarada
            if (!variablesDeclaradas.containsKey(nombreVar)) {
                String MensajeError = "ERROR 201: La variable no está declarada.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
            }

            // Segundo Verificar el tipo de la variable (int o byte)
            String tipoDato = variablesDeclaradas.get(nombreVar);
            if (!(tipoDato.equals("integer") || tipoDato.equals("byte"))) {
                String MensajeError = "ERROR 202: La variable debe ser de tipo numérica.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
            return;
        }

        // Si el operando no es numérico o un identificador, es inválido
        String MensajeError = "ERROR 203: Operando inválido";
        System.out.println("Linea " + linenum + MensajeError);
        try {
            registrador.EscribirError(linenum, MensajeError);
        } catch (IOException ex) {
            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    //FUNCION ELIMINADA, SE OPTIMIZO CON LA IA.
    /* private void validarOperando1(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum) {

        TablaSimbolos.tokentype tipoOp1 = tokentypes.get(5);

        // Si es NUM - OK
        if (tipoOp1 == TablaSimbolos.tokentype.Numero) {
            return;
        }

        //Si es IDENT es variable
        if (tipoOp1 == TablaSimbolos.tokentype.Identificador) {

            String nombreVar = linea.get(5).toLowerCase();

            // Verificar si la variable está declarada
            if (!variablesDeclaradas.containsKey(nombreVar)) {
                String MensajeError = "ERROR 201: La variable no está declarada.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
            }

            // Verificar el tipo de la variable (int o byte)
            String tipoDato = variablesDeclaradas.get(nombreVar);
            if (!(tipoDato.equals("integer") || tipoDato.equals("byte"))) {
                String MensajeError = "ERROR 202: La variable debe ser de tipo numérica.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }

            return;
        }

        // Si el operando no es numérico o un identificador, es inválido
        String MensajeError = "ERROR 203: Operando inválido";
        System.out.println("Linea " + linenum + MensajeError);
        try {
            registrador.EscribirError(linenum, MensajeError);
        } catch (IOException ex) {
            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    } */
}
