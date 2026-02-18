package analizador_lexico_compilador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fargu
 */
public class Validador {

    private boolean BanderaIdent = false;
    private boolean BanderaTD = false;
    //Diccionario para guardar variables nombre-tipo
    private final Map<String, String> variablesDeclaradas = new HashMap<>();

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

        //Paso 1: Verificar si las banderas están activas
        // ESPERAR UN IDENT, LUEGO DE UN DIM
        if (BanderaIdent) {
            BanderaIdent = false;
            if (type == TablaSimbolos.tokentype.Reservada) {
                System.out.println("Linea " + linenum + ": ERROR 100. " + "La Palabra " + token + " no puede usarse como identificador");
                return true; //Envio error a log
            }
        }

        // ESPERAR UN TIPO DATO, LUEGO DE UN AS
        if (BanderaTD) {
            BanderaTD = false;
            if (type == TablaSimbolos.tokentype.Reservada) {
                System.out.println("Linea " + linenum + ": ERROR 101. " + "La Palabra " + token + " no puede usarse como tipo de dato");
                return true; //Envio error a log
            }
        }

        // Paso 2: Activar banderas
        if (type == TablaSimbolos.tokentype.Reservada && token.equalsIgnoreCase("dim")) {
            BanderaIdent = true;
        }

        if (type == TablaSimbolos.tokentype.Reservada && token.equalsIgnoreCase("as")) {
            BanderaTD = true;
        }
        return false;
    }

    //VALIDACION #3 FORMATO DECLARACION DE VARIABLES
    public void ValidarDeclaracionDim(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //Verifico si la linea comienza con la palabra dim
        if (linea.get(0).equalsIgnoreCase("dim")) {
            //Llamo a la Funcion Validar para ver que tipo de expresion es
            TablaExpresiones.expresiones TipoExpresion = TablaExpresiones.validar(tokentypes);
            //Si la linea no coincide con una expresion, envio error
            if (TipoExpresion == null) {
                System.out.println("Linea " + linenum + ": ERROR 200. La declaracion de variable no coincide con el formato adecuado");
                return;
            }

            //VALIDACIONES ADICIONALES PARA FORMATO 3
            
            //VERIFICAR SI HAY VARIABLES DECLARADAS Y SUS TIPOS
            
            //Guardo la variable (dim x as y)
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

                // Verificacion Operando1 (dim x as y = O1 + O2)
                validarOperando1(linea, tokentypes, linenum);

                // Verificacion Operando2 (dim x as y = O1 + O2)
                validarOperando2(linea, tokentypes, linenum);
            }

            //Si la linea coincide con una expresion lo imprimo. ****TEMPORAL****
            System.out.println("Linea " + linenum + ": Dim válido, la expresión es de tipo: = " + TipoExpresion);
        }
    }

    //FUNCIONES PARA VERIFICAR LOS OPERANDOS DEL FORMATO DE VARIABLES F3
    private void validarOperando1(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum) {

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
                System.out.println("Linea " + linenum + ": ERROR 201. Variable '" + linea.get(5) + "' no declarada.");
                return;
            }

            // Verificar el tipo de la variable (int o byte)
            String tipoDato = variablesDeclaradas.get(nombreVar);
            if (!(tipoDato.equals("integer") || tipoDato.equals("byte"))) {
                System.out.println("Linea " + linenum + ": ERROR 202. Variable '" + linea.get(5) + "' no es numérica, es: " + tipoDato);
            }

            return;
        }

        // Si el operando no es numérico o un identificador, es inválido
        System.out.println("Linea " + linenum + ": ERROR 203. Operando inválido: '" + linea.get(5) + "'.");
    }
    
    private void validarOperando2(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum) {

        TablaSimbolos.tokentype tipoOp2 = tokentypes.get(7);

        // Si es NUM - OK
        if (tipoOp2 == TablaSimbolos.tokentype.Numero) {
            return;
        }

        //Si es IDENT es variable
        if (tipoOp2 == TablaSimbolos.tokentype.Identificador) {

            String nombreVar = linea.get(7).toLowerCase();

            // Verificar si la variable está declarada
            if (!variablesDeclaradas.containsKey(nombreVar)) {
                System.out.println("Linea " + linenum + ": ERROR 201. Variable '" + linea.get(7) + "' no declarada.");
                return;
            }

            // Verificar el tipo de la variable (int o byte)
            String tipoDato = variablesDeclaradas.get(nombreVar);
            if (!(tipoDato.equals("integer") || tipoDato.equals("byte"))) {
                System.out.println("Linea " + linenum + ": ERROR 202. Variable '" + linea.get(7) + "' no es numérica, es: " + tipoDato);
            }

            return;
        }

        // Si el operando no es numérico o un identificador, es inválido
        System.out.println("Linea " + linenum + ": ERROR 203. Operando inválido: '" + linea.get(7) + "'.");
    }

}
