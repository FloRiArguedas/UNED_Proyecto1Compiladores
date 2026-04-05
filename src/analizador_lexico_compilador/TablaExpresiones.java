package analizador_lexico_compilador;

import analizador_lexico_compilador.TablaSimbolos.tokentype;
import java.util.List;

public class TablaExpresiones {

    public enum expresiones {

        //REGLAS DECLARACION DE VARIABLES VB
        //Dim nombreVariable As TipoDeDato
        DIM_F1(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato
        }),
        //OPCIONES FORMA #2
        //Dim nombreVariable As TipoDeDato = Valornum
        DIM_F2N(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato,
            tokentype.Asignacion,
            tokentype.Numero
        }),
        //Dim nombreVariable As TipoDeDato = ValorIdent
        DIM_F2I(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato,
            tokentype.Asignacion,
            tokentype.Identificador
        }),
        //OPCIONES FORMA #3
        // Dim nombreVariable As TipoDeDato = ID op ID
        DIM_F3_II(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato,
            tokentype.Asignacion,
            tokentype.Identificador,
            tokentype.OperadorAritmetico,
            tokentype.Identificador
        }),
        // Dim nombreVariable As TipoDeDato = ID op NUM
        DIM_F3_IN(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato,
            tokentype.Asignacion,
            tokentype.Identificador,
            tokentype.OperadorAritmetico,
            tokentype.Numero
        }),
        // Dim nombreVariable As TipoDeDato = NUM op ID
        DIM_F3_NI(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato,
            tokentype.Asignacion,
            tokentype.Numero,
            tokentype.OperadorAritmetico,
            tokentype.Identificador
        }),
        // Dim nombreVariable As TipoDeDato = NUM op NUM
        DIM_F3_NN(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Reservada,
            tokentype.Tipo_dato,
            tokentype.Asignacion,
            tokentype.Numero,
            tokentype.OperadorAritmetico,
            tokentype.Numero
        }),
        //ESTRUCTURAS CONDICION WHILE - ANÁLISIS SINTÁCTICO

        // While variable operador integer
        WHILE_CONDICION(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Operador,
            tokentype.Numero,}),
        // While variable igual integer
        WHILE_CONDICION_T2(new tokentype[]{
            tokentype.Reservada,
            tokentype.Identificador,
            tokentype.Asignacion,
            tokentype.Numero,});

        public final tokentype[] exp;

        expresiones(tokentype[] e
        ) {
            this.exp = e;
        }
    }

    //REVISO SI LA LINEA COINCIDE CON LAS EXPRESIONES
    private static boolean coincide(List<TablaSimbolos.tokentype> linea, TablaSimbolos.tokentype[] formato) {

        //Verificar si el tamaño coincide
        if (linea.size() == formato.length) {
            //Compara cada token
            for (int i = 0; i < formato.length; i++) {
                if (linea.get(i) != formato[i]) {
                    return false; //Si alguno no coincide return
                }
            }
            return true;  //Si coinciden retorno true
        }
        return false;
    }

    //VALIDO QUE TIPO DE EXPRESION DE DIM ES
    public static expresiones validar(List<TablaSimbolos.tokentype> linea) {

        if (linea == null || linea.isEmpty()) {
            return null;
        }

        //Verifico que la declaracion tenga asignacion "="
        boolean tieneIgual = linea.contains(TablaSimbolos.tokentype.Asignacion);

        //Si no tiene igual verifico que sea tipo DIM_F1 
        if (!tieneIgual) {
            if (coincide(linea, expresiones.DIM_F1.exp)) {
                return expresiones.DIM_F1;
            }
            return null;
        }

        //En caso de tener asignacion "=" Recorro las otras expresiones
        for (expresiones exp : expresiones.values()) {

            // Omito F1 porque con "=" no puede ser F1
            if (exp == expresiones.DIM_F1) {
                continue;
            }

            //comparo si la linea coincide con otras expresiones
            if (coincide(linea, exp.exp)) {
                return exp; //retorno la expresion
            }
        }
        return null;
    }

    //VALIDO SI CUMPLE FORMATOS DE CONDICION WHILE - INICIO DEL CICLO
    public static expresiones ValidarCondicionWhile(List<TablaSimbolos.tokentype> linea) {

        if (linea == null || linea.isEmpty()) {
            return null;
        }
        //Si coincide con formato  Tipo 1 retorno esa expresión.
        if (coincide (linea, expresiones.WHILE_CONDICION.exp)){
            return expresiones.WHILE_CONDICION;
        }
        //Si coincide con formato Tipo 2 retorno esa expresión.
        if (coincide (linea, expresiones.WHILE_CONDICION_T2 .exp)){
            return expresiones.WHILE_CONDICION_T2;
        }
        return null;
    }
}
