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
    private boolean EstaImports = false;
    //Diccionario para guardar variables nombre-tipo
    private final Map<String, String> variablesDeclaradas = new HashMap<>();

    //VALIDACION #1 TIPO ARCHIVO vb
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
                return true;
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
                return true;
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

        //Valido si existe Module
        int indiceModule = -1;
        for (int i = 0; i < linea.size(); i++) {
            // Normalizo el token por si trae caracteres adicionales
            String token = linea.get(i).replaceAll("[^A-Za-z]", "").toLowerCase();

            if (token.equals("module")) {
                indiceModule = i;
                break;
            }
        } //Si encuentro el token module activo la bandera
        if (indiceModule != -1) {
            EstaModule = true;
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
            //Verifico que la linea cumpla al menos con el formato mínimo: Dim nombreVariable As TipoDeDato
            if (linea.size() < 4) {
                String MensajeError = "ERROR 200: La declaracion de variable no coincide con el formato adecuado";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
            }
            //Si la linea si cumple el formato mínimo, continuo con la evaluación
            
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
            //Guardo la variable y su tipo en el diccionario (Dim X As Tipo)
            String NombreVariable = linea.get(1); //X
            String tipoDato = linea.get(3); //Tipo
            //las ingreso al diccionario
            variablesDeclaradas.put(
                    NombreVariable.toLowerCase(),
                    tipoDato.toLowerCase()
            );

            //SI LA EXPRESION ES FORMATO3 - VERIFICO OPERANDOS NUMERICOS
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
}

    //VALIDACION #3.1 FUNCIONES PARA VERIFICAR LOS OPERANDOS DEL FORMATO DE VARIABLES F3 
    //Funcion para encontrar la posición de la asignacion "="
    private int obtenerIndiceAsignacion(List<TablaSimbolos.tokentype> tokentypes) {

        for (int i = 0; i < tokentypes.size(); i++) {

            if (tokentypes.get(i) == TablaSimbolos.tokentype.Asignacion) {
                return i; // Retorno la posición del = 
            }
        }
        return -1; // si no existe retorno -1
    }

    //Validación de los operandos - (Tomada de la IA y modificada) Prompt #X
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

    //VALIDACION #4 SENTENCIAS DE IMPRESION CON CONSOLE.WRITELINE (ERRORES 300)
    public void ValidarSentenciasCWL(List<String> linea, int linenum) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }
        int indiceCWL = -1; //Bandera

        //Verifico cada token de la linea a ver si alguno coincide con CWL
        for (int i = 0; i < linea.size(); i++) {
            String token = linea.get(i).toLowerCase();
            if (token.contains("console.writeline")) {
                indiceCWL = i;
                break;
            }
        }
        //Si no encontre CWL en la linea retorno.
        if (indiceCWL == -1) {
            return;
        }

        //Si encontré CWL hago las validaciones necesarias
        if (indiceCWL != -1) {

            //VALIDACION #1: Parentesis de apertura y cierre ()
            ValidarParentesisCWL(linea, linenum, indiceCWL);

            //--Convierto la linea en un string--
            //Constructor de Strings
            StringBuilder SB = new StringBuilder();
            //Recorro a partir del CWL y agrego cada token
            for (int i = indiceCWL; i < linea.size(); i++) {
                SB.append(linea.get(i)).append(" "); //Agrego los TK al SB
            }
            //Convierto el SB en un String normal
            String lineaString = SB.toString().trim();

            //VALIDACION #2: Texto entre comillas("")
            //VALIDACION #3: NO PUEDEN ESTAR VACIOS LOS "( )"
            ValidarContenidoParentesis(lineaString, linenum);
        }

    }

    //FUNCIONES APARTE PARA LAS VALIDACIONES #4 DE ()
    //VALIDACION #4.1: Parentesis de apertura y cierre ()
    private void ValidarParentesisCWL(List<String> linea, int linenum, int indiceCWL) {

        //Banderas indices de ()
        int indiceApertura = -1;
        int indiceCierre = -1;

        //Recorro a partir del CWL para buscar ()
        for (int i = indiceCWL; i < linea.size(); i++) {
            String token = linea.get(i);

            //Si encuentro un token con ( guardo el indice
            if (indiceApertura == -1 && token.contains("(")) {
                indiceApertura = i;
            }
            //Si encuentro un token con ) guardo el indice
            if (indiceCierre == -1 && token.contains(")")) {
                indiceCierre = i;
            }

            // Si encuentro () salgo del ciclo.
            if (indiceApertura != -1 && indiceCierre != -1) {
                break;
            }
        }

        //REGISTRO ERRORES DE ()
        if (indiceApertura == -1) {
            String MensajeError = "ERROR 300: Falta paréntesis de apertura '(' en Console.WriteLine.";
            System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }

        if (indiceCierre == -1) {
            String MensajeError = "ERROR 300: Falta paréntesis de cierre ')' en Console.WriteLine.";
            System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        //Si los paréntesis no están en el orden correcto, marco error
        if (indiceApertura != -1 && indiceCierre != -1 && indiceCierre < indiceApertura) {
            String MensajeError = "ERROR 300: El paréntesis de cierre ')' aparece antes del paréntesis de apertura '(' en Console.WriteLine.";
            System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }

    //VALIDACION #4.2: Texto entre comillas("")
    private void ValidarContenidoParentesis(String lineaString, int linenum) {

        //REGISTRO ERRORES DE ()
        //Busco la posición del 1er "("
        int ParentesisAbrir = lineaString.indexOf("(");

        //Busco el 1er ")" luego del "("
        int ParentesisCerrar = lineaString.indexOf(")", ParentesisAbrir + 1);

        //Verifico que se hayan encontrado "( )"
        if (ParentesisAbrir == -1 || ParentesisCerrar == -1 || ParentesisCerrar < ParentesisAbrir) {
            return; //Si no hay parentesis o están mal posicionados, retorno.
        }

        //Si los "( )" están correctos, extraigo el contenido dentro y elimino espacios.
        String ContenidoParentesis = lineaString.substring(ParentesisAbrir + 1, ParentesisCerrar).trim();

        //VALIDACION#4.3 NO PUEDEN ESTAR VACIOS LOS "( )"
        if (ContenidoParentesis.isEmpty()) {
            String MensajeError = "ERROR 302: El contenido dentro de paréntesis no puede estar vacío.";
            System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            return;
        }

        //Verifico si el contenido inicia con comillas "
        if (ContenidoParentesis.charAt(0) == '"') {

            //Busco las otras comillas " a partir de la posición 1
            int ComillaCierre = ContenidoParentesis.indexOf('"', 1);

            //Si no encontré ComillaCierre, muestro error.
            if (ComillaCierre == -1) {
                String MensajeError = "ERROR 301: El texto a imprimir debe ir encerrado entre comillas dobles.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }

    }

    /*
    //VALIDACION #5 ESTRUCTURA DE MODULE (ERRORES 400)
    public void ValidarEstructuraModule(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //Valido si la linea empieza con IMPORTS
        if (linea.get(0).equalsIgnoreCase("imports")) {
            EstaImports = true;
        }

        //Verifico si la linea comienza con la palabra module
        if (linea.get(0).equalsIgnoreCase("module")) {
            //Verifico que imports aparezca antes de module
            if (!EstaImports) {
                String MensajeError = "ERROR 400: Module debe aparecer después de Imports";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
             //Llamo a la Funcion Validar para verificar que sea un identificador
            TablaSimbolos.tokentype TokenType = TablaSimbolos.clasificar (tokentypes);
            //Si la linea no coincide con una expresion, envio error
            if (TokenType != "identificador") {
                String MensajeError = "ERROR 401: Después de module debe haber un identificador válido.";
                System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
        }

    }*/
}
