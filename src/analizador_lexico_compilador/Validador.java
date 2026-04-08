package analizador_lexico_compilador;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author fargu
 */
public class Validador {

    //Constructor (Consultado a la IA) Prompt #3
    private Registrador registrador; //Atributo

    public Validador(Registrador r) {
        this.registrador = r;
    }

    //Banderas
    public boolean BanderaIdent = false;
    public boolean BanderaTD = false;
    private boolean EstaModule = false;
    private boolean EstaImports = false;
    private boolean EstaEndModule = false;
    private boolean Error500Detectado = false;
    Stack<Integer> PilaWhile = new Stack<>();
    Stack<Integer> PilaFor = new Stack<>();
    Stack<Boolean> PilaEstructuraFor = new Stack<>();
    Stack<Integer> PilaIf = new Stack<>();
    Stack<Boolean> PilaEstructuraIf = new Stack<>();
    private boolean ContenidoWhileValido = false;
    private boolean ContenidoForValido = false;
    private boolean SentenciaThen = false;
    private boolean SentenciaElse = false;
    private boolean EstaElse = false;
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
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return true;
            }
            //Valido que el IDENT sea correcto luego de DIM (ERROR 205 FORMATO VARIABLES)
            if (type != TablaSimbolos.tokentype.Identificador) {
                String MensajeError = "ERROR 205: El identificador posee un formato invalido.";
                //System.out.println("Linea " + linenum + MensajeError);
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
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return true;
            }
        }

        // Paso 2: Activar banderas  - (Consultado en la IA) Prompt #1
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

        //Verifico si la linea comienza con la palabra dim
        if (linea.get(0).equalsIgnoreCase("dim")) {
            //Verifico que dim aparezca luego de module
            if (!EstaModule) {
                String MensajeError = "ERROR 204: Module debe aparecer antes de Dim";
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
            //Verifico que la linea cumpla al menos con el formato mínimo: Dim nombreVariable As TipoDeDato
            if (linea.size() < 4) {
                String MensajeError = "ERROR 200: La declaracion de variable no coincide con el formato adecuado";
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
            }
            //Si la linea si cumple el formato(tamaño) mínimo, continuo con la evaluación

            //Llamo a la Funcion Validar para ver que tipo de expresion es
            TablaExpresiones.expresiones TipoExpresion = TablaExpresiones.validar(tokentypes);
            //System.out.println("LINEA: " + linea);
            //System.out.println("TOKENS: " + tokentypes);
            //System.out.println("EXPRESION DETECTADA: " + TipoExpresion);

            //Si la linea no coincide con una expresion, envio error
            if (TipoExpresion == null) {

                //Solo para Dim x as tipo_dato = Valor. (Valor = cadena).
                //Verifico si VALOR, es una cadena entre "".
                int indiceIgual = obtenerIndiceAsignacion(tokentypes);
                if (indiceIgual != -1) {
                    StringBuilder valor = new StringBuilder(); //Creo SB para ingresar el string del valor.
                    for (int i = indiceIgual + 1; i < linea.size(); i++) {
                        valor.append(linea.get(i)).append(" ");
                    }
                    String ValorString = valor.toString().trim(); //Paso el SB a String normal, sin espacios.

                    //Verifico si ese valor es una "cadena".
                    if (ValorString.matches("\"[^\"]*\"")) { //Cualquier conjunto de caracteres entre comillas. Consulta a la IA Promtp#9 
                        return; // Si era una cadena, retorno, porque es formato 2 para String válido.
                    }
                }

                //Si no cumplió con estructura. ERROR.
                String MensajeError = "ERROR 200: La declaracion de variable no coincide con el formato adecuado";
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return;
            }

            //VALIDACIONES ADICIONALES PARA FORMATO 3
            //Verifico que la declaración de la variable sea correcta
            if (tokentypes.get(1) == TablaSimbolos.tokentype.Identificador && tokentypes.get(3) == TablaSimbolos.tokentype.Tipo_dato) {
                //Diccionario Consultado en la IA - Promtp #2
                //Guardo la variable y su tipo en el diccionario (Dim X As Tipo)
                String NombreVariable = linea.get(1); //X
                String tipoDato = linea.get(3); //Tipo
                //las ingreso al diccionario
                variablesDeclaradas.put(
                        NombreVariable.toLowerCase(),
                        tipoDato.toLowerCase()
                );
            }

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
        }
    }

    //--VALIDACION #3.1 FUNCIONES PARA VERIFICAR LOS OPERANDOS DEL FORMATO DE VARIABLES F3 
    //Funcion para encontrar la posición de la asignacion "="
    private int obtenerIndiceAsignacion(List<TablaSimbolos.tokentype> tokentypes) {

        for (int i = 0; i < tokentypes.size(); i++) {

            if (tokentypes.get(i) == TablaSimbolos.tokentype.Asignacion) {
                return i; // Retorno la posición del = 
            }
        }
        return -1; // si no existe retorno -1
    }

    //--Validación de los operandos 
    private void ValidarOperando(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum, int indiceOperando) {

        //Verifico que existan los dos operandos después del =
        if (indiceOperando >= tokentypes.size()) {
            String MensajeError = "ERROR 206: Formato erroneo. Operando faltante después del '='.";
            //System.out.println("Linea " + linenum + MensajeError);
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
                //System.out.println("Linea " + linenum + MensajeError);
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
                //System.out.println("Linea " + linenum + MensajeError);
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
        //System.out.println("Linea " + linenum + MensajeError);
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

            //--Convierto la linea en un string-- (Consultado a IA - Promtp #4)
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
    //--VALIDACION #4.1: Parentesis de apertura y cierre ()
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
            String MensajeError = "ERROR 300: Falta parentesis de apertura '(' en Console.WriteLine.";
            //System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }

        if (indiceCierre == -1) {
            String MensajeError = "ERROR 301: Falta parentesis de cierre ')' en Console.WriteLine.";
            //System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
        //Si los paréntesis no están en el orden correcto, marco error
        if (indiceApertura != -1 && indiceCierre != -1) {

            if (indiceCierre < indiceApertura) {
                String MensajeError = "ERROR 302: El parentesis de cierre ')' aparece antes del paréntesis de apertura '(' en Console.WriteLine.";
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            } else if (indiceCierre == indiceApertura) {
                String token = linea.get(indiceApertura);
                int posicionCierre = token.indexOf(')');
                int posicionApertura = token.indexOf('(');
                //Si el de cierre está antes que el de apertura. ERROR
                if (posicionCierre < posicionApertura) {
                    String MensajeError = "ERROR 302: El parentesis de cierre ')' aparece antes del parentesis de apertura '(' en Console.WriteLine.";
                    //System.out.println("Linea " + linenum + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }

        }
    }

    //--VALIDACION #4.2: Texto entre comillas("")
    private void ValidarContenidoParentesis(String lineaString, int linenum) {

        //REGISTRO ERRORES DE ()
        //Busco la posición del 1er "("
        int ParentesisAbrir = lineaString.indexOf("(");

        //Busco el último ")" de todo el string. Consulta a la IA. Prompt #10.
        int ParentesisCerrar = lineaString.lastIndexOf(")");

        //Verifico que se hayan encontrado "( )"
        if (ParentesisAbrir == -1 || ParentesisCerrar == -1 || ParentesisCerrar < ParentesisAbrir) {
            return; //Si no hay parentesis o están mal posicionados, retorno.
        }

        //Si los "( )" están correctos, extraigo el contenido dentro y elimino espacios.
        String ContenidoParentesis = lineaString.substring(ParentesisAbrir + 1, ParentesisCerrar).trim();

        //VALIDACION#4.3 NO PUEDEN ESTAR VACIOS LOS "( )"
        if (ContenidoParentesis.isEmpty()) {
            String MensajeError = "ERROR 303: El contenido dentro de parentesis no puede estar vacio.";
            //System.out.println("Linea " + linenum + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            return;
        }

        //VERIFICO COMENTARIOS CORRECTOS ENTRE ""
        //Busco la posición de las primeras comillas 
        int ComillaApertura = ContenidoParentesis.indexOf('"');

        //Si hay comillas de inicio, busco las de fin
        if (ComillaApertura != -1) {
            //Busco las otras comillas " a partir de la posición de las de inicio
            int ComillaCierre = ContenidoParentesis.indexOf('"', ComillaApertura + 1);

            //Si no encontré ComillaCierre, muestro error.
            if (ComillaCierre == -1) {
                String MensajeError = "ERROR 304: El texto a imprimir debe ir encerrado entre comillas dobles.";
                //System.out.println("Linea " + linenum + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }
    }

    //VALIDACION #5 ESTRUCTURA DE MODULE (ERRORES 400)
    public void ValidarEstructuraModule(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum, String CadenaOriginal) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //Valido si ya existe IMPORTS
        int indiceImports = -1;
        for (int i = 0; i < linea.size(); i++) {
            //Normalizo el token por si trae caracteres adicionales
            //Normalización consultada a la IA - Promtp #5
            String token = linea.get(i).replaceAll("[^A-Za-z]", "").toLowerCase();

            if (token.equals("imports")) {
                //Verifico que no exista un module líneas antes.
                if (EstaModule) {
                    String MensajeError = "ERROR 401: No deben existir Imports, despues de Module";
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
                indiceImports = i;
                EstaImports = true; //Si encuentro el token imports, activo la bandera
                break;
            }
        }

        //#1 **Verificar que MODULE esté después de IMPORTS**
        int indiceModule = -1;
        //Recorro la línea para ver si está Module
        for (int i = 0; i < linea.size(); i++) {
            // Normalizo el token por si trae caracteres adicionales
            String token = linea.get(i).replaceAll("[^A-Za-z]", "").toLowerCase();

            if (token.equals("module")) {
                indiceModule = i;
                EstaModule = true; //Module Encontrado

                //Verifico que Module sea la primera palabra de la linea (Por estructura)
                if (indiceModule == 0) {

                    //Si encuentro Module e Imports no está, ERROR.
                    if (!EstaImports) {
                        String MensajeError = "ERROR 400: Module debe aparecer despues de Imports";
                        //System.out.println("Linea " + linenum + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                    }
                    //#2 **Verificar que después de MODULE exista IDENT válido**
                    //Verifico que después de Module exista otro token
                    if (indiceModule + 1 >= tokentypes.size()) {
                        String MensajeError = "ERROR 402: Falta un identificador despues de Module";
                        //System.out.println("Linea " + linenum + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                        return;
                    }
                    //Verifico que el token después de Module sea un IDENT
                    if (tokentypes.get(indiceModule + 1) != TablaSimbolos.tokentype.Identificador) {
                        String MensajeError = "ERROR 403: Identificador Inválido despues de Module";
                        //System.out.println("Linea " + linenum + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                        return;
                    }
                    //#3 **Verificar que solo exista un espacio entre Modulo e Identificador**
                    //Quito los espacios iniciales de la Linea Original
                    String LineaCompleta = CadenaOriginal.trim();

                    //Guardo los dos siguientes caracteres despues de Module
                    //Verifico el caracter 6 - Debe ser un espacio
                    if (LineaCompleta.length() > 6) {
                        char PrimerCaracter = LineaCompleta.charAt(6);
                        //Verifico que sea un espacio - Extracto consultado a la IA y modificado Promtp #6
                        if (PrimerCaracter != ' ') {
                            String MensajeError = "ERROR 404: Entre Module e Identificador debe existir unicamente un espacio.";
                            //System.out.println("Linea " + linenum + MensajeError);
                            try {
                                registrador.EscribirError(linenum, MensajeError);
                            } catch (IOException ex) {
                                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            }
                            return;
                        }
                    }
                    //Verifico el caracter 7 - No debe haber más de un espacio.
                    if (LineaCompleta.length() > 7) {
                        char SegundoCaracter = LineaCompleta.charAt(7);

                        if (SegundoCaracter == ' ') {
                            String MensajeError = "ERROR 404: Entre Module e Identificador debe existir unicamente un espacio.";
                            //System.out.println("Linea " + linenum + MensajeError);
                            try {
                                registrador.EscribirError(linenum, MensajeError);
                            } catch (IOException ex) {
                                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            }
                            return;
                        }
                    }
                    break;
                }
            }

        }
    }

    //VALIDACION #6 END MODULE (ERRORES 500)
    public void ValidarEndModule(List<String> linea, String CadenaOriginal, int linenum) {

        //No verifico las líneas vacías
        if (CadenaOriginal == null || CadenaOriginal.trim().isEmpty()) {
            return;
        }

        //VALIDACION #1 END MODULE DEBE SER LA ÚLTIMA LINEA DEL CODIGO
        //Si ya se había detectado una línea EM y hay más líneas, ERROR.
        if (EstaEndModule && !Error500Detectado) {
            String MensajeError = "ERROR 500: 'End Module' debe ser la última línea del codigo.";
            //System.out.println("Linea " + linenum + ": " + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            Error500Detectado = true; //Si detecto al menos 1 vez el error, ya no lo reporto más.
            return;
        }

        //Verifico si la línea es END MODULE
        if (linea != null && linea.size() >= 2) {

            String token0 = linea.get(0).replaceAll("[^A-Za-z]", "").toLowerCase();
            String token1 = linea.get(1).replaceAll("[^A-Za-z]", "").toLowerCase();

            if (token0.equals("end") && token1.equals("module")) {

                //VALIDACION #2 DESPUES DE END MODULE NO DEBE HABER NADA MAS EN LA LINEA
                if (linea.size() > 2) {
                    String MensajeError = "ERROR 501: No debe aparecer nada más en la línea de END MODULE";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                    return;
                }

                //VALIDACION #3 ENTRE END y MODULE SOLO DEBE HABER UN ESPACIO
                //Quito los espacios iniciales de la Linea Original
                String LineaCompleta = CadenaOriginal.trim();

                //Guardo los dos siguientes caracteres despues de End
                //Verifico el caracter 3
                if (LineaCompleta.length() > 4) {
                    char PrimerCaracter = LineaCompleta.charAt(3);
                    char SegundoCaracter = LineaCompleta.charAt(4);
                    //Verifico que luego de END exista un espacio
                    if (PrimerCaracter != ' ') {
                        String MensajeError = "ERROR 502: Entre End y Module debe existir únicamente un espacio.";
                        //System.out.println("Linea " + linenum + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                        return;
                    }
                    //Si el siguiente caracter también es espacio. ERROR
                    if (SegundoCaracter == ' ') {
                        String MensajeError = "ERROR 502: Entre End y Module debe existir únicamente un espacio.";
                        //System.out.println("Linea " + linenum + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                        return;
                    }
                }
                EstaEndModule = true;
                return;
            }
        }
    }

    //VALIDACION #7 COMENTARIOS (ERRORES 600)
    public boolean ValidarComentarios(String CadenaOriginal, int linenum) {

        //Normalizo la linea para quitarle los espacios de adelante
        String LineaOriginal = CadenaOriginal.trim();

        //Verifico si la linea inicia con apóstrofe
        if (LineaOriginal.startsWith("'")) {
            return true; //Comentario Válido
        }

        //Verifico que la línea no tenga apóstrofe en otra posición
        if (!LineaOriginal.startsWith("'") && LineaOriginal.contains("'")) {
            String MensajeError = "ERROR 600: Comentario Invalido. La linea debe iniciar con 'apostrofe.";
            //System.out.println("Linea " + linenum + ": " + MensajeError);
            try {
                registrador.EscribirError(linenum, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            return false; //Esta linea tiene un comentario inválido
        }
        return false; //Esta linea no es un comentario 
    }

    //VALIDACION #8 COMANDO WHILE (ERRORES 700)
    public void ValidarBucleWhile(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum, String cadena) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //BUSCO SI ESTA WHILE
        //Normalizo el token por si trae caracteres adicionales
        String token = linea.get(0).replaceAll("[^A-Za-z]", "").toLowerCase();

        if (token.equals("while")) {
            PilaWhile.push(linenum); //Ingreso este While a la Pila.
            ContenidoWhileValido = false; //Bandera para revisar contenido

            //VERIFICO LA SINTAXIS DE LA CONDICION WHILE
            //Llamo a la función para verificar si coincide con alguna de las condiciones While.
            TablaExpresiones.expresiones tipoExpresion = TablaExpresiones.ValidarCondicionWhile(tokentypes);
            //Si no coincide, muestro error SINTACTICO
            if (tipoExpresion == null) {
                String MensajeError = "ERROR 700: Condicion WHILE, con sintaxis incorrecta.";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            } else { //Si cumple el formato, valido...

                //Verifico que la variable de la condición esté declarada.
                String VariableCondicion = linea.get(1).toLowerCase();
                if (!variablesDeclaradas.containsKey(VariableCondicion)) {
                    String MensajeError = "ERROR 701: La variable de la condicion While, debe estar declarada.";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
                //Verifico que la variable de la condición sea integer
                String TipoVariable = variablesDeclaradas.get(VariableCondicion);
                if (TipoVariable != null && !TipoVariable.equals("integer")) {
                    String MensajeError = "ERROR 702: La variable de la condicion While, debe ser de tipo INTEGER";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }

                //Verifico que después de la variable haya un número entero.
                //Regex (Consultado a la IA) Prompt #7
                if (linea.size() > 3) {
                    String Numero = linea.get(3);
                    if (!Numero.matches("^-?\\d+$")) {
                        String MensajeError = "ERROR 703: El Numero de la condicion While, debe ser un entero.";
                        //System.out.println("Linea " + linenum + ": " + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                    }
                }
            }
            return; //Si es While, salgo de la función para evaluar la siguiente línea
        }

        if (!PilaWhile.isEmpty()) {
            //VALIDO CONTENIDO dentro del ciclo (No vacio, no comentario)
            if (!cadena.trim().startsWith("'") && !token.equals("end")) {
                ContenidoWhileValido = true;
            }
        }

        //BUSCO SI ESTA END WHILE
        if (token.equals("end")) {

            //Verifico que este el WHILE de cierre (END WHILE)
            if (linea.size() > 1) {
                String token1 = linea.get(1).replaceAll("[^A-Za-z]", "").toLowerCase();

                //Si es WHILE de cierre, hago las validaciones de errores.
                if (token1.equals("while")) {

                    //Si no hay While de inicio, ERROR:
                    if (PilaWhile.isEmpty()) { //Pila Vacía.
                        String MensajeError = "ERROR 704: Debe existir un WHILE, antes del END WHILE";
                        //System.out.println("Linea " + linenum + ": " + MensajeError);
                        try {
                            registrador.EscribirError(linenum, MensajeError);
                        } catch (IOException ex) {
                            System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        }
                        return; //Salgo de la función, para no evaluar más si no está el While de inicio.
                    } else {

                        int lineaWhile = PilaWhile.pop();// Este EW cierra el While pendiente en la pila.

                        //SI HAY WHILE y END WHILE, valido contenido.
                        //Si el contenido del bucle es inválido. ERROR
                        if (!ContenidoWhileValido) {
                            String MensajeError = "ERROR 705: El bloque WHILE debe tener codigo a ejecutar.";
                            //System.out.println("Linea " + lineaInicioWhile + ": " + MensajeError);
                            try {
                                registrador.EscribirError(lineaWhile, MensajeError);
                            } catch (IOException ex) {
                                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                            }
                        }
                    }
                    //Limpio el contenido del ciclo.
                    ContenidoWhileValido = false;
                }
            }
        }
    }

    //-- VALIDACION #8.1: Validar cierre del bucle While.
    public void ExisteCierreWhile() {

        while (!PilaWhile.isEmpty()) { //Si no se vació la pila, muestro error.
            int lineaWhile = PilaWhile.pop(); //Guardo cuál linea de While es.

            String MensajeError = "ERROR 706: El bloque WHILE en la linea: " + lineaWhile + " debe cerrar con END WHILE.";
            //System.out.println("Linea " + linenum + ": " + MensajeError);
            try {
                registrador.EscribirError(lineaWhile, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        }
    }

    //VALIDACION #9 COMANDO FOR (ERRORES 800)
    public void ValidarBucleFor(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum, String cadena) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //BUSCO SI ESTA FOR
        boolean EstructuraValida = false; //Bandera para verificar sintaxis correcta.
        //Normalizo el token por si trae caracteres adicionales
        String token = linea.get(0).replaceAll("[^A-Za-z]", "").toLowerCase();

        if (token.equals("for")) {
            PilaFor.push(linenum); //Ingreso este For a la Pila.
            ContenidoForValido = false; //Bandera para revisar contenido

            //Valido si cumple con el tamaño correspondiente. -For variable_control = valor_inicial To valor_final- 
            if (linea.size() < 6) {
                EstructuraValida = false;
                String MensajeError = "ERROR 800: Formato invalido para el ciclo FOR.";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                PilaEstructuraFor.push(EstructuraValida);
                return; //salgo de la función, porque no cumple estructura.
            } else { //Si cumple con tamaño valido.
                EstructuraValida = true;
                //VALIDACIONES SINTÁCTICAS.
                //Verifico que tenga el =
                if (!linea.get(2).equals("=")) {
                    EstructuraValida = false;
                    String MensajeError = "ERROR 801: Sintaxis FOR incorrecta, el comando FOR debe tener un '='";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }

                //Verifico que valor inicial y valor final sean enteros
                String valor_inicial = linea.get(3);
                String valor_final = linea.get(5);

                if (!valor_inicial.matches("^-?\\d+$") || !valor_final.matches("^-?\\d+$")) {
                    EstructuraValida = false;
                    String MensajeError = "ERROR 802: Sintaxis FOR incorrecta, el valor inicial y/o valor final, deben ser numeros enteros.";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }

                //Verifico que aparezca el comando TO.
                String TokenTo = linea.get(4).replaceAll("[^A-Za-z]", "").toLowerCase();
                if (!TokenTo.equals("to")) {
                    EstructuraValida = false;
                    String MensajeError = "ERROR 803: Sintaxis FOR incorrecta, el comando FOR debe tener el comando TO";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
            PilaEstructuraFor.push(EstructuraValida);//Ingreso bool de estructura a la pila.
            return; //Salgo para validar la siguiente línea.
        }
        if (!PilaFor.isEmpty()) {
            //VALIDO CONTENIDO dentro del ciclo (No vacio, no comentario)
            if (!cadena.trim().startsWith("'") && !token.equals("next")) {
                ContenidoForValido = true;
            }
        }

        //Verifico que exista NEXT
        if (token.equals("next")) {

            if (PilaFor.isEmpty()) { //Pila Vacía.
                String MensajeError = "ERROR 804: Debe existir un FOR, antes del NEXT";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return; //Salgo de la función, para no evaluar más este ciclo.
            } else {

                int lineaFor = PilaFor.pop();// Este Next cierra el For pendiente en la pila.
                boolean estructura = PilaEstructuraFor.pop(); //Cierro la estructura de este ciclo.

                //Si existe TO y NEXT valido el contenido - Solo si la sintaxis es correcta.
                if (estructura && !ContenidoForValido) {
                    String MensajeError = "ERROR 805: El bloque FOR debe tener codigo valido a ejecutar.";
                    //System.out.println("Linea " + lineaFor + ": " + MensajeError);
                    try {
                        registrador.EscribirError(lineaFor, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
            //Limpio el contenido del ciclo.
            ContenidoForValido = false;
        }
    }

    //-- VALIDACION #9.1: Validar cierre del bucle FOR.
    public void ExisteCierreFor() {
        //Si hay un for en la Pila, error de cierre.
        while (!PilaFor.isEmpty()) {
            int lineaFor = PilaFor.pop();
            PilaEstructuraFor.pop();

            String MensajeError = "ERROR 806: El bloque FOR en la linea: " + lineaFor + " debe cerrar con NEXT.";
            //System.out.println("Linea " + linenum + ": " + MensajeError);
            try {
                registrador.EscribirError(lineaFor, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }

    //VALIDACION #9 COMANDO FOR (ERRORES 900)
    public void ValidarComandoIf(List<String> linea, List<TablaSimbolos.tokentype> tokentypes, int linenum, String cadena) {

        //Valido si la linea esta vacia
        if (linea == null || linea.isEmpty()) {
            return;
        }

        //BUSCO SI ESTA IF
        boolean EstructuraValida = false; //Bandera para verificar sintaxis correcta.
        //Normalizo el token por si trae caracteres adicionales
        String token = linea.get(0).replaceAll("[^A-Za-z]", "").toLowerCase();

        //Verifico si estoy en una condición IF.
        if (token.equals("if")) {
            PilaIf.push(linenum); //Ingreso este If a la Pila.

            //Busco que cumpla con: If-condicion-Then 
            //VALIDACIONES SINTÁCTICAS.
            //Busco dónde está then.
            int indiceThen = -1;
            for (int i = 0; i < linea.size(); i++) {
                String t = linea.get(i).replaceAll("[^A-Za-z]", "").toLowerCase();

                if (t.equals("then")) {
                    indiceThen = i;
                    break;
                }
            }
            if (indiceThen == -1) { //Si no está THEN. ERROR.

                EstructuraValida = false;
                String MensajeError = "ERROR 900: Sintaxis incorrecta, la sentencia IF debe tener THEN.";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                PilaEstructuraIf.push(EstructuraValida);//Ingreso bool de estructura a la pila.
                return; //Salgo de la función, porque no cumple estructura.

            } else if (indiceThen <= 1) { //Si no tiene CONDICION entre IF y THEN. ERROR.
                EstructuraValida = false;
                String MensajeError = "ERROR 901: Sintaxis incorrecta, la sentencia IF debe tener una CONDICION.";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                PilaEstructuraIf.push(EstructuraValida);//Ingreso bool de estructura a la pila.
                return; //Salgo de la función, porque no cumple estructura.

            } else {
                //SINTAXIS INICIO IF CORRECTO.
                EstructuraValida = true;
                PilaEstructuraIf.push(EstructuraValida);//Ingreso bool de estructura a la pila.
                return; //Salgo para validar la siguiente línea.
            }
        }

        //Verifico que exista ELSE.
        if (token.equals("else")) {
            EstaElse = true;

            if (PilaIf.isEmpty()) { //Pila Vacía.
                String MensajeError = "ERROR 902: Debe existir un IF, antes del ELSE";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                return; //Salgo de la función, para no evaluar más esta condición.
            }

            //Si llegué al ELSE y no encontré sentencia del THEN. Error.
            if (!SentenciaThen) {
                String MensajeError = "ERROR 903: El bloque THEN no contiene una sentencia valida.";
                //System.out.println("Linea " + linenum + ": " + MensajeError);
                try {
                    registrador.EscribirError(linenum, MensajeError);
                } catch (IOException ex) {
                    System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
            return; //Si hay else, retorno para evaluar la siguiente línea.
        }

        if (!PilaIf.isEmpty()) {
            //VALIDO SENTENCIAS dentro del comando IF (No vacio, no comentario)
            if (!cadena.trim().startsWith("'") && !token.equals("else") && !token.equals("end")) {

                if (!EstaElse) { //Estoy en la línea antes de llegar al ELSE.
                    SentenciaThen = true;
                } else {
                    SentenciaElse = true; //Ya estoy verificando la línea después de ELSE.
                }
            }
        }

        //VERIFICAR SI ESTA END IF
        if (token.equals("end") && linea.size() > 1) {

            String token1 = linea.get(1).replaceAll("[^A-Za-z]", "").toLowerCase();

            //Verificar si es END IF
            if (token1.equals("if")) {

                if (PilaIf.isEmpty()) { //Pila Vacía.
                    String MensajeError = "ERROR 904: Debe existir un IF, antes del END IF";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                    return; //Salgo porque la condición está incorrecta.
                }
                //CIERRO PILAS DEL COMANDO IF
                int lineaIf = PilaIf.pop();// Este End IF cierra el IF pendiente en la pila.
                boolean estructura = PilaEstructuraIf.pop(); //Cierro la estructura de este Comando if..

                //Si ya pasó ELSE y no encontré sentencia del ELSE. Error.
                if (estructura && EstaElse && !SentenciaElse) {
                    String MensajeError = "ERROR 905: El bloque ELSE no contiene una sentencia valida.";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }

                //Si no había ELSE, valido igual la sentencia del THEN.
                if (estructura && !EstaElse && !SentenciaThen) {
                    String MensajeError = "ERROR 903: El bloque THEN no contiene una sentencia valida.";
                    //System.out.println("Linea " + linenum + ": " + MensajeError);
                    try {
                        registrador.EscribirError(linenum, MensajeError);
                    } catch (IOException ex) {
                        System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }

                //Limpio el contenido del comando.
                SentenciaThen = false;
                SentenciaElse = false;
                EstaElse = false;
            }
        }
    }
    //-- VALIDACION #10.1: Validar cierre del bucle FOR.

    public void ExisteCierreIf() {
        //Si hay un for en la Pila, error de cierre.
        while (!PilaIf.isEmpty()) {
            int lineaIf = PilaIf.pop();
            PilaEstructuraIf.pop(); //

            String MensajeError = "ERROR 906: La Condicion IF en la linea: " + lineaIf + " debe cerrar con END IF.";
            //System.out.println("Linea " + linenum + ": " + MensajeError);
            try {
                registrador.EscribirError(lineaIf, MensajeError);
            } catch (IOException ex) {
                System.getLogger(Validador.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
}
